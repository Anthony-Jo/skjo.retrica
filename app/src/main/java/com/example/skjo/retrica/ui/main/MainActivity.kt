package com.example.skjo.retrica.ui.main

import android.opengl.GLSurfaceView
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.skjo.retrica.databinding.ActivityMainBinding
import com.example.skjo.retrica.ui.BaseActivity
import com.example.skjo.retrica.ui.main.filter.FilterAdapter
import com.example.skjo.retrica.utils.GLRenderer
import com.example.skjo.retrica.utils.IFilter
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(), GLRenderer.PerformanceMonitor {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var filter: IFilter
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var filterAdapter: FilterAdapter

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        cameraExecutor = Executors.newSingleThreadExecutor()

        // GLSurfaceView 및 필터(렌더러) 설정
        val renderer = GLRenderer(binding.layoutGlSurfaceView)
        renderer.setPerformanceMonitor(this)
        filter = renderer
        binding.layoutGlSurfaceView.setEGLContextClientVersion(3) // OpenGL ES 3.0 사용
        binding.layoutGlSurfaceView.setRenderer(renderer)
        binding.layoutGlSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // 어댑터와 UI를 먼저 설정합니다.
        setupFilterList()
        // UI가 준비된 후 ViewModel의 데이터를 구독합니다.
        observeViewModel()
        // 마지막으로 카메라를 설정합니다.
        setupCamera()
    }

    override fun onFpsUpdated(fps: Double) {
        viewModel.updateFps(fps)
    }

    private fun observeViewModel() {
        viewModel.fps.observe(this) {
            binding.tvFps.text = it
        }

        // 1. 마지막 필터 정보를 받아와서 초기 스크롤 위치를 설정합니다.
        viewModel.lastSelectedFilter.observe(this) { lastFilter ->
            binding.rvFilters.post {
                val layoutManager = binding.rvFilters.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()
                if (currentPosition == RecyclerView.NO_POSITION) return@post

                val targetPosition = filterAdapter.findClosestPosition(currentPosition, lastFilter)

                val smoothScroller = object : LinearSmoothScroller(this) {
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
        viewModel.loadLastFilter() // ViewModel에 마지막 필터 로드를 요청합니다.
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

        // 2. 초기 위치를 대략적으로만 설정합니다. (정확한 위치는 ViewModel이 설정)
        val startPosition = Integer.MAX_VALUE / 2
        layoutManager.scrollToPosition(startPosition)

        // 3. 초기 클릭 이벤트를 제거합니다.
        // binding.rvFilters.post { ... }

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
