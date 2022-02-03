package com.mugames.todo

import android.view.View
import android.view.animation.Animation

fun View.startAnim(animation: Animation, onAnimationEnd: () -> Unit) {
    animation.setAnimationListener(object : Animation.AnimationListener{
        override fun onAnimationStart(animation: Animation?) = Unit

        override fun onAnimationEnd(animation: Animation?) {
            onAnimationEnd()
        }

        override fun onAnimationRepeat(animation: Animation?) = Unit
    })
    this.startAnimation(animation)
    setOnClickListener {

    }
}
