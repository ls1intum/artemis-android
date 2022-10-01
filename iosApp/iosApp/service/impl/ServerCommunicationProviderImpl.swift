//
// Created by Tim Ortel on 26.09.22.
//

import Foundation
import Combine
import CombineExt

/**
 * Provides data about which instance of artemis is communicated with.
 */
class ServerCommunicationProviderImpl: ServerCommunicationProvider {

    /**
     * The currently selected server.
     */
    var serverUrl: AnyPublisher<String, Never> = Just("https://artemis.ase.in.tum.de/").eraseToAnyPublisher()
}
