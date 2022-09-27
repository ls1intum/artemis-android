//
//  RootView.swift
//  iosApp
//
//  Created by Tim Ortel on 16.09.22.
//
//

import SwiftUI
import Factory
import Combine

struct RootView: View {

    @ObservedObject private var isLoggedIn: IsLoggedIn

    init() {
        isLoggedIn = IsLoggedIn(accountService: Container.accountService())
    }

    var body: some View {
        VStack {
            if (isLoggedIn.isLoggedIn) {
                CoursesOverviewView()
            } else {
                LoginView()
            }

        }
//                .onReceive(accountService.authenticationData.eraseToAnyPublisher()) { authData in
//                    switch authData {
//                    case AuthenticationData.NotLoggedIn: isLoggedIn = false
//                    case AuthenticationData.LoggedIn: isLoggedIn = true
//                    }
//                }


    }
}

class IsLoggedIn: ObservableObject {
    @Published var isLoggedIn: Bool = false

    init(accountService: AccountService) {
        accountService
                .authenticationData
                .eraseToAnyPublisher()
                .map { authData in
                    switch authData {
                    case AuthenticationData.NotLoggedIn: return false
                    case AuthenticationData.LoggedIn: return true
                    }
                }
                .receive(on: DispatchQueue.main)
                .assign(to: &$isLoggedIn)
    }
}