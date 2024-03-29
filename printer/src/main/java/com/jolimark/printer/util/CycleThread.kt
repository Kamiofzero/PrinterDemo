package com.jolimark.printer.util

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

abstract class CycleThread : Thread() {

    private var lock = ReentrantLock()
    private var condition: Condition = lock.newCondition()

    private var waitMilliSeconds: Long = 50

    //    private var breakSignal: Boolean = false
    private var timeoutMilliSeconds: Long = 0

    fun setWaitMilliSeconds(milliSeconds: Long) {
        waitMilliSeconds = milliSeconds
    }

    fun setTimeoutMilliSeconds(milliSeconds: Long) {
        timeoutMilliSeconds = milliSeconds
    }


    abstract fun doBeforeCycle()
    abstract fun doInCycle()
    abstract fun doAfterCycle()

    private var timer = Timer()
    private var timerTask: TimerTask? = null
    private var isTimeout: Boolean = false

    protected fun isTimeout() = isTimeout

    //线程外部调用，打断循环
    fun stopCycle() {
        lock.lock()
        try {
            interrupt()
        } finally {
            lock.unlock()
        }
    }

    //doInCycle内调用的，调用doInCycle结束后，马上break循环
    fun callBreak() {
        interrupt()
    }

    override fun run() {
        doBeforeCycle()
        if (timeoutMilliSeconds > 0) {
            timerTask = object : TimerTask() {
                override fun run() {
                    isTimeout = true
                    stopCycle()
                }
            }
            timer.schedule(timerTask, timeoutMilliSeconds)
        }
        while (true) {
            doInCycle()
//            if (breakSignal) break

            lock.lock()
            if (isInterrupted) break
            try {
                condition.await(waitMilliSeconds, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                break
            } finally {
                lock.unlock()
            }
        }
        timerTask?.cancel()
        timer.cancel()
        doAfterCycle()
    }

}