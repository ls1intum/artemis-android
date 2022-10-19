import Foundation
import Factory

@MainActor class AccountViewController: ObservableObject {

    private let serverCommunicationProvider: ServerCommunicationProvider = Container.serverCommunicationProvider()

    @Published var serverProfileInfo: DataState<ProfileInfo> = DataState.loading
}