package com.otiasj.analytics.core

import android.app.Application
import android.support.v4.util.ArrayMap
import com.otiasj.analytics.core.Target.ALL
import com.otiasj.analytics.handlers.*
import timber.log.Timber
import java.util.*

/**
 * The Analytics class is a tool class meant to be used instead of specific 3rd party analytics sdks,
 * you can add additional analytic target by implementing a new AnalyticHandler
 */
open class Analytics {

    private val handlers = ArrayMap<Target, AnalyticHandler>()
    private val defaultParameters = ArrayMap<Target, Map<String, Any>>()

    /**
     * Method that allows us to handle turning on or off of the analytics libraries at random
     *
     * @param application the Application's reference
     * @param handlerId   the index for the analytics handler library
     * @param turnOn      true to turn them on, false to remove them
     */
    open fun initialize(application: Application, handlerId: Target, turnOn: Boolean) {

        val attrs = ArrayMap<String, Any>()
        var isInitialized: Boolean

        when (handlerId) {
            ALL -> {
                isInitialized = initLib(Target.CRASHLYTICS, turnOn, CrashlyticsHandler(), application)
                attrs["Crashlytics"] = isInitialized

                isInitialized = initLib(Target.FIREBASE, turnOn, FirebaseHandler(), application)
                attrs["Firebase"] = isInitialized

                isInitialized = initLib(Target.LOGSTASH, turnOn, LogStashHandler(), application)
                attrs["LogStash"] = isInitialized

                isInitialized = initLib(Target.TIMBER, turnOn, TimberHandler(), application)
                attrs["Timber"] = isInitialized
            }

            Target.CRASHLYTICS -> {
                isInitialized = initLib(Target.CRASHLYTICS, turnOn, CrashlyticsHandler(), application)
                attrs["Crashlytics"] = isInitialized
            }

            Target.FIREBASE -> {
                isInitialized = initLib(Target.FIREBASE, turnOn, FirebaseHandler(), application)
                attrs["Firebase"] = isInitialized
            }

            Target.LOGSTASH -> {
                isInitialized = initLib(Target.LOGSTASH, turnOn, LogStashHandler(), application)
                attrs["LogStash"] = isInitialized
            }

            Target.TIMBER -> {
                isInitialized = initLib(Target.TIMBER, turnOn, TimberHandler(), application)
                attrs["Timber"] = isInitialized
            }
        }

        newEvent("Analytics initialized").with(attrs).send()
    }

    /**
     * Create a new event for logging
     *
     * @param name name of the event occurring
     * @return the event builder for successful logging
     */
    open fun newEvent(name: String): AnalyticEvent {
        return AnalyticEvent(this, name)
    }

    /**
     * Add default parameters for the given target
     *
     * @param target
     * @param data
     */
    open fun addDefaultParameters(target: Target, data: MutableMap<String, Any>) {
        if (target == ALL) {
            Target.values().forEach {
                val previousData = defaultParameters[it]
                if (previousData != null) {
                    data.putAll(previousData)
                }
                defaultParameters[it] = data
            }
        } else {
            val previousData = defaultParameters[target]
            if (previousData != null) {
                data.putAll(previousData)
            }
            defaultParameters[target] = data
        }
    }

    /**
     * Send some data to a specific target, bypassing the creation of a AnalyticEvent,
     *
     * @param target
     * @param name
     * @param data
     * @param applyDefaultParam
     */
    internal fun tagEvent(target: Target, applyDefaultParam: Boolean, name: String, data: MutableMap<String, Any>?) {
        var data = data

        if (applyDefaultParam) { //Apply default param common to all handlers
            val defaultParam = defaultParameters[target]
            if (defaultParam != null) {
                if (data == null) {
                    data = LinkedHashMap()
                }
                data.putAll(defaultParam)
            }
        }

        if (target == ALL) {
            for ((currentTarget, value) in handlers) {

                if (applyDefaultParam) { //Apply default param specific to the current target
                    val defaultParam = defaultParameters[currentTarget]
                    if (defaultParam != null) {
                        if (data == null) {
                            data = LinkedHashMap()
                        }
                        data.putAll(defaultParam)
                    }
                }
                value.tagEvent(name, data)
            }
        } else {
            val analytic = handlers[target]
            if (analytic != null) {
                analytic.tagEvent(name, data)
            } else {
                Timber.e("Analytic is not initialized, skipping analytic event %s", name)
            }
        }
    }

    private fun initLib(target: Target, turnOn: Boolean, analyticHandler: AnalyticHandler,
                        application: Application): Boolean {
        if (turnOn) {
            try {
                analyticHandler.initialize(application)
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize analytic handler: " + target)
                return false
            }

            handlers[target] = analyticHandler
            return true
        } else {
            // Turn off, eg, remove
            handlers.remove(target)
            return false
        }
    }
}
