package com.roxify.music.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.roxify.music.RoxifyApp
import com.roxify.music.di.AppContainer

/**
 * Small helper that builds a ViewModel from the app's [AppContainer] without pulling in a
 * DI framework. Usage: `rememberRoxifyViewModel { HomeViewModel(it) }`.
 */
@Composable
inline fun <reified VM : ViewModel> rememberRoxifyViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM
): VM {
    val context = LocalContext.current
    val application = context.applicationContext as RoxifyApp
    return viewModel(
        key = key,
        factory = viewModelFactory {
            initializer { create(application.container) }
        }
    )
}
