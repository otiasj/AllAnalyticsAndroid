package com.otiasj.analytics.handlers

import android.app.Application

interface AnalyticHandler {

    fun initialize(context: Application)

    /**
     * Effectively log the message in the current Analytic Handler
     * @param name
     * @param data
     */
    fun tagEvent(name: String, data: Map<String, Any>?)

}
