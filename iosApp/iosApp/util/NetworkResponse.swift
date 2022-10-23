//
// Created by Tim Ortel on 27.09.22.
//

import Foundation
import AsyncHTTPClient
import NIO
import NIOHTTP1

/**
 * Wrapper around network responses. Used to propagate failures correctly.
 */
enum NetworkResponse<T> {
    case response(data: T)
    case failure(error: Error)

    func bind<K>(f: (T) -> K) -> NetworkResponse<K> {
        switch self {
        case .response(data: let data): return NetworkResponse<K>.response(data: f(data))
        case .failure(error: let error): return NetworkResponse<K>.failure(error: error)
        }
    }
}

func performNetworkCall<T>(httpClient: HTTPClient, timeout: TimeAmount = .seconds(30), maxBodyByteSize: Int = 1024 * 1024, createRequest: () -> HTTPClientRequest, decode: (ByteBuffer) throws -> T) async -> NetworkResponse<T> {
    do {
        let response = try await httpClient.execute(createRequest(), timeout: timeout)

        if (response.status == .ok) {
            let body = try await response.body.collect(upTo: maxBodyByteSize)
            return NetworkResponse<T>.response(data: try decode(body))
        } else {
            return NetworkResponse<T>.failure(error: InvalidHttpResponseError(status: response.status))
        }
    } catch {
        return NetworkResponse<T>.failure(error: error)
    }
}

struct InvalidHttpResponseError: Error {
    let status: HTTPResponseStatus
}
