





Great question. Here's a clear and defensible explanation you can include in your RFC:


---

🔀 Why Is Strategy Switching Required?

In animation systems like Bloks, spring motion serves two distinct UX goals:


---

✅ 1. Physics-Based Spring (Dynamic, natural)

Behaves like a real-world spring — bounces, settles based on stiffness, damping, and initial velocity.

Timing is not fixed — animation duration adapts to the physics.

Ideal for gesture-driven interactions (e.g., swipe to dismiss, drag release, overscroll bounce).

Provides realism and responsiveness.


🧠 Problem: Since it has no fixed duration, it can’t be sequenced predictably with other animations (e.g., in an AnimatorSet).


---

✅ 2. Duration-Based Spring (Deterministic, composable)

Simulates spring-like behavior using a fixed duration and a custom interpolator (or dampingRatio).

Great for predefined UI transitions, like onboarding flows, reveal effects, or chained animations.

Ensures consistent timing and fits into existing animation sequencing models.


🧠 Limitation: It’s not reactive — can’t respond naturally to gesture velocity or dynamic input.


---

🎯 Why We Switch Based on duration

To offer the best of both worlds in a single, unified API:

When duration is provided → we switch to timing-based strategy.

When duration is absent → we use true physical spring motion.


This allows:

Developers to choose style implicitly via config (e.g., duration).

Cross-platform parity with React Native Reanimated and Framer Motion, which follow the same convention.

Smooth integration with existing animation tooling like AnimatorSet, useAnimatedStyle, or Bloks’ sequencing engine.



---

✅ In Summary:

> The strategy switch is intentional and user-friendly: it empowers developers to control animation style using config, without changing APIs — enabling both interactive realism and predictable composition in one design.



Let me know if you'd like this formalized into an RFC paragraph or turned into a diagram.













Certainly — here’s a clear and concise version you can include under a “Proposal” or “Rationale” section of your RFC:


---

✅ Proposal: Two-Strategy Spring Animator

We propose implementing a unified SpringLikeAnimator that internally switches between two strategies based on the configuration:


---

🔀 Why Two Strategies?

✅ To support gesture-driven, physics-accurate animations (using stiffness, damping, velocity).

✅ To support timed, sequenced animations that play well with AnimatorSet-style flows (via duration).

✅ To maintain backward compatibility with existing Animator infrastructure (like Bloks or Jetpack).

✅ To provide developer flexibility in a single API: config drives behavior.

✅ To match behavior of React Native Reanimated v3 and Framer Motion, which switch spring behavior based on duration.



---

🤖 Strategy Switching Logic

If duration is provided → use InterpolatedSpringStrategy
(Backed by ValueAnimator + springy interpolator. Deterministic and composable.)

If duration is not provided → use PhysicsSpringStrategy
(Backed by SpringAnimation. Physics-driven and responsive.)



---

This design offers the flexibility of modern UX while ensuring technical integration with existing animation pipelines. Let me know if you'd like a diagram version of this too.

