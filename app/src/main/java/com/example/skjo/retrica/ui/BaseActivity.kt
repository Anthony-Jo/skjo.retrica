package com.example.skjo.retrica.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 상태 표시줄을 투명하게 만들고, 콘텐츠를 상태 표시줄 뒤로 확장 (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        binding = getViewBinding()
        setContentView(binding.root)

        // 2. 시스템 UI(상태 표시줄 등)가 차지하는 영역을 가져와, 해당 영역만큼 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = systemBars.left, top = systemBars.top, right = systemBars.right, bottom = systemBars.bottom)
            insets
        }

        init()
    }

    abstract fun getViewBinding(): B

    open fun init() {
        // 하위 액티비티에서 필요에 따라 재정의하여 사용
    }
}
