package com.example.skjo.retrica.ui.main

import android.opengl.GLSurfaceView
import android.util.Log
import android.util.Rational
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.skjo.retrica.databinding.ActivityMainBinding
import com.example.skjo.retrica.ui.BaseActivity
import com.example.skjo.retrica.utils.GLRenderer
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var renderer: GLRenderer
    private var cameraProvider: ProcessCameraProvider? = null

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        observeViewModel()

        binding.switchFilter.setOnCheckedChangeListener { _, isChecked ->
            renderer.isFilterEnabled = isChecked
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // GLSurfaceView 및 렌더러 설정
        renderer = GLRenderer(binding.layoutGlSurfaceView)
        // OpenGL ES 3.0 사용을 권장합니다.
        binding.layoutGlSurfaceView.setEGLContextClientVersion(3)
        binding.layoutGlSurfaceView.setRenderer(renderer)
        binding.layoutGlSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        setupCamera()
    }

    private fun observeViewModel() {
        viewModel.data.observe(this) {
            binding.tvFps.text = it
        }
        viewModel.fetchData()
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // cameraProvider 인스턴스를 멤버 변수에 저장해 둡니다.
            cameraProvider = cameraProviderFuture.get()
            // 뷰가 완전히 그려진 후에 첫 바인딩을 시작합니다.
            binding.layoutGlSurfaceView.post {
                bindPreview()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun bindPreview() {
        // cameraProvider가 초기화되지 않았으면 아무것도 하지 않습니다.
        val provider = cameraProvider ?: return

        val preview: Preview = Preview.Builder().build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val viewPort = ViewPort.Builder(
            Rational(binding.layoutGlSurfaceView.width, binding.layoutGlSurfaceView.height),
            binding.layoutGlSurfaceView.display.rotation
        ).build()

        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .setViewPort(viewPort)
            .build()

        preview.setSurfaceProvider(renderer.surfaceProvider)

        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, useCaseGroup)
        } catch (e: Exception) {
            Log.e("MainActivity", "UseCase binding failed", e)
        }
    }

    // <<<<< 핵심 수정 부분 >>>>>
    override fun onResume() {
        super.onResume()
        binding.layoutGlSurfaceView.onResume()
        // cameraProvider가 초기화 된 이후, 즉 한 번이라도 카메라가 연결된 적이 있다면,
        // onResume 시점에 다시 바인딩을 해줍니다.
        if (cameraProvider != null) {
            binding.layoutGlSurfaceView.post { // 뷰가 준비된 후 실행
                bindPreview()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.layoutGlSurfaceView.onPause()
        // onPause에서는 unbind를 명시적으로 호출하지 않습니다.
        // bindToLifecycle이 생명주기를 관리하므로, onPause 시 카메라 리소스는 자동으로 해제됩니다.
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
