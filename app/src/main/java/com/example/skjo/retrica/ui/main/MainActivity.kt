package com.example.skjo.retrica.ui.main

import android.opengl.GLSurfaceView
import android.util.Size
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
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

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        viewModel.data.observe(this) {
            binding.tvFps.text = it
        }
        viewModel.fetchData()

        binding.switchFilter.setOnCheckedChangeListener { _, isChecked ->
            renderer.isFilterEnabled = isChecked
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

        // GLSurfaceView 및 렌더러 설정
        renderer = GLRenderer(binding.layoutGlSurfaceView)
        binding.layoutGlSurfaceView.setEGLContextClientVersion(2)
        binding.layoutGlSurfaceView.setRenderer(renderer)
        binding.layoutGlSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .setTargetResolution(Size(1080, 1920))
            .setTargetRotation(binding.layoutGlSurfaceView.display.rotation)
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(renderer.surfaceProvider)

        cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
