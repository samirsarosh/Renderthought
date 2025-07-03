
// Shared abstraction for spring-based animations

interface SpringAnimatable { fun getAnimatedValue(): Float fun isRunning(): Boolean fun cancel() fun setUpdateListener(callback: (Float) -> Unit) }

// --- InterpolatedSpringAnimator.kt ---

class InterpolatedSpringAnimator(private val config: SpringConfig) : ValueAnimator(), SpringAnimatable {

private var updateCallback: ((Float) -> Unit)? = null

init {
    setFloatValues(config.from, config.to)
    duration = config.duration ?: 300L
    startDelay = config.startDelay
    interpolator = PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f)

    addUpdateListener {
        val value = it.animatedValue as Float
        updateCallback?.invoke(value)
    }
}

override fun getAnimatedValue(): Float = super.getAnimatedValue() as Float
override fun isRunning(): Boolean = super.isRunning
override fun cancel() = super.cancel()
override fun setUpdateListener(callback: (Float) -> Unit) {
    updateCallback = callback
}

}

// --- PhysicsSpringAnimator.kt ---

class PhysicsSpringAnimator(private val config: SpringConfig) : Animator(), SpringAnimatable {

private val valueHolder = FloatValueHolder(config.from)
private val springAnimation = SpringAnimation(valueHolder)
private var updateCallback: ((Float) -> Unit)? = null
private var startAction: (() -> Unit)? = null

init {
    springAnimation.spring = SpringForce(config.to).apply {
        stiffness = config.stiffness ?: SpringForce.STIFFNESS_MEDIUM
        dampingRatio = config.dampingRatio ?: SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    }
    springAnimation.setStartVelocity(config.initialVelocity ?: 0f)

    springAnimation.addUpdateListener { _, value, _ ->
        updateCallback?.invoke(value)
    }
}

override fun start() {
    startAction = { springAnimation.start() }
    if (config.startDelay > 0) {
        Choreographer.getInstance().postFrameCallbackDelayed(
            frameCallback, config.startDelay
        )
    } else {
        startAction?.invoke()
    }
}

private val frameCallback = Choreographer.FrameCallback {
    startAction?.invoke()
    startAction = null
}

override fun cancel() {
    Choreographer.getInstance().removeFrameCallback(frameCallback)
    springAnimation.cancel()
    startAction = null
}

override fun isRunning(): Boolean = springAnimation.isRunning || startAction != null
override fun getAnimatedValue(): Float = valueHolder.value
override fun setUpdateListener(callback: (Float) -> Unit) {
    this.updateCallback = callback
}

override fun addListener(listener: Animator.AnimatorListener) {}
override fun setInterpolator(interpolator: TimeInterpolator?) {}
override fun getStartDelay(): Long = config.startDelay
override fun setStartDelay(startDelay: Long) {}
override fun getDuration(): Long = 0L
override fun setDuration(duration: Long): Animator = this

}

// Usage (in client code) fun useAnimation(animator: Animator) { (animator as? SpringAnimatable)?.setUpdateListener { value -> // Do something with animated value }

animator.start()

}
















// ---------- SpringConfig.kt

data class SpringConfig( val from: Float, val to: Float, val duration: Long? = null, val dampingRatio: Float? = null, val stiffness: Float? = null, val initialVelocity: Float? = null, val startDelay: Long = 0L )

// ---------- SpringStrategy.kt

interface SpringStrategy { fun start() fun cancel() fun isRunning(): Boolean fun getAnimatedValue(): Float fun setUpdateListener(callback: (Float) -> Unit) fun addListener(listener: Animator.AnimatorListener) }

// ---------- InterpolatedSpringStrategy.kt

class InterpolatedSpringStrategy(private val config: SpringConfig) : SpringStrategy {

private val animator: ValueAnimator = ValueAnimator.ofFloat(config.from, config.to).apply {
    duration = config.duration ?: 300L
    startDelay = config.startDelay
    interpolator = PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f)
}

