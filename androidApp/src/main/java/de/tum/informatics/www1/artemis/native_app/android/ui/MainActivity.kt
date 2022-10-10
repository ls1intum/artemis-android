package de.tum.informatics.www1.artemis.native_app.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.ui.courses_overview.CoursesOverview
import de.tum.informatics.www1.artemis.native_app.android.ui.account.login.LoginScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * Main and only activity used in the android app.
 * Navigation is handled by decompose and jetpack compose.
 */
class MainActivity : ComponentActivity() {

    private val accountService: AccountService = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //When the user is logged in, immediately display the course overview.
        val startDestination = runBlocking {
            when (accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> Navigation.Dest.COURSE_OVERVIEW
                AccountService.AuthenticationData.NotLoggedIn -> Navigation.Dest.LOGIN
            }
        }

        setContent {
            MaterialTheme(
//                colors = Colors(
//                    primary = Color(0xff1e88e5),
//                    secondary = Color(0xff424242),
//                    primaryVariant = Color(0xff6ab7ff),
//                    secondaryVariant = Color(0xff6d6d6d),
//                    background = Color.White,
//                    surface = Color(0xffe4e5e6),
//                    error = Color(0xffffc107),
//                    onPrimary = Color(0xff000000),
//                    onSecondary = Color(0xffffffff),
//                    onBackground = Color(0xff000000),
//                    onSurface = Color.Black,
//                    onError = Color.Black,
//                    isLight = true
//                )
            ) {
                val navController = rememberNavController()

                //Use jetpack compose navigation for the navigation logic.
                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Navigation.Dest.LOGIN) {
                        LoginScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = getViewModel(),
                            onLogin = {
                                //Navigate to the course overview and remove the login screen from the navigation stack.
                                navController.navigate(Navigation.Dest.COURSE_OVERVIEW) {
                                    popUpTo(Navigation.Dest.LOGIN) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }

                    composable(Navigation.Dest.COURSE_OVERVIEW) {
                        CoursesOverview(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = getViewModel(),
                            onLogout = {
                                //Navigate to the login screen and remove the course overview screen from the navigation stack.
                                navController.navigate(Navigation.Dest.LOGIN) {
                                    popUpTo(Navigation.Dest.COURSE_OVERVIEW) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}