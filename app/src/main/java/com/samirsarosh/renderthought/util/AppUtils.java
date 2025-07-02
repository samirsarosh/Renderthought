
    import android.animation.Animator import android.animation.ValueAnimator import android.view.Choreographer import android.view.animation.Interpolator import androidx.dynamicanimation.animation.SpringAnimation import androidx.dynamicanimation.animation.SpringForce import androidx.dynamicanimation.animation.FloatValueHolder import java.lang.ref.WeakReference

// --- Strategy interface --- interface SpringStrategy { fun start() fun cancel() fun isRunning(): Boolean fun getAnimatedValue(): Float fun addListener(listener: Animator.AnimatorListener) fun removeListener(listener: Animator.AnimatorListener) }

// --- Physics-based spring strategy --- class PhysicsSpringStrategy( private val from: Float, private val to: Float, private val dampingRatio: Float, private val stiffness: Float, private val initialVelocity: Float, private val startDelay: Long, private val onUpdate: ((Animator, Float) -> Unit)?, private val owner: WeakReference<Animator> ) : SpringStrategy {

private val valueHolder = FloatValueHolder(from)
private val springAnim = SpringAnimation(valueHolder)
private val listeners = mutableListOf<Animator.AnimatorListener>()
private var isRunning = false
private var animatedValue: Float = from

init {
    springAnim.spring = SpringForce(to).apply {
        this.dampingRatio = this@PhysicsSpringStrategy.dampingRatio
        this.stiffness = this@PhysicsSpringStrategy.stiffness
    }
    springAnim.setStartVelocity(initialVelocity)
    springAnim.addUpdateListener { _, value, _ ->
        animatedValue = value
        onUpdate?.invoke(owner.get() ?: springAnim, value)
    }
    springAnim.addEndListener { _, _, _, _ ->
        isRunning = false
        listeners.forEach { it.onAnimationEnd(owner.get() ?: springAnim) }
    }
}

override fun start() {
    if (isRunning) return
    val startAction = {
        valueHolder.value = from
        springAnim.spring.finalPosition = to
        springAnim.cancel()
        isRunning = true
        listeners.forEach { it.onAnimationStart(owner.get() ?: springAnim) }
        springAnim.start()
    }
    if (startDelay > 0) postFrameDelay(startDelay, startAction) else startAction()
}

override fun cancel() {
    if (isRunning) {
        springAnim.cancel()
        isRunning = false
        listeners.forEach { it.onAnimationCancel(owner.get() ?: springAnim) }
        listeners.forEach { it.onAnimationEnd(owner.get() ?: springAnim) }
    }
}

override fun isRunning() = isRunning
override fun getAnimatedValue(): Float = animatedValue
override fun addListener(listener: Animator.AnimatorListener) { listeners.add(listener) }
override fun removeListener(listener: Animator.AnimatorListener) { listeners.remove(listener) }

private fun postFrameDelay(delayMs: Long, block: () -> Unit) {
    val start = System.nanoTime()
    fun loop() {
        val elapsed = (System.nanoTime() - start) / 1_000_000
        if (elapsed >= delayMs) block() else Choreographer.getInstance().postFrameCallback { loop() }
    }
    loop()
}

}

// --- Interpolated spring strategy --- class InterpolatedSpringStrategy( private val from: Float, private val to: Float, private val duration: Long, private val startDelay: Long, private val dampingRatio: Float, private val stiffness: Float, private val onUpdate: ((Animator, Float) -> Unit)?, private val owner: WeakReference<Animator> ) : SpringStrategy {

private var animator: ValueAnimator? = null
private val listeners = mutableListOf<Animator.AnimatorListener>()
private var animatedValue: Float = from
private var isRunning = false

override fun start() {
    if (isRunning) return
    animator = ValueAnimator.ofFloat(from, to).apply {
        interpolator = SpringInterpolator(dampingRatio, stiffness)
        this.duration = this@InterpolatedSpringStrategy.duration
        this.startDelay = this@InterpolatedSpringStrategy.startDelay
        addUpdateListener {
            animatedValue = it.animatedValue as Float
            onUpdate?.invoke(owner.get() ?: this, animatedValue)
        }
        listeners.forEach { addListener(it) }
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) { isRunning = true }
            override fun onAnimationEnd(animation: Animator) { isRunning = false }
            override fun onAnimationCancel(animation: Animator) { isRunning = false }
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
    animator?.start()
}

override fun cancel() { animator?.cancel() }
override fun isRunning(): Boolean = isRunning
override fun getAnimatedValue(): Float = animatedValue
override fun addListener(listener: Animator.AnimatorListener) { listeners.add(listener) }
override fun removeListener(listener: Animator.AnimatorListener) { listeners.remove(listener) }

}

// --- Spring interpolator (approximation) --- class SpringInterpolator( private val dampingRatio: Float, private val stiffness: Float ) : Interpolator { override fun getInterpolation(t: Float): Float { val overshoot = 1.0f + (1.0f - dampingRatio.coerceIn(0f, 1f)) return (t - 1).let { it * it * ((overshoot + 1) * it + overshoot) + 1 } } }

// --- Hybrid animator class --- class SpringLikeAnimator( private val from: Float, private val to: Float, private val dampingRatio: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY, private val stiffness: Float = SpringForce.STIFFNESS_MEDIUM, private val initialVelocity: Float = 0f, private var duration: Long? = null, private var startDelay: Long = 0L, private val onUpdate: ((Animator, Float) -> Unit)? = null ) : Animator() {

private val selfRef = WeakReference<Animator>(this)
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

override fun start() {
    strategy = createStrategy()
    listeners.forEach { strategy?.addListener(it) }
    strategy?.start()
}

private fun createStrategy(): SpringStrategy {
    return if (duration != null && duration != -1L) {
        InterpolatedSpringStrategy(from, to, duration!!, startDelay, dampingRatio, stiffness, { anim, value ->
            lastAnimatedValue = value
            onUpdate?.invoke(anim, value)
        }, selfRef)
    } else {
        PhysicsSpringStrategy(from, to, dampingRatio, stiffness, initialVelocity, startDelay, { anim, value ->
            lastAnimatedValue = value
            onUpdate?.invoke(anim, value)
        }, selfRef)
    }
}

override fun cancel() { strategy?.cancel() }
override fun end() { strategy?.cancel() }
override fun isRunning(): Boolean = strategy?.isRunning() ?: false
fun getAnimatedValue(): Float = strategy?.getAnimatedValue() ?: lastAnimatedValue

}



    
