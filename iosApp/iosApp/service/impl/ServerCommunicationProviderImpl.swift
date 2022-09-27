//
// Created by Tim Ortel on 26.09.22.
//

import Foundation
import Combine

/**
 * Provides data about which instance of artemis is communicated with.
 */
class ServerCommunicationProviderImpl: ServerCommunicationProvider {

    /**
     * The currently selected server.
     * TODO: Convert to publisher.
     */
    var serverUrl = "https://artemis.ase.in.tum.de/"
}
