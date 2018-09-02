package com.dailymotion.android.player.sampleapp

import android.content.Context
import com.dailymotion.websdksample.R

class CastOptionsProvider : OptionsProvider {
    fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.app_id))
                .build()
    }

    fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}