package com.otiasj.analytics

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.otiasj.analytics.core.Target
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        //To be called from your application
        MyAppAnalytics.initialize(application)

        //you can turn on or off specific logger:
//        MyAppAnalytics.initialize(application, Analytics.CRASHLYTICS, true)
//        MyAppAnalytics.initialize(application, Analytics.FIREBASE, false)

        //setDefault parameters to be sent with every event
        val parametersForAll = LinkedHashMap<String, Any>()
        parametersForAll["Some Default parameter"] = "For All"
        parametersForAll["Another Default parameter"] = "For All to"

        MyAppAnalytics.addDefaultParameters(Target.ALL, parametersForAll)

        val justInTimberLog = LinkedHashMap<String, Any>()
        justInTimberLog["Just"] = "In"
        justInTimberLog["Timber"] = "Log"
        MyAppAnalytics.addDefaultParameters(Target.TIMBER, justInTimberLog)

        // A simple event
        MyAppAnalytics.newEvent("My analytic event!").send()

        // An event with parameters
        val data = LinkedHashMap<String, Any>()
        data["some Int"] = 3
        data["A String!"] = "Hello World"
        data["a timestamp"] = System.currentTimeMillis()

        MyAppAnalytics.newEvent("My analytic event with parameters!").with(data).send()

        //Specify loggers
        MyAppAnalytics.newEvent("An Event TO ALL").to(Target.ALL).send() // by default it is sent to every handler
        MyAppAnalytics.newEvent("Only Timber").to(Target.TIMBER).send()
        MyAppAnalytics.newEvent("crashlytics and firebase").to(Target.CRASHLYTICS, Target.FIREBASE).send()

    }
}
