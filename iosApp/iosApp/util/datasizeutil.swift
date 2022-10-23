import Foundation

extension Int {
    func KB() -> Int {
        self * 1024
    }

    func MB() -> Int {
        KB() * 1024
    }
}