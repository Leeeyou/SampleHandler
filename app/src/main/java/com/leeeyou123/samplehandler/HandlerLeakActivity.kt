package com.leeeyou123.samplehandler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread

class HandlerLeakActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handler_leak)

        findViewById<TextView>(R.id.tv_leak_1).setOnClickListener {
            mHandler.sendEmptyMessageDelayed(0, 20000)
            Toast.makeText(this, "请查看LeakCanary", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<TextView>(R.id.tv_leak_2).setOnClickListener {
            Toast.makeText(this, "请查看LeakCanary", Toast.LENGTH_SHORT).show()
            thread {
                finish()
                Thread.sleep(20000)
                mHandler.sendEmptyMessage(1)
            }
        }
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0 -> findViewById<TextView>(R.id.tv_leak_1).text = "发送延迟消息了"
                1 -> findViewById<TextView>(R.id.tv_leak_2).text = "开启子线程耗时任务了"
            }
        }
    }

}