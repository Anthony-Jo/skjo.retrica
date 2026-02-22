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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skjo.retrica.R
import com.example.skjo.retrica.databinding.ActivityMainBinding
import com.example.skjo.retrica.model.FilterInfo
import com.example.skjo.retrica.model.FilterType
import com.example.skjo.retrica.ui.BaseActivity
import com.example.skjo.retrica.utils.GLRenderer
import com.example.skjo.retrica.utils.IFilter
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var filter: IFilter
    private var cameraProvider: ProcessCameraProvider? = null

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        observeViewModel()
        setupFilterList()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // GLSurfaceView 및 필터(렌더러) 설정
        val renderer = GLRenderer(binding.layoutGlSurfaceView)
        filter = renderer
        binding.layoutGlSurfaceView.setEGLContextClientVersion(3) // OpenGL ES 3.0 사용
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

    private fun setupFilterList() {
        val filters = listOf(
            FilterInfo("None", FilterType.NONE, R.mipmap.ic_launcher),
            FilterInfo("Grayscale", FilterType.GRAYSCALE, R.mipmap.ic_launcher),
            FilterInfo("Sepia", FilterType.SEPIA, R.mipmap.ic_launcher),
            FilterInfo("Invert", FilterType.INVERT, R.mipmap.ic_launcher),
            FilterInfo("Vignette", FilterType.VIGNETTE, R.mipmap.ic_launcher),
            FilterInfo("Posterize", FilterType.POSTERIZE, R.mipmap.ic_launcher)
        )

        val adapter = FilterAdapter(filters) { selectedFilter ->
            (filter as? GLRenderer)?.setFilter(selectedFilter.type)
        }

        binding.rvFilters.adapter = adapter
        binding.rvFilters.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            binding.layoutGlSurfaceView.post { bindPreview() }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview() {
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

        preview.setSurfaceProvider(filter.surfaceProvider)

        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, useCaseGroup)
        } catch (e: Exception) {
            Log.e("MainActivity", "UseCase binding failed", e)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.layoutGlSurfaceView.onResume()
        if (cameraProvider != null) {
            binding.layoutGlSurfaceView.post { bindPreview() }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.layoutGlSurfaceView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        filter.release()
    }
}
