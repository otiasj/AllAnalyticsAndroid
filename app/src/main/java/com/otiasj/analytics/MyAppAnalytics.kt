package com.otiasj.analytics

import android.app.Application
import com.otiasj.analytics.MyAppAnalytics.Event.Companion.LOGOUT_EVENT
import com.otiasj.analytics.core.AnalyticEvent
import com.otiasj.analytics.core.Analytics
import com.otiasj.analytics.core.Target
import timber.log.Timber

/**
 * Created by julien on 2/16/18.
 * Implement your custom initialization in this object
 */

object MyAppAnalytics {

    var analytics: Analytics = Analytics()

    fun initialize(application: Application) {
        Timber.v("initializing analytics.")
        initialize(application, Target.ALL, true)
    }

    /**
     * Statically initialize the singleton for handling analytics events,
     *
     * @param application reference to the Application
     */
    fun initialize(application: Application, handlerId: Target) {
        Timber.v("initializing analytics.")
        initialize(application, handlerId, true)
    }

    /**
     * Method that allows us to handle turning on or off of the analytics libraries at random
     *
     * @param application the Application's reference
     * @param handlerId   the index for the analytics handler library
     * @param turnOn      true to turn them on, false to remove them
     */
    fun initialize(application: Application, handlerIds: Target, turnOn: Boolean) {
        analytics.initialize(application, handlerIds, turnOn)
    }

    fun addDefaultParameters(target: Target, data: HashMap<String, Any>) {
        analytics.addDefaultParameters(target, data)
    }

    /**
     * You could create custom event here
     */
    fun onLogin(someLogin: String, someInfo: String) {
        val data = HashMap<String, Any>()
        data["login"] = someLogin
        data["something"] = someInfo
        analytics.addDefaultParameters(Target.ALL, data)
    }

    /**
     * or generate event like this
     */
    fun newEvent(name: String): AnalyticEvent {
        return analytics.newEvent(name)
    }

    /**
     *
     */
    fun onLogout(): AnalyticEvent {
        return analytics.newEvent(LOGOUT_EVENT)
    }

    class Event {
        companion object {
            const val LOGOUT_EVENT = "Something"
        }
    }
}
