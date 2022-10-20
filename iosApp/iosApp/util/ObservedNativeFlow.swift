//
// Created by Tim Ortel on 18.09.22.
// Helper class that holds the latest emission of the latest flow.
// Similar to collectAsState(initial) in Jetpack Compose.
//

import Foundation
import SwiftUI
import appCommon
import KMPNativeCoroutinesCombine

class ObservedNativeFlow<T>: ObservableObject {

    @Published var latestEmission: T

    init(nativeFlow: @escaping (@escaping (T, KotlinUnit) -> KotlinUnit, @escaping (Error?, KotlinUnit) -> KotlinUnit) -> () -> KotlinUnit, initialValue: T) {
        latestEmission = initialValue

        createPublisher(for: nativeFlow)
                .replaceError(with: initialValue) //Kotlin flows do not emit errors.
                .receive(on: DispatchQueue.main)
                .assign(to: &$latestEmission)
    }
}