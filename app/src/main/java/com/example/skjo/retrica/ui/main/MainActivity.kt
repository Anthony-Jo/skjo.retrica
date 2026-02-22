package com.example.skjo.retrica.ui.main

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.skjo.retrica.databinding.ActivityMainBinding
import com.example.skjo.retrica.model.CameraType
import com.example.skjo.retrica.ui.BaseActivity
import com.example.skjo.retrica.ui.main.filter.FilterAdapter
import com.example.skjo.retrica.utils.GLRenderer
import com.example.skjo.retrica.utils.IFilter
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(), GLRenderer.PerformanceMonitor {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var filter: IFilter
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacingType: CameraType = CameraType.Back

    private lateinit var filterAdapter: FilterAdapter

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
        cameraExecutor = Executors.newSingleThreadExecutor()

        // GLSurfaceView 및 필터(렌더러) 설정
        val renderer = GLRenderer(binding.layoutGlSurfaceView)
        renderer.setPerformanceMonitor(this)
        filter = renderer
        binding.layoutGlSurfaceView.apply {
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
    }

    override fun initView() {
        super.initView()
        binding.btnChangeCamera.setOnClickListener {
            it.isEnabled = false
            // 현재 렌즈 방향을 반대로 바꿉니다.
            lensFacingType = if (lensFacingType.value == CameraSelector.LENS_FACING_BACK) {
                CameraType.Front
            } else {
                CameraType.Back
            }
            viewModel.saveLastCamera(lensFacingType)
            cameraExecutor.execute {
                bindPreview()
                runOnUiThread {
                    it.isEnabled = true
                }
            }
        }

        setupFilterList()
    }

    override fun onFpsUpdated(fps: Double) {
        viewModel.updateFps(fps)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isInitialized.collect { isInitialized ->
                if (isInitialized) {
                    if (cameraProvider == null) { // 카메라가 아직 설정되지 않았을 때만 호출
                        setupCamera()
                    }
                }
            }
        }

        viewModel.apply {
            fps.observe(this@MainActivity) {
                binding.tvFps.text = it
            }

            currentFilter.observe(this@MainActivity) {
                binding.tvCurrentFilter.text = it
            }

            lastUsedCamera.observe(this@MainActivity) {
                lensFacingType = CameraType.toLensFacing(it)
            }

            lastSelectedFilter.observe(this@MainActivity) { lastFilter ->
                binding.rvFilters.post {
                    val layoutManager = binding.rvFilters.layoutManager as LinearLayoutManager
                    val currentPosition = layoutManager.findFirstVisibleItemPosition()
                    if (currentPosition == RecyclerView.NO_POSITION) return@post

                    val targetPosition = filterAdapter.findClosestPosition(currentPosition, lastFilter)

                    val smoothScroller = object : LinearSmoothScroller(this@MainActivity) {
                        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                            val dxToStart = super.calculateDxToMakeVisible(view, -1)
                            val dxToEnd = super.calculateDxToMakeVisible(view, 1)
                            return (dxToStart + dxToEnd) / 2
                        }
                    }
                    smoothScroller.targetPosition = targetPosition
                    layoutManager.startSmoothScroll(smoothScroller)
                }
            }
        }
    }

    private fun setupFilterList() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilters.layoutManager = layoutManager

        filterAdapter = FilterAdapter(this) { clickedView ->
            val clickedPosition = layoutManager.getPosition(clickedView)
            if (clickedPosition == RecyclerView.NO_POSITION) return@FilterAdapter

            val smoothScroller = object : LinearSmoothScroller(this) {
                override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                    val dxToStart = super.calculateDxToMakeVisible(view, -1)
                    val dxToEnd = super.calculateDxToMakeVisible(view, 1)
                    return (dxToStart + dxToEnd) / 2
                }
            }
            smoothScroller.targetPosition = clickedPosition
            layoutManager.startSmoothScroll(smoothScroller)
        }
        binding.rvFilters.adapter = filterAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvFilters)

        val startPosition = Integer.MAX_VALUE / 2
        layoutManager.scrollToPosition(startPosition)

        binding.rvFilters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager.getPosition(centerView)
                    val selectedFilter = filterAdapter.getFilterItemAt(position)
                    filter.setFilter(selectedFilter.type)

                    // 4. 필터가 변경될 때마다 ViewModel을 통해 저장합니다.
                    viewModel.saveLastFilter(selectedFilter.type)
                }
            }
        })
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
            .requireLensFacing(lensFacingType.value)
            .build()

        runOnUiThread {
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
                /**
                 * Preview UseCase는 렌더링할 Surface를 요청합니다(SurfaceRequest).
                 */
                provider.bindToLifecycle(this, cameraSelector, useCaseGroup)
            } catch (e: Exception) {
                Log.e("MainActivity", "UseCase binding failed", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.layoutGlSurfaceView.onResume()
        if (cameraProvider != null) {
            cameraExecutor.execute {
                bindPreview()
            }
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
