//
// Created by Tim Ortel on 27.09.22.
//

import Foundation
import Combine

/**
 * The data state of the request.
 */
enum DataState<T> {
    /**
     * Waiting until a valid internet connection is available again.
     */
    case suspended(error: Error?)

    /**
     * Currently loading.
     */
    case loading

    case failure(error: Error)

    case done(response: T)

    func bind<K>(op: (T) -> K) -> DataState<K> {
        switch self {
        case .suspended(error: let exception): return DataState<K>.suspended(error: exception)
        case .loading: return DataState<K>.loading
        case .failure(error: let exception): return DataState<K>.failure(error: exception)
        case .done(response: let response): return DataState<K>.done(response: op(response))
        }
    }
}

func retryOnInternet<T>(
        connectivity: AnyPublisher<NetworkStatus, Never>,
        baseBackoffMillis: UInt64 = 2000,
        perform: @escaping () async -> NetworkResponse<T>
) -> AnyPublisher<DataState<T>, Never> {
    connectivity
            .transformLatest { subscription, networkStatus in
                switch networkStatus {
                case .internet:
                    //Fetch data with exponential backoff
                    var currentBackoff = baseBackoffMillis * 1_000_000

                    while true {
                        subscription.send(DataState<T>.loading)

                        let response: NetworkResponse<T> = await perform()
                        switch response {
                        case .response(let data):
                            subscription.send(DataState<T>.done(response: data))
                            return
                        case .failure(let error):
                            subscription.send(DataState<T>.failure(error: error))
                        }

                        //Perform exponential backoff
                        try? await Task.sleep(nanoseconds: currentBackoff)
                        currentBackoff *= 2
                    }
                case .unavailable:
                    subscription.send(DataState<T>.suspended(error: nil))
                }
            }
}