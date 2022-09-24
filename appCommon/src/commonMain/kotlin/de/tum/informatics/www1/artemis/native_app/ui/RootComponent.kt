package de.tum.informatics.www1.artemis.native_app.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import de.tum.informatics.www1.artemis.native_app.service.AccountService
import de.tum.informatics.www1.artemis.native_app.ui.courses_overview.CoursesOverviewComponent
import de.tum.informatics.www1.artemis.native_app.ui.login.LoginComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Root component that handles the navigation in the client application.
 */
class RootComponent(componentContext: ComponentContext) : ComponentContext by componentContext,
    KoinComponent {

    private val accountService: AccountService = get()

    private val navigation = StackNavigation<Config>()

    private val stack = childStack(
        source = navigation,
        initialConfiguration = runBlocking {
            when (accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> Config.CoursesOverview
                AccountService.AuthenticationData.NotLoggedIn -> Config.Login
            }
        },
        handleBackButton = true,
        childFactory = ::createChild
    )

    val childStack: Value<ChildStack<*, NavGraphChild>> get() = stack

    sealed class NavGraphChild {
        class CoursesOverview(val component: CoursesOverviewComponent) : NavGraphChild()

        class Login(val component: LoginComponent) : NavGraphChild()
    }

    private sealed class Config : Parcelable {
        @Parcelize
        object CoursesOverview : Config()

        @Parcelize
        object Login : Config()
    }

    private fun createChild(config: Config, componentContext: ComponentContext): NavGraphChild {
        return when (config) {
            Config.CoursesOverview -> NavGraphChild.CoursesOverview(
                CoursesOverviewComponent(
                    componentContext,
                    onLogout = {
                        navigation.replaceCurrent(Config.Login)
                    }
                )
            )
            Config.Login -> NavGraphChild.Login(
                LoginComponent(
                    componentContext,
                    onLoggedIn = {
                        //Based on the assumption, that LoginComponent is always the single element on the stack.
                        navigation.replaceCurrent(Config.CoursesOverview)
                    }
                )
            )
        }
    }
}