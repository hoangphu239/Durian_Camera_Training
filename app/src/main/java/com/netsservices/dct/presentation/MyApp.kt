package com.netsservices.dct.presentation

import android.app.Application
import com.netsservices.dct.presentation.common.AppConfig
import com.netsservices.dct.presentation.common.LanguagePrefs
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            LanguagePrefs.getLanguageId(this@MyApp).collect {
                AppConfig.language = it
            }
        }
    }
}