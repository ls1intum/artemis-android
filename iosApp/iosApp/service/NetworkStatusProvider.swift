//
// Created by Tim Ortel on 19.10.22.
// Copyright (c) 2022 orgName. All rights reserved.
//

import Foundation
import Combine

/**
 * Service that provides the current connectivity status of this device.
 */
protocol NetworkStatusProvider {

    /**
     * Emits every time when the connectivity of this device to the internet changes.
     */
    var currentNetworkStatus: AnyPublisher<NetworkStatus, Never> { get }
}

enum NetworkStatus {
    case internet
    case unavailable
}