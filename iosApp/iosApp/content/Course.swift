//
// Created by Tim Ortel on 27.09.22.
//

import Foundation

/**
 * Representation of a single course.
 */
struct Course: Codable {
    let id: Int
    let title: String
    let description: String
    let courseIcon: String
}
