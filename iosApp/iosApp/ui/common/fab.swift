import Foundation
import SwiftUI

/**
 * Adapted from: https://programmingwithswift.com/swiftui-floating-action-button/
 */
struct FloatingActionButton: View {

    let backgroundColor: Color
    let content: FabContent
    let action: () -> Void

    var body: some View {
            Button(action: action, label: {
                switch content {
                case .Text(let text):
                    Text(text)
                            .font(.system(.largeTitle))
                            .frame(width: 77, height: 70)
                            .foregroundColor(Color.white)
                            .padding(.bottom, 7)
                case .Icon(let systemName):
                    Image(systemName: systemName)
                }
            })
                    .background(backgroundColor)
                    .cornerRadius(38.5)
                    .padding()
                    .shadow(
                            color: Color.black.opacity(0.3),
                            radius: 3,
                            x: 3,
                            y: 3
                    )
    }
}

enum FabContent {
    case Text(text: LocalizedStringKey)
    case Icon(systemName: String)
}