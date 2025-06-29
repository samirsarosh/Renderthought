package com.samirsarosh.renderthought.util;

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




