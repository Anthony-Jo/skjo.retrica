package com.example.skjo.retrica.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.example.skjo.retrica.ui.splash.SplashActivity

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: B
    private var permissionDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTransparentStatusBar()

        binding = getViewBinding()
        setContentView(binding.root)

        setStatusBarPadding()

        initView()
    }

    abstract fun getViewBinding(): B

    open fun initView() = Unit

    /**
     * 상태 표시줄을 투명하게 만들고, 콘텐츠를 상태 표시줄 뒤로 확장 (Edge-to-Edge)
     */
    private fun setTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * 시스템 UI(상태 표시줄 등)가 차지하는 영역을 가져와, 해당 영역만큼 패딩 적용
     */
    private fun setStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = systemBars.left, top = systemBars.top, right = systemBars.right, bottom = systemBars.bottom)
            insets
        }
    }

    /**
     * 필수 권한 허용 필요 안내 dialog 노출
     * - 중복 노출 방지
     * - positiveButton : App settings 이동
     * - negativeButton : 앱 종료
     * - launcher: App settings 이동을 위한 launcher / result callback
     */
    fun showPermissionDeniedDialog(launcher: ActivityResultLauncher<Intent>? = null) {
        if (permissionDialog?.isShowing == true) {
            return
        }
        permissionDialog = AlertDialog.Builder(this)
            .setTitle("권한 필요")
            .setMessage("앱을 사용하기 위해 카메라와 저장소 접근 권한이 필요합니다. 설정으로 이동하여 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                startAppDetailSettings(launcher)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * start app detail settings
     * - ActivityResultLauncher 가 null 이면 startActivity()
     */
    private fun startAppDetailSettings(launcher: ActivityResultLauncher<Intent>? = null) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        launcher?.launch(intent) ?: run { startActivity(intent) }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.CAMERA)
    } else {
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    val hasAllPermissions: Boolean
        get() = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun checkPermission() {
        if (this is SplashActivity) {
            return
        }
        if (!hasAllPermissions) {
            showPermissionDeniedDialog()
        }
    }
}
