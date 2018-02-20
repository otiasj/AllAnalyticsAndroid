package com.otiasj.analytics.handlers

import android.app.Application
import android.os.Bundle

import com.google.firebase.analytics.FirebaseAnalytics

import timber.log.Timber

class FirebaseHandler : AnalyticHandler {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun initialize(context: Application) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun tagEvent(name: String, data: Map<String, Any>?) {
        //Timber.v("Firebase Analytic event:%s with data:%s", name, ((data != null)? data.keySet() : "none"));

        val bundle = Bundle()
        if (data != null) {
            for ((key, value) in data) {
                val sanitizedKey = sanitizeString(key)
                if (value != null) {
                    bundle.putString(sanitizedKey, value.toString())
                } else {
                    bundle.putString(sanitizedKey, "")
                }
            }
        }
        val sanitizedName = "event_" + sanitizeString(name)
        mFirebaseAnalytics!!.logEvent(sanitizedName, bundle)

        if (sanitizedName.length > 500) {
            Timber.e("logging to firebase " + sanitizedName)
        }
    }

    /**
     * Because firebase does not support some characters in event names
     * @param entry
     * @return
     */
    private fun sanitizeString(entry: String): String {
        return "attr_" + entry.replace("[^A-Za-z0-9_]".toRegex(), "")
    }
}
