//
// Created by Tim Ortel on 01.10.22.
// Copyright (c) 2022 orgName. All rights reserved.
//

import Foundation
import Combine

/**
 * Adapted from: https://www.swiftbysundell.com/articles/creating-combine-compatible-versions-of-async-await-apis/
 */
extension AsyncSequence {
    func publisher() -> AnyPublisher<Element, Error> {
        AnyPublisher<Element, Error>.create { subscription in
            let task = Task {
                do {
                    for try await value in self {
                        subscription.send(value)
                    }

                    subscription.send(completion: .finished)
                } catch {
                    subscription.send(completion: .failure(error))
                }
            }

            return AnyCancellable {
                task.cancel()
            }
        }
    }
}

extension Publisher {
    func transformLatest<T>(_ transform: @escaping (Publishers.Create<T, Failure>.Subscriber, Output) async -> ()) -> AnyPublisher<T, Failure> {
        self
                .map { output in
                    AnyPublisher<T, Failure>.create { subscription in
                        let task = Task {
                            await transform(subscription, output)
                        }

                        return AnyCancellable {
                            task.cancel()
                        }
                    }
                }
                .switchToLatest()
                .eraseToAnyPublisher()
    }
}