import Foundation
import SwiftUI

extension Color {
    static var outline: Color = Color("outline")

    static var primaryContainer = PrimaryContainer(
            primaryContainer: Color("primaryContainer"),
            surface: Color("primaryContainerSurface"),
            onSurface: Color("onPrimaryContainerSurface"),
            onPrimaryContainer: Color("onPrimaryContainer")
    )
}

struct PrimaryContainer {
    let primaryContainer: Color
    let surface: Color
    let onSurface: Color
    let onPrimaryContainer: Color
}