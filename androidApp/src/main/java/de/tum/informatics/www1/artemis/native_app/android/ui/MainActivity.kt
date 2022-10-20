package de.tum.informatics.www1.artemis.native_app.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import de.tum.informatics.www1.artemis.native_app.android.ui.courses_overview.CoursesOverview
import de.tum.informatics.www1.artemis.native_app.android.ui.login.LoginScreen
import de.tum.informatics.www1.artemis.native_app.ui.RootComponent

/**
 * Main and only activity used in the android app.
 * Navigation is handled by decompose and jetpack compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootComponent = RootComponent(defaultComponentContext())

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
                val stackState = rootComponent.childStack.subscribeAsState()

                //Display the current screen based on the config state in the root component nav stack
                when (val instance = stackState.value.active.instance) {
                    is RootComponent.NavGraphChild.CoursesOverview -> {
                        CoursesOverview(modifier = Modifier.fillMaxSize(), instance.component)
                    }
                    is RootComponent.NavGraphChild.Login -> {
                        LoginScreen(
                            modifier = Modifier.fillMaxSize(),
                            component = instance.component
                        )
                    }
                }
            }
        }
    }
}