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
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private var isBound = false

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var textView: TextView

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
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById<Button>(R.id.startButton)
        stopButton = findViewById<Button>(R.id.stopButton)
        textView = findViewById<TextView>(R.id.textView)

        Intent(this, TimerService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startService(Intent(this, TimerService::class.java))
            if (isBound) {
                if (!timerBinder!!.isRunning && !timerBinder!!.paused) {
                    timerBinder!!.start(10)
                    startButton.text = "Pause"
                } else if (timerBinder!!.isRunning && !timerBinder!!.paused) {
                    timerBinder!!.pause()
                    startButton.text = "Resume"
                } else if (timerBinder!!.paused) {
                    timerBinder!!.pause()  // unpause
                    startButton.text = "Pause"
                }
            }
        }



        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isBound) {
                timerBinder!!.stop()
                startButton.text = "Start"
                textView.text = "Stopped"
            }
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

