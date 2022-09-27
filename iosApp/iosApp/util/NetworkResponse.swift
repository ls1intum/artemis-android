//
// Created by Tim Ortel on 27.09.22.
//

import Foundation

/**
 * Wrapper around network responses. Used to propagate failures correctly.
 */
enum NetworkResponse<T> {
    case response(data: T)
    case failure(error: Error)
}
