//
// Created by Tim Ortel on 27.09.22.
// Copyright (c) 2022 orgName. All rights reserved.
//

import Foundation
import AsyncHTTPClient

class HTTPClientProvider {
    let httpClient = HTTPClient(eventLoopGroupProvider: .createNew)
}
