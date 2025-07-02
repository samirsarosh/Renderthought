import android.animation.Animator import android.animation.TimeInterpolator import android.animation.ValueAnimator import android.view.Choreographer import android.view.animation.Interpolator import androidx.dynamicanimation.animation.FloatValueHolder import androidx.dynamicanimation.animation.SpringAnimation import androidx.dynamicanimation.animation.SpringForce

// --- Strategy interface --- interface SpringStrategy { fun start() fun cancel() fun isRunning(): Boolean fun getAnimatedValue(): Float }

// --- Physics-based spring strategy --- class PhysicsSpringStrategy( private val from: Float, private val to: Float, dampingRatio: Float, stiffness: Float, private val initialVelocity: Float, private val startDelayMs: Long, private val onUpdate: (() -> Unit)? = null, private val onStart: (() -> Unit)? = null, private val onEnd: (() -> Unit)? = null, private val onCancel: (() -> Unit)? = null ) : SpringStrategy {

private val valueHolder = FloatValueHolder(from)
private val springAnim = SpringAnimation(valueHolder)
private var isRunning = false
private var animatedValue: Float = from

init {
    springAnim.setStartVelocity(initialVelocity)
    springAnim.spring = SpringForce().apply {
        finalPosition = to
        this.dampingRatio = dampingRatio
        this.stiffness = stiffness
    }
    springAnim.addUpdateListener { _, value, _ ->
        animatedValue = value
        onUpdate?.invoke()
    }
    springAnim.addEndListener { _, _, _, _ ->
        isRunning = false
        onEnd?.invoke()
    }
}

override fun start() {
    if (isRunning) return
    val startAction = {
        valueHolder.value = from
        isRunning = true
        onStart?.invoke()
        springAnim.start()
    }
    if (startDelayMs > 0) postFrameDelay(startDelayMs, startAction) else startAction()
}

override fun cancel() {
    if (isRunning) {
        springAnim.cancel()
        isRunning = false
        onCancel?.invoke()
        onEnd?.invoke()
    }
}

override fun isRunning(): Boolean = isRunning
override fun getAnimatedValue(): Float = animatedValue

private fun postFrameDelay(delayMs: Long, block: () -> Unit) {
    val start = System.nanoTime()
    fun loop() {
        val elapsed = (System.nanoTime() - start) / 1_000_000
        if (elapsed >= delayMs) block() else Choreographer.getInstance().postFrameCallback { loop() }
    }
    loop()
}

}

// --- Interpolated spring strategy --- class InterpolatedSpringStrategy( private val from: Float, private val to: Float, private val durationMs: Long, private val startDelayMs: Long, private val dampingRatio: Float, private val stiffness: Float, private val onUpdate: (() -> Unit)? = null, private val onStart: (() -> Unit)? = null, private val onEnd: (() -> Unit)? = null, private val onCancel: (() -> Unit)? = null ) : SpringStrategy {

private var animator: ValueAnimator? = null
private var animatedValue: Float = from
private var isRunning = false

override fun start() {
    if (isRunning) return
    animator = ValueAnimator.ofFloat(from, to).apply {
        interpolator = SpringInterpolator(dampingRatio, stiffness)
        duration = durationMs
        startDelay = startDelayMs
        addUpdateListener {
            animatedValue = it.animatedValue as Float
            onUpdate?.invoke()
        }
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isRunning = true
                onStart?.invoke()
            }
            override fun onAnimationEnd(animation: Animator) {
                isRunning = false
                onEnd?.invoke()
            }
            override fun onAnimationCancel(animation: Animator) {
                isRunning = false
                onCancel?.invoke()
                onEnd?.invoke()
            }
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
    animator?.start()
}

override fun cancel() {
    animator?.cancel()
}

override fun isRunning(): Boolean = isRunning
override fun getAnimatedValue(): Float = animatedValue

}

// --- Spring interpolator (approximation) --- class SpringInterpolator( private val dampingRatio: Float, private val stiffness: Float ) : Interpolator { override fun getInterpolation(t: Float): Float { val overshoot = 1.0f + (1.0f - dampingRatio.coerceIn(0f, 1f)) return (t - 1).let { it * it * ((overshoot + 1) * it + overshoot) + 1 } } }

// --- Hybrid animator class --- class SpringLikeAnimator( private val from: Float, private val to: Float, private val dampingRatio: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY, private val stiffness: Float = SpringForce.STIFFNESS_MEDIUM, private val initialVelocity: Float = 0f, private var duration: Long? = null, private var startDelay: Long = 0L, private val onUpdate: ((Animator) -> Unit)? = null ) : Animator() {

private var strategy: SpringStrategy? = null
private val listeners = mutableListOf<Animator.AnimatorListener>()
private var lastAnimatedValue: Float = from

override fun setDuration(duration: Long): Animator {
    this.duration = duration
    return this
}

override fun getDuration(): Long = duration ?: -1L
override fun setStartDelay(startDelay: Long) { this.startDelay = startDelay }
override fun getStartDelay(): Long = startDelay
override fun addListener(listener: Animator.AnimatorListener?) { listener?.let { listeners.add(it) } }
override fun removeListener(listener: Animator.AnimatorListener?) { listeners.remove(listener) }
override fun setInterpolator(interpolator: TimeInterpolator?) {} // no-op
override fun getInterpolator(): TimeInterpolator? = null // no-op

override fun start() {
    strategy = createStrategy()
    strategy?.start()
    listeners.forEach { it.onAnimationStart(this) }
}

private fun createStrategy(): SpringStrategy {
    val callbacks = Triple(
        { listeners.forEach { it.onAnimationEnd(this) } },
        { listeners.forEach { it.onAnimationCancel(this) } },
        { onUpdate?.invoke(this) }
    )

    return if (duration != null && duration != -1L) {
        InterpolatedSpringStrategy(from, to, duration!!, startDelay, dampingRatio, stiffness,
            onUpdate = callbacks.third,
            onStart = {},
            onEnd = callbacks.first,
            onCancel = callbacks.second
        )
    } else {
        PhysicsSpringStrategy(from, to, dampingRatio, stiffness, initialVelocity, startDelay,
            onUpdate = callbacks.third,
            onStart = {},
            onEnd = callbacks.first,
            onCancel = callbacks.second
        )
    }
}

override fun cancel() {
    strategy?.cancel()
    listeners.forEach { it.onAnimationCancel(this) }
    listeners.forEach { it.onAnimationEnd(this) }
}

override fun end() {
    strategy?.cancel()
    listeners.forEach { it.onAnimationEnd(this) }
}

override fun isRunning(): Boolean = strategy?.isRunning() ?: false
fun getAnimatedValue(): Float = strategy?.getAnimatedValue() ?: lastAnimatedValue

}

