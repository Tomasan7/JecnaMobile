package me.tomasan7.jecnamobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JecnaMobileApplication : Application()
{
    companion object
    {
        const val NETWORK_AVAILABLE_ACTION = "me.tomasan7.jecnamobile.NETWORK_AVAILABLE"
        const val SUCCESSFUL_LOGIN_ACTION = "me.tomasan7.jecnamobile.SUCCESSFULL_LOGIN"
        const val SUCCESSFUL_LOGIN_FIRST_EXTRA = "first"
    }
}
