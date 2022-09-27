//
// Created by Tim Ortel on 27.09.22.
//
//

import Foundation
import UIKit
import Factory
import SwiftUI

extension LoginView {
    @MainActor class LoginViewModel: ObservableObject {
        @Injected(Container.accountService) private var accountService: AccountService

        func login(username: String, password: String, rememberMe: Bool) async -> LoginResponse {
            await accountService.login(username: username, password: password, rememberMe: rememberMe)
        }
    }
}
