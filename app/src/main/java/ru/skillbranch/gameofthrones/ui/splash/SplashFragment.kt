package ru.skillbranch.gameofthrones.ui.splash

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_splash.*
import ru.skillbranch.gameofthrones.R

class SplashFragment : Fragment() {
    private lateinit var animation : AnimatorSet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_splash, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var pulseAnimator = ValueAnimator.ofFloat(1f, 1.2f, 1f).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            duration = 1000
            addUpdateListener {
                val zoom = it.animatedValue as Float
                splash_image.apply {
                    scaleX = zoom
                    scaleY = zoom
                }
            }
        }

        var tintAnimator = ValueAnimator.ofArgb(
            Color.BLACK,
            Color.WHITE,
            Color.BLACK,
            Color.RED,
            Color.BLACK,
            Color.MAGENTA,
            Color.BLACK,
            Color.YELLOW,
            Color.BLACK,
            Color.CYAN,
            Color.BLACK
        ).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            duration = 5000
            addUpdateListener {
                val color = it.animatedValue as Int
                splash_image.imageTintList = ColorStateList.valueOf(color)
            }
        }

        animation = AnimatorSet().apply {
            playTogether(tintAnimator, pulseAnimator)
            duration = 10000
            start()
        }
    }

    override fun onDestroyView() {
        animation.cancel()

        super.onDestroyView()
    }
}