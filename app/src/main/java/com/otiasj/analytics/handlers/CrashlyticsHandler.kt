package com.otiasj.analytics.handlers

import android.app.Application

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.core.CrashlyticsCore

import io.fabric.sdk.android.Fabric

class CrashlyticsHandler : AnalyticHandler {

    override fun initialize(context: Application) {
        val crashlytics = CrashlyticsCore.Builder().build()
        Fabric.with(context, crashlytics, Answers())
    }

    override fun tagEvent(name: String, data: Map<String, Any>?) {
        //Timber.v("Crashlytics Analytic event:%s with data:%s", name, ((data != null)? data.keySet() : "none"));
        val customEvent = CustomEvent(name)
        if (data != null) {
            for ((key, value) in data) {
                customEvent.putCustomAttribute(key, "" + value)
            }
        }
        Answers.getInstance().logCustom(customEvent)
    }
}
