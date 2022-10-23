import Foundation
import SwiftUI
import Combine


class ObservedValue<T>: ObservableObject {
    @Published var latestValue: T

    init(publisher: AnyPublisher<T, Never>, initialValue: T) {
        latestValue = initialValue

        publisher
                .receive(on: DispatchQueue.main)
                .assign(to: &$latestValue)
    }
}