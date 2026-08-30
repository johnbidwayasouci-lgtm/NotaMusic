package com.notationstudio.ui

import android.content.Context
import androidx.compose.runtime.Composable

object LocalContext {
    val current: Context
        @Composable get() = androidx.compose.ui.platform.LocalContext.current
}
