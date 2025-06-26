package com.samirsarosh.renderthought.util;

/**
 * Created by samirsarosh on 11/12/18.
 */

public class AppUtils {
}


class SpringFloatAnimator(
    startValue: Float,
    private val endValue: Float,
    stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
    damping: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
    private val onUpdate: (Float) -> Unit,
    private val onEnd: (() -> Unit)? = null,
) {
    private val valueHolder = FloatValueHolder(startValue)
    private val spring = SpringAnimation(valueHolder).apply {
        spring = SpringForce(endValue).apply {
            this.stiffness = stiffness
            this.dampingRatio = damping
        }
        addUpdateListener { _, value, _ -> onUpdate(value) }
        addEndListener { _, _, _, _ -> onEnd?.invoke() }
    }

    fun start() = spring.start()
    fun cancel() = spring.cancel()
    fun isRunning(): Boolean = spring.isRunning
}








fun createSpringAnimator(
    from: Float,
    to: Float,
    onUpdate: (Float) -> Unit,
    onEnd: () -> Unit = {}
): Animator {
    val valueHolder = FloatValueHolder(from)
    val springAnim = SpringAnimation(valueHolder).apply {
        spring = SpringForce(to).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
        addUpdateListener { _, value, _ -> onUpdate(value) }
        addEndListener { _, _, _, _ -> onEnd() }
    }

    return object : Animator() {
        override fun start() = springAnim.start()
        override fun cancel() = springAnim.cancel()
        override fun isRunning() = springAnim.isRunning

        override fun addListener(listener: Animator.AnimatorListener?) {
            // Optional — map SpringAnimation events
        }

        override fun removeListener(listener: Animator.AnimatorListener?) {}
    }
}
          
          

