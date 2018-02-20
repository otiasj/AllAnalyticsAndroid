package com.otiasj.analytics.service

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import timber.log.Timber
import java.io.*
import java.net.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by julien on 2/20/18.
 */
class LogStashService : IntentService("LoggerService") {

    var mode = LogMod.silent

    enum class LogMod {
        silent,
        active
    }

    override fun onCreate() {
        super.onCreate()
        setCleanupWakeAlarm(DAY.toLong())
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action

        if (action != null) {
            when {
                action.equals(ACTION_LOG, ignoreCase = true) -> {
                    val log = intent.getStringExtra(EXTRA_LOG)
                    if (TextUtils.isEmpty(log)) {
                        return
                    }
                    Timber.d("mode:" + this.mode + ". got log:" + log)

                    when (this.mode) {
                        LogStashService.LogMod.silent -> writeLogToFile(log)
                        LogStashService.LogMod.active -> sendLogToServer(log)
                    }
                }
                action.equals(ACTION_SET_MODE, ignoreCase = true) -> {
                    val newMode = intent.getIntExtra(EXTRA_MODE, LogMod.silent.ordinal)
                    setLogMode(LogMod.values()[newMode])
                }
                action.equals(ACTION_CLEANUP, ignoreCase = true) -> // delete old log file if needed. only keep 7 days of logs
                    deleteOldLogFile()
            }
        }
    }

    private fun sendLogToServer(logStr: String?) {
        if (logStr == null) {
            return
        }
        val socket: DatagramSocket?
        val host: InetAddress?
        try {
            socket = DatagramSocket()
            host = InetAddress.getByName(URL(LOGSTASH_SERVER_URL).host)
        } catch (e: SocketException) {
            Timber.e(e, "couldn't send log:")
            return
        } catch (e: UnknownHostException) {
            Timber.e(e, "couldn't send log:")
            return
        } catch (e: MalformedURLException) {
            Timber.d(e, "couldn't send log:")
            return
        }

        val messageLength = logStr.length
        val message = logStr.toByteArray()
        if (host != null) {
            val p = DatagramPacket(message, messageLength, host, LOGSTASH_UDP_JSON_PORT)
            try {
                socket.send(p)
            } catch (e: IOException) {
                Timber.e(e, "couldn't send:")
                return
            }

        }
    }

    private fun writeLogToFile(log: String) {
        val dateStr = dateFormat.format(Date())
        val fileName = LOGSTASH_FILE_PREFIX + dateStr
        var bufferedWriter: BufferedWriter? = null
        try {
            val outputStream = openFileOutput(fileName, Context.MODE_APPEND)
            val out = DataOutputStream(outputStream)
            bufferedWriter = BufferedWriter(OutputStreamWriter(out))
            bufferedWriter.write(log)
            bufferedWriter.newLine()
        } catch (e: FileNotFoundException) {
            Timber.e(e, "couldn't write log:")
        } catch (e: IOException) {
            Timber.e(e, "couldn't write log:")
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close()
                } catch (e: IOException) {
                    Timber.e(e, "failed to close BufferedWriter:")
                }

            }
        }
    }

    private fun setLogMode(newMode: LogMod) {
        if (newMode == this.mode) {
            return
        }
        val oldMode = this.mode
        this.mode = newMode
        if (oldMode == LogMod.silent && newMode == LogMod.active) {
            // activating the logging, send all the accumulated logs
            flushLogsToServer()
        }
    }

    private fun deleteOldLogFile() {
        // get the date of MAX_LOG_DAYS days ago
        val dateStr = getDayString(-MAX_LOG_DAYS)

        // delete the old (week ago) file
        val fileName = LOGSTASH_FILE_PREFIX + dateStr
        deleteFile(fileName)

        // schedule the logs deletion to occur once a day
        setCleanupWakeAlarm(DAY.toLong())
    }

    private fun getDayString(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        val newDate = calendar.time
        return dateFormat.format(newDate)
    }

    private fun flushLogsToServer() {
        // send log file one by one (each log file is a day of logs)
        for (i in MAX_LOG_DAYS downTo 0) {
            val dateStr = getDayString(-i)
            val fileName = LOGSTASH_FILE_PREFIX + dateStr
            sendLogFile(fileName)
            // delete the log file
            deleteFile(fileName)
        }
    }

    /**
     * Sends a log file to the server, line by line - each line is a separate log.
     *
     * @param fileName log file name
     */
    private fun sendLogFile(fileName: String) {
        var fileInputStream: FileInputStream?
        try {
            fileInputStream = openFileInput(fileName)
        } catch (e: FileNotFoundException) {
            Timber.e(e, "couldn't open log file")
            return
        }

        // Get the object of DataInputStream
        val dataInputStream = DataInputStream(fileInputStream!!)
        val bufferedReader = BufferedReader(InputStreamReader(dataInputStream))

        try {
            //useLines closes the inputstream automatically
            bufferedReader.useLines {
                it.map { line ->
                    sendLogToServer(line)
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "couldn't send log to server:")
        }
    }

    private fun setCleanupWakeAlarm(interval: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval,
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_CLEANUP), 0))
    }

    companion object {

        const val ACTION_LOG = "LOGGER_SERVICE_ACTION_LOG"
        const val ACTION_SET_MODE = "LOGGER_SERVICE_ACTION_SET_MODE"
        const val ACTION_CLEANUP = "LOGGER_SERVICE_ACTION_CLEANUP" // Set by an alarm for daily old log files cleanup

        const val EXTRA_LOG = "EXTRA_LOG"
        const val EXTRA_MODE = "EXTRA_MODE"

        private const val LOGSTASH_SERVER_URL = "http://logger.yaloapp.com/"
        private const val LOGSTASH_UDP_JSON_PORT = 5958
        private const val LOGSTASH_FILE_PREFIX = "logstash_"
        private const val MAX_LOG_DAYS = 7
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        private const val DAY = 24 * 60 * 60 * 1000 // in milliseconds

        /**
         * Start this service to perform a writing action with the given parameters. If
         * the service is already performing a task this action will be queued.*
         *
         * @param context
         * @param log     the log row to be written
         */
        fun logEvent(context: Context, log: String) {
            val intent = Intent(context, LogStashService::class.java)
            intent.action = ACTION_LOG
            intent.putExtra(EXTRA_LOG, log)

            context.startService(intent)
        }

        /**
         * Start this service to change the way the service behaves. If
         * the service is already performing a task this action will be queued.
         *
         * @param context
         * @param newMode the new mode ordinal to be set
         */
        fun changeMode(context: Context, newMode: LogMod) {
            val intent = Intent(context, LogStashService::class.java)
            intent.action = ACTION_SET_MODE
            intent.putExtra(EXTRA_MODE, newMode.ordinal)

            context.startService(intent)
        }
    }
}