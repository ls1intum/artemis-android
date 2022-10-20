package de.tum.informatics.www1.artemis.native_app.util

import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * @return a [CoroutineScope] bound to the lifecylce of the [LifecycleOwner]. The returned scope is cancelled when
 * the [LifecycleOwner] is destroyed.
 */
fun LifecycleOwner.lifecycleScope(): CoroutineScope {
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lifecycle.doOnDestroy(scope::cancel)
    return scope
}