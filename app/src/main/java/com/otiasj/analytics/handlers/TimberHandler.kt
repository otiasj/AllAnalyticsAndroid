package com.otiasj.analytics.handlers

import android.app.Application

import timber.log.Timber

/**
 * Created by julien on 2/15/18.
 */

class TimberHandler : AnalyticHandler {
    override fun initialize(context: Application) {

    }

    override fun tagEvent(name: String, data: Map<String, Any>?) {
        Timber.v("Sending Analytic event:%s with data:%s", name, data?.keys ?: "none")
    }
}
