package com.leeeyou123.samplehandler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tv_verify_threadLocal).setOnClickListener { verifyThreadLocal() }
        findViewById<TextView>(R.id.tv_handler_leak).setOnClickListener {
            startActivity(Intent(this, HandlerLeakActivity::class.java))
        }
    }

    /**
     * 验证ThreadLocal全局唯一性，分别在主线程和子线程中反射获取sThreadLocal和mQueue，然后观察它们是否一致
     *
     * 期望：sThreadLocal一致，完全是同一个实例对象，因为它是static final的，隶属于类
     *      而mQueue是final的，是跟线程实例对象对应的，不同线程是不同的实例对象
     *
     * 运行日志：
     *       2021-12-16 11:12:20.449 17586-19810/com.leeeyou123.samplehandler E/MainActivity: ThreadName [Thread-6], sThreadLocal is [java.lang.ThreadLocal@5ac742b], mQueue is [android.os.MessageQueue@a14c088]
     *       2021-12-16 11:12:20.476 17586-17586/com.leeeyou123.samplehandler E/MainActivity: ThreadName [main], sThreadLocal is [java.lang.ThreadLocal@5ac742b], mQueue is [android.os.MessageQueue@6d71ecc]
     */
    private fun verifyThreadLocal() {
        Toast.makeText(this, "请查看logcat", Toast.LENGTH_SHORT).show()

        val looperClazz = Class.forName("android.os.Looper")

        // 在主线程中反射获取sThreadLocal和mQueue对象
        Looper.getMainLooper()?.also {
            Handler(it).post {
                val threadLocalField = looperClazz.getDeclaredField("sThreadLocal")
                val mqField = looperClazz.getDeclaredField("mQueue")
                threadLocalField.isAccessible = true
                mqField.isAccessible = true
                val threadLocalObj = threadLocalField.get(null) // 获取静态类型的实例对象
                val mqObj = mqField.get(it)

                val msg =
                    "ThreadName [${Thread.currentThread().name}], sThreadLocal is [${threadLocalObj}], mQueue is [${mqObj}]"
                Log.e(TAG, msg)
            }
        }

        // 在子线程中反射获取sThreadLocal和mQueue对象
        Thread {
            Looper.prepare() // 子线程中prepare looper
            Looper.myLooper()?.also {
                Handler(it).post { // 在子线程中创建Handler并post msg
                    val threadLocalField = looperClazz.getDeclaredField("sThreadLocal")
                    val mqField = looperClazz.getDeclaredField("mQueue")
                    threadLocalField.isAccessible = true
                    mqField.isAccessible = true
                    val threadLocalObj = threadLocalField.get(null) // 获取静态类型的实例对象
                    val mqObj = mqField.get(it)

                    val msg =
                        "ThreadName [${Thread.currentThread().name}], sThreadLocal is [${threadLocalObj}], mQueue is [${mqObj}]"
                    Log.e(TAG, msg)
                }
            }
            Looper.loop() // 开启子线程的loop循环
        }.start()
    }
}