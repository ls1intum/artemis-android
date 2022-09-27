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

    @State private var isLoggedIn: Bool = false

    @Injected(Container.accountService) var accountService: AccountService

    init() {
        isLoggedIn = accountService.isLoggedIn()
    }

    var body: some View {
        VStack {
            if (isLoggedIn) {
                CoursesOverviewView()
            } else {
                LoginView()
            }

        }
                .onReceive(accountService.authenticationData.eraseToAnyPublisher()) { authData in
                    switch authData {
                    case AuthenticationData.NotLoggedIn: isLoggedIn = false
                    case AuthenticationData.LoggedIn: isLoggedIn = true
                    }
                }


    }
}