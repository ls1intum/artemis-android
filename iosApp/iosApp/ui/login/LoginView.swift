//
// Created by Tim Ortel on 24.09.22.
// Copyright (c) 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI

//struct LoginView: View {
//
//    var component: LoginComponent
//
//    @ObservedObject var username: ObservedNativeFlow<String>
//    @ObservedObject var password: ObservedNativeFlow<String>
//    @ObservedObject var rememberMe: ObservedNativeFlow<KotlinBoolean>
//    @ObservedObject var isLoginButtonEnabled: ObservedNativeFlow<KotlinBoolean>
//
//    init(component: LoginComponent) {
//        self.component = component
//
//        username = ObservedNativeFlow(nativeFlow: component.usernameNative, initialValue: "")
//        password = ObservedNativeFlow(nativeFlow: component.passwordNative, initialValue: "")
//        rememberMe = ObservedNativeFlow(nativeFlow: component.rememberMeNative, initialValue: false)
//        isLoginButtonEnabled = ObservedNativeFlow(nativeFlow: component.loginButtonEnabledNative, initialValue: false)
//    }
//
//    var body: some View {
//        let usernameBinding = Binding(
//                get: { username.latestEmission },
//                set: { newValue in component.updateUsername(newUsername: newValue) }
//        )
//
//        let passwordBinding = Binding(
//                get: { password.latestEmission },
//                set: { newValue in component.updatePassword(newPassword: newValue) }
//        )
//
//        let rememberMeBinding = Binding(
//                get: { rememberMe.latestEmission.boolValue },
//                set: { newValue in component.updateRememberMe(newRememberMe: newValue) }
//        )
//
//        VStack {
//            Spacer()
//
//            Text("Welcome to Artemis!")
//                    .font(.system(size: 35, weight: .bold))
//                    .padding(.horizontal, 10)
//                    .frame(maxWidth: .infinity)
//                    .multilineTextAlignment(.center)
//
//            Text("Please login with your TUM login credentials.")
//                    .font(.system(size: 25))
//                    .padding(.horizontal, 10)
//                    .frame(maxWidth: .infinity)
//                    .multilineTextAlignment(.center)
//
//            VStack(spacing: 10) {
//                TextField("Username", text: usernameBinding)
//                        .textFieldStyle(.roundedBorder)
//                SecureField("Password", text: passwordBinding)
//                        .textFieldStyle(.roundedBorder)
//
//                Toggle("Automatic login", isOn: rememberMeBinding)
//                        .toggleStyle(.switch)
//            }
//                    .frame(maxWidth: .infinity)
//                    .padding(.horizontal, 40)
//
//            Button("Login", action: {
//                component.login {
//
//                }
//            })
//                    .frame(maxWidth: .infinity)
//                    .disabled(!isLoginButtonEnabled.latestEmission.boolValue)
//                    .buttonStyle(.borderedProminent)
//
//            Spacer()
//        }
//                .frame(maxWidth: .infinity, maxHeight: .infinity)
//    }
//}
