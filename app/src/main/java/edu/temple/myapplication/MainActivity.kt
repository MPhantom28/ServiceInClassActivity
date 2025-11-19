package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private var isBound = false
    private lateinit var textView: TextView

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val startMenuItem = menu?.findItem(R.id.action_play)
        val stopMenuItem = menu?.findItem(R.id.action_pause)

        if (isBound && timerBinder != null) {
            when {
                !timerBinder!!.isRunning && !timerBinder!!.paused -> {
                    startMenuItem?.title = "Start"
                }

                timerBinder!!.isRunning && !timerBinder!!.paused -> {
                    startMenuItem?.title = "Pause"
                }

                timerBinder!!.paused -> {
                    startMenuItem?.title = "Resume"
                }
            }
        } else {
            startMenuItem?.title = "Start"
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_play -> {
                handleStartAction()
                true
            }

            R.id.action_pause -> {
                handleStopAction()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private val timerHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            textView.text = msg.what.toString()
        }
    }
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder?.setHandler(timerHandler)
            isBound = true
            invalidateOptionsMenu()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<TextView>(R.id.textView)

        Intent(this, TimerService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

        private fun handleStartAction() {
            startService(Intent(this, TimerService::class.java))
            if (isBound) {
                if (!timerBinder!!.isRunning && !timerBinder!!.paused) {
                    timerBinder!!.start(100)
                } else if (timerBinder!!.isRunning && !timerBinder!!.paused) {
                    timerBinder!!.pause()
                } else if (timerBinder!!.paused) {
                    timerBinder!!.pause()
                }
                invalidateOptionsMenu()
            }
        }

        private fun handleStopAction() {
            if (isBound) {
                timerBinder!!.stop()
                textView.text = "Stopped"
                invalidateOptionsMenu()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            if (isBound) {
                unbindService(connection)
                isBound = false
            }
        }
}
