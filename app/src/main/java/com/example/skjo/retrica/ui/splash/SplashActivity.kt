package com.example.skjo.retrica.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.skjo.retrica.databinding.ActivitySplashBinding
import com.example.skjo.retrica.ui.BaseActivity
import com.example.skjo.retrica.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    /**
     * main 이동 전 delay 2000ms
     */
    private var handler: Handler? = null

    /**
     * - 모든 필수 권한 허용됨: start main activity
     * - 하나라도 허용되지 않음: 권한 요청 안내 popup 노출
     */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                startMainWithDelay()
            } else {
                showPermissionDeniedDialog(appDetailsSettingsLauncher)
            }
        }

    override fun getViewBinding() = ActivitySplashBinding.inflate(layoutInflater)
    override fun initView() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionsAndProceed()
    }

    /**
     * App Details Settings Launcher
     * - App settings 에서 돌아오면 다시 권한 체크 수행
     */
    private val appDetailsSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkPermissionsAndProceed()
        }

    override fun onPause() {
        super.onPause()
        handler?.removeCallbacksAndMessages(null)
    }

    private fun checkPermissionsAndProceed() {
        if (hasAllPermissions) {
            // 모든 권한이 있다면, 메인 액티비티로 이동합니다.
            startMainWithDelay()
        } else {
            // 권한이 하나라도 없다면, 권한을 요청합니다.
            permissionLauncher.launch(requiredPermissions)
        }
    }

    /**
     * start Main activity
     */
    private fun startMainWithDelay() {
        handler?.removeCallbacksAndMessages(null) // 중복 실행 방지
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            if (!isFinishing) { // 액티비티가 종료되지 않았을 때만 실행
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000)
    }
}