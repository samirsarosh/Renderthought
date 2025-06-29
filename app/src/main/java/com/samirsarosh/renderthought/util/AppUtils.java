package com.samirsarosh.renderthought.util;


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class SpringAnimatorCustom(
    private val from: Float,
    private val to: Float,
    private val onUpdate: (Float) -> Unit,
    private val onEnd: (() -> Unit)? = null
) : Animator() {

    private val valueHolder = FloatValueHolder(from)
    private val springAnim = SpringAnimation(valueHolder).apply {
        spring = SpringForce(to).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }
        addUpdateListener { _, value, _ -> onUpdate(value) }
        addEndListener { _, _, _, _ ->
            isRunningInternal = false
            notifyEnd()
        }
    }

    private var isRunningInternal = false
    private val animatorListeners = mutableListOf<Animator.AnimatorListener>()

    override fun start() {
        if (isRunningInternal) return
        isRunningInternal = true
        notifyStart()
        springAnim.start()
    }

    override fun cancel() {
        if (!isRunningInternal) return
        springAnim.cancel()
        isRunningInternal = false
        notifyCancel()
        notifyEnd()
    }

    override fun isRunning(): Boolean = isRunningInternal

    override fun getDuration(): Long = -1L // Not time-based
    override fun setDuration(duration: Long): Animator = this // Ignored

    override fun getStartDelay(): Long = 0L
    override fun setStartDelay(startDelay: Long) {
        // No-op: SpringAnimation does not support delay natively
        // You can implement delay manually using Handler/Coroutine if needed
    }

    override fun setInterpolator(timeInterpolator: android.animation.TimeInterpolator?) {
        // No-op: SpringAnimation does not support interpolators
    }

    override fun getInterpolator(): android.animation.TimeInterpolator? = null

    override fun addListener(listener: Animator.AnimatorListener) {
        animatorListeners.add(listener)
    }

    override fun removeListener(listener: Animator.AnimatorListener) {
        animatorListeners.remove(listener)
    }

    override fun removeAllListeners() {
        animatorListeners.clear()
    }

    private fun notifyStart() {
        animatorListeners.forEach { it.onAnimationStart(this) }
    }

    private fun notifyEnd() {
        animatorListeners.forEach { it.onAnimationEnd(this) }
        onEnd?.invoke()
    }

    private fun notifyCancel() {
        animatorListeners.forEach { it.onAnimationCancel(this) }
    }
}

/**
 * Created by samirsarosh on 11/12/18.
 */

public class AppUtils {
}


interface SpringAnimationController {
    fun start()
    fun cancel()
    fun isRunning(): Boolean
    fun getValue(): Float
    fun addEndListener(listener: () -> Unit)
    fun addUpdateListener(listener: (Float) -> Unit)
}


class SpringPhysicsAnimator(
    private val from: Float,
    private val to: Float,
    stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
    damping: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
    private val initialVelocity: Float = 0f
) : SpringAnimationController {

    private val valueHolder = FloatValueHolder(from)
    private val springAnim = SpringAnimation(valueHolder).apply {
        spring = SpringForce(to).apply {
            this.stiffness = stiffness
            this.dampingRatio = damping
        }
        setStartVelocity(initialVelocity)
    }

    private val endListeners = mutableListOf<() -> Unit>()
    private val updateListeners = mutableListOf<(Float) -> Unit>()
    private var isRunning = false

    init {
        springAnim.addUpdateListener { _, value, _ ->
            updateListeners.forEach { it(value) }
        }
        springAnim.addEndListener { _, _, _, _ ->
            isRunning = false
            endListeners.forEach { it() }
        }
    }

    override fun start() {
        if (isRunning) return
        isRunning = true
        springAnim.start()
    }

    override fun cancel() {
        springAnim.cancel()
        isRunning = false
    }

    override fun isRunning(): Boolean = isRunning

    override fun getValue(): Float = valueHolder.value

    override fun addEndListener(listener: () -> Unit) {
        endListeners.add(listener)
    }

    override fun addUpdateListener(listener: (Float) -> Unit) {
        updateListeners.add(listener)
    }
}




