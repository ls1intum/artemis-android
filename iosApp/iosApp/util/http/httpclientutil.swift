import Foundation
import AsyncHTTPClient
import NIO

extension HTTPClient {
    /**
     * Closes the http client after the block has been executed
     */
    func use<T>(perform: (HTTPClient) async -> T) async -> T {
        let result = await perform(self)
        try? syncShutdown()
        return result
    }
}