private var animatedValue: Float = config.from
private var updateCallback: ((Float) -> Unit)? = null

init {
    animator.addUpdateListener {
        animatedValue = it.animatedValue as Float
        updateCallback?.invoke(animatedValue)
    }
}

override fun start() = animator.start()
override fun cancel() = animator.cancel()
override fun isRunning(): Boolean = animator.isRunning
override fun getAnimatedValue(): Float = animatedValue
override fun setUpdateListener(callback: (Float) -> Unit) { updateCallback = callback }
override fun addListener(listener: Animator.AnimatorListener) { animator.addListener(listener) }

}

// ---------- PhysicsSpringStrategy.kt

class PhysicsSpringStrategy(private val config: SpringConfig) : SpringStrategy {

private val valueHolder = FloatValueHolder(config.from)
private val springAnimation = SpringAnimation(valueHolder)
private var updateCallback: ((Float) -> Unit)? = null
private var startAction: (() -> Unit)? = null

init {
    springAnimation.spring = SpringForce(config.to).apply {
        stiffness = config.stiffness ?: SpringForce.STIFFNESS_MEDIUM
        dampingRatio = config.dampingRatio ?: SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    }
    springAnimation.setStartVelocity(config.initialVelocity ?: 0f)

    springAnimation.addUpdateListener { _, value, _ ->
        updateCallback?.invoke(value)
    }
}

override fun start() {
    startAction = { springAnimation.start() }

    if (config.startDelay > 0) {
        Choreographer.getInstance().postFrameCallbackDelayed(
            frameCallback, config.startDelay
        )
    } else {
        startAction?.invoke()
    }
}

private val frameCallback = Choreographer.FrameCallback {
    startAction?.invoke()
    startAction = null
}

override fun cancel() {
    Choreographer.getInstance().removeFrameCallback(frameCallback)
    springAnimation.cancel()
    startAction = null
}

override fun isRunning(): Boolean = springAnimation.isRunning || startAction != null
override fun getAnimatedValue(): Float = valueHolder.value
override fun setUpdateListener(callback: (Float) -> Unit) { this.updateCallback = callback }
override fun addListener(listener: Animator.AnimatorListener) { /* Not supported natively */ }

}

// ---------- SpringLikeAnimator.kt

class SpringLikeAnimator(private val strategy: SpringStrategy) : Animator() {

override fun start() = strategy.start()
override fun cancel() = strategy.cancel()
override fun isRunning(): Boolean = strategy.isRunning()

fun getAnimatedValue(): Float = strategy.getAnimatedValue()
fun addUpdateListener(callback: (Float) -> Unit) = strategy.setUpdateListener(callback)

override fun addListener(listener: Animator.AnimatorListener) = strategy.addListener(listener)
override fun setInterpolator(interpolator: TimeInterpolator?) {}
override fun getStartDelay(): Long = 0L
override fun setStartDelay(startDelay: Long) {}
override fun getDuration(): Long = 0L
override fun setDuration(duration: Long) {}

}

// ---------- SpringAnimatorFactory.kt

fun createSpringAnimator(config: SpringConfig): SpringLikeAnimator { val strategy = if (config.duration != null) { InterpolatedSpringStrategy(config) } else { PhysicsSpringStrategy(config) } return SpringLikeAnimator(strategy) }

// ---------- Usage.kt

fun useSpring(view: View) { val animator = createSpringAnimator( SpringConfig( from = 0f, to = 300f, duration = 500L, // Remove to switch to physics dampingRatio = 0.6f, stiffness = 500f, startDelay = 150L ) )

animator.addUpdateListener { value ->
    view.translationX = value
}

animator.addListener(object : Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator?) {}
    override fun onAnimationEnd(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
    override fun onAnimationRepeat(animation: Animator?) {}
})

animator.start()

}

