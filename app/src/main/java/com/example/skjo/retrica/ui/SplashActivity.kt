package com.example.skjo.retrica.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.skjo.retrica.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private var handler: Handler? = null
    private var isResumedAfterPermissionRequest = false

    private val PERMISSIONS_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.CAMERA)
    } else {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun getViewBinding() = ActivitySplashBinding.inflate(layoutInflater)

    override fun init() {
        // 초기화 로직은 onResume에서 처리
    }

    override fun onResume() {
        super.onResume()
        // 설정 화면에서 돌아오거나, 권한 요청 후 onResume이 호출될 때만 로직 실행
        if (isResumedAfterPermissionRequest) {
            checkPermissionsAndProceed()
        } else {
             checkPermissionsAndProceed()
        }
    }

    override fun onPause() {
        super.onPause()
        handler?.removeCallbacksAndMessages(null)
        isResumedAfterPermissionRequest = false
    }

    private fun checkPermissionsAndProceed() {
        if (hasPermissions()) {
            startMainWithDelay()
        } else {
             if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    private fun hasPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun startMainWithDelay() {
        handler?.removeCallbacksAndMessages(null) // 중복 실행 방지
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isResumedAfterPermissionRequest = true
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (!(grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                 showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 필요")
            .setMessage("앱을 사용하기 위해 카메라와 저장소 접근 권한이 필요합니다. 설정으로 이동하여 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
