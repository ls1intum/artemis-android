//
// Created by Tim Ortel on 27.09.22.
//

import Foundation

/**
 * Representation of a single course.
 */
struct Course: Codable, Identifiable {
    let id: Int
    var title: String? = ""
    var description: String? = ""
    var courseIcon: String? = nil
    var semester: String? = ""
    var registrationConfirmationMessage: String? = ""
}
