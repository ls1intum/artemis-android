import Foundation
import Combine
import CombineExt
import Reachability

class NetworkStatusProviderImpl: NetworkStatusProvider {

    /**
     * Use https://github.com/ashleymills/Reachability.swift to gather information about the internet access of this device.
     */
    var currentNetworkStatus: AnyPublisher<NetworkStatus, Never> = AnyPublisher.create { subscriber in
        do {
            let reachability: Reachability = try Reachability()

            reachability.whenReachable = { r in
                subscriber.send(NetworkStatus.internet)
            }

            reachability.whenUnreachable = { r in
                subscriber.send(NetworkStatus.unavailable)
            }

            try! reachability.startNotifier()

            return AnyCancellable {
                reachability.stopNotifier()
            }
        } catch {
            //Apparently we cannot gather information using reachability. Therefore, we just say the device always has internet.
            subscriber.send(NetworkStatus.internet)

            return AnyCancellable {}
        }
    }
}
