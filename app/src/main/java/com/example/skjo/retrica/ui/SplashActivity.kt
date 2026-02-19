package com.example.skjo.retrica.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.skjo.retrica.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    /**
     * main 이동 전 delay 2000ms
     */
    private var handler: Handler? = null

    /**
     * 필수 권한 list
     */
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.CAMERA)
    } else {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                // 모든 권한이 승인됨
                startMainWithDelay()
            } else {
                // 권한이 하나라도 거부됨
                showPermissionDeniedDialog()
            }
        }

    override fun getViewBinding() = ActivitySplashBinding.inflate(layoutInflater)
    override fun initView() = Unit

    override fun onResume() {
        super.onResume()
        checkPermissionsAndProceed()
    }

    override fun onPause() {
        super.onPause()
        handler?.removeCallbacksAndMessages(null)
    }

    private fun checkPermissionsAndProceed() {
        if (hasAllPermissions()) {
            // 모든 권한이 있다면, 메인 액티비티로 이동합니다.
            startMainWithDelay()
        } else {
            // 권한이 하나라도 없다면, 권한을 요청합니다.
            permissionLauncher.launch(requiredPermissions)
        }
    }

    /**
     * 필수 권한 여부 check
     */
    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
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
