package com.d108.familring.core

import android.app.Application
import com.d108.familring.BuildConfig
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FarmilringApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(TimberDebugTree())
        }

        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
        Timber.plant(Timber.DebugTree())
    }
}