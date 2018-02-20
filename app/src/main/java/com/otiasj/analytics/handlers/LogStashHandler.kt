package com.otiasj.analytics.handlers

import android.app.Application
import android.content.Context
import com.otiasj.analytics.service.LogStashService

/**
 * Created by julien on 2/15/18.
 */

class LogStashHandler : AnalyticHandler {
    private var context: Context? = null

    override fun initialize(context: Application) {
        this.context = context.applicationContext
    }

    override fun tagEvent(name: String, data: Map<String, Any>?) {
        if (context != null) {
            LogStashService.logEvent(context!!, name)
        }
    }
}
