package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util

sealed class MemberSelectionMode {
    data object MemberSelectionList : MemberSelectionMode()
    data object MemberSelectionDropdown : MemberSelectionMode()
}