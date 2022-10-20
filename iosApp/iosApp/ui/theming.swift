import Foundation
import SwiftUI

extension Color {
    static var outline: Color = Color("outline")

    static var primaryContainer = PrimaryContainer(surface: Color("primaryContainerSurface"), onSurface: Color("onPrimaryContainerSurface"))
}

struct PrimaryContainer {
    let surface: Color
    let onSurface: Color
}