//
//  RootView.swift
//  iosApp
//
//  Created by Tim Ortel on 16.09.22.
//
//

import SwiftUI
import appCommon

struct RootView: View {
    private let rootComponent: RootComponent
    
    @ObservedObject
    private var childStack: ObservableValue<ChildStack<AnyObject, RootComponent.NavGraphChild>>
    
    private var activeChild: RootComponent.NavGraphChild { childStack.value.active.instance }
    
    init(_ rootComponent: RootComponent) {
        self.rootComponent = rootComponent
        
        childStack = ObservableValue<ChildStack<AnyObject, RootComponent.NavGraphChild>>(rootComponent.childStack)
    }
    
    var body: some View {
        VStack {
            ChildView(child: activeChild)
                .frame(maxHeight: .infinity)
        }
    }
}

private struct ChildView: View {
    let child: RootComponent.NavGraphChild
    
    var body: some View {
        switch child {
        case let child as RootComponent.NavGraphChildCoursesOverview: CoursesOverviewView(component: child.component)
        case let child as RootComponent.NavGraphChildLogin: LoginView(component: child.component)
        default: EmptyView()
        }
    }
}
