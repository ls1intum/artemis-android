//
// Created by Tim Ortel on 26.09.22.
//

import Foundation
import Combine

protocol ServerCommunicationProvider {
    var serverUrl: AnyPublisher<String, Never> { get }
}
