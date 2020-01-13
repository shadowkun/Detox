package com.wix.detox.reactnative.idlingresources

import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactContext
import org.joor.Reflect
import java.util.*

class TimerReflected(timer: Any) {
    private var reflected = Reflect.on(timer)

    val isRepeating: Boolean
        get() = reflected.field("mRepeat").get()
    val interval: Int
        get() = reflected.field("mInterval").get()
    val targetTime: Long
        get() = reflected.field("mTargetTime").get()
}

class TimingModuleReflected(private val nativeModule: NativeModule) {
    val timersQueue: PriorityQueue<Any>
        get() = Reflect.on(nativeModule).field("mTimers").get()
    val timersLock: Any
        get() = Reflect.on(nativeModule).field("mTimerGuard").get()

    operator fun component1() = timersQueue
    operator fun component2() = timersLock
}

interface IdleInterrogationStrategy {
    fun isIdleNow(): Boolean
}

class DefaultIdleInterrogationStrategy
    internal constructor(private val timersModule: NativeModule)
    : IdleInterrogationStrategy {

    override fun isIdleNow(): Boolean {
        val (timersQueue, timersLock) = TimingModuleReflected(timersModule)
        synchronized(timersLock) {
            val nextTimer = timersQueue.peek()
            nextTimer?.let {
                return !isTimerInBusyWindow(it) && !hasBusyTimers(timersQueue)
            }
            return true
        }
    }

    private fun isTimerInBusyWindow(timer: Any): Boolean {
        val timerReflected = TimerReflected(timer)
        return when {
            timerReflected.isRepeating -> false
            timerReflected.interval > BUSY_WINDOW_THRESHOLD -> false
            else -> true
        }
    }

    private fun hasBusyTimers(timersQueue: PriorityQueue<Any>): Boolean {
        timersQueue.forEach {
            if (isTimerInBusyWindow(it)) {
                return true
            }
        }
        return false
    }
}

class RN62IdleInterrogationStrategy : IdleInterrogationStrategy {
    override fun isIdleNow(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

fun getInterrogationStrategy(reactContext: ReactContext): IdleInterrogationStrategy {
    val timingClass: Class<NativeModule> = Class.forName("com.facebook.react.modules.core.Timing") as Class<NativeModule>
    return DefaultIdleInterrogationStrategy(reactContext.getNativeModule(timingClass))
}
