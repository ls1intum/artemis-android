import Foundation

/**
 * From: https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/home/saml2-login/saml2.config.ts
 */
struct Saml2Config: Decodable {
    var identityProviderName: String? = nil
    var buttonLabel: String? = nil
    var passwordLoginDisabled: Bool = false
    var enablePassword: Bool = false

    enum CodingKeys: String, CodingKey {
        case identityProviderName
        case buttonLabel = "button-label"
        case passwordLoginDisabled
        case enablePassword
    }
}
