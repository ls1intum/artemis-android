//
// Created by Tim Ortel on 27.09.22.
//

import Foundation

/**
 * The data state of the request, either loading or the call is done.
 */
enum DataState<T> {
    case loading
    case done(response: NetworkResponse<T>)
}
