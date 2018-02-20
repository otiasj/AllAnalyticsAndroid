package com.otiasj.analytics.core

import java.util.*

class AnalyticEvent internal constructor(private val sender: Analytics, private val name: String) {

    private var data: MutableMap<String, Any>? = null

    private val targets = HashSet<Target>()

    /**
     * Add some key/value parameters to be sent with the event
     *
     * @param data
     * @return
     */
    fun with(data: MutableMap<String, Any>): AnalyticEvent {
        if (this.data == null) {
            this.data = data
        } else {
            this.data!!.putAll(data)
        }

        return this
    }

    /**
     * Specify one or multiple analytics target @AnalyticsTarget.CRASHLYTICS, @AnalyticsTarget.FIREBASE...
     * by default the event is sent to ALL analytics handlers
     *
     * @param target one of @AnalyticsTarget.CRASHLYTICS or @AnalyticsTarget.FIREBASE or both
     * @return
     */
    fun to(vararg target: Target): AnalyticEvent {
        for (targetId in target) {
            targets.add(targetId)
        }
        return this
    }

    fun send() {
        // send to all by default
        if (targets.isEmpty()) {
            sender.tagEvent(Target.ALL, true, name, data)
        } else {
            for (target in targets) {
                sender.tagEvent(target, true, name, data)
            }
        }
    }
}
