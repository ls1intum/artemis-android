/**
 * This function can be used to remove the verbose formatting from the github release notes to be used
 * in the PlayStore relase notes.
 *
 * Replaces "* `Feature`: " with "-".
 * Removes "by @UserABC" at the the end of each line.
 *
 */
fun removeFormattingForGooglePlayReleaseNotes(input: String): String {
    val removedMentionsAndPrLinks = input.replace(Regex(" by @.*"), "")
    val replacedTypes = removedMentionsAndPrLinks.replace(Regex("`.*`: "), "")
    return replacedTypes.replace("*", "-")
}