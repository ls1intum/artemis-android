import Foundation

struct Account: Decodable {
    var activated: Bool? = false
    var authorities: [String]? = []
    var username: String? = nil
    var email: String? = nil
    var name: String? = ""
    var isInternal: Bool? = true
    var firstName: String? = ""
    var lastName: String? = ""
    var langKey: String? = "en"
    var imageUrl: String? = nil
    var id: Int? = nil

    enum CodingKeys: String, CodingKey {
        case activated
        case authorities
        case username = "login"
        case email
        case name
        case isInternal = "internal"
        case firstName
        case lastName
        case langKey
        case imageUrl
        case id
    }
}