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
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.skjo.retrica.databinding.ActivityMainBinding
import com.example.skjo.retrica.model.FilterData
import com.example.skjo.retrica.ui.BaseActivity
import com.example.skjo.retrica.ui.main.filter.FilterAdapter
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
    private lateinit var filterAdapter: FilterAdapter

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        observeViewModel()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // GLSurfaceView 및 필터(렌더러) 설정
        val renderer = GLRenderer(binding.layoutGlSurfaceView)
        filter = renderer
        binding.layoutGlSurfaceView.setEGLContextClientVersion(3) // OpenGL ES 3.0 사용
        binding.layoutGlSurfaceView.setRenderer(renderer)
        binding.layoutGlSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // filter가 초기화된 후에 호출되어야 합니다.
        setupFilterList()
        setupCamera()
    }

    private fun observeViewModel() {
        viewModel.data.observe(this) {
            binding.tvFps.text = it
        }
        viewModel.fetchData()
    }

    private fun setupFilterList() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilters.layoutManager = layoutManager

        filterAdapter = FilterAdapter(this) { clickedView ->
            val clickedPosition = layoutManager.getPosition(clickedView)
            if (clickedPosition == RecyclerView.NO_POSITION) return@FilterAdapter

            val smoothScroller = object : LinearSmoothScroller(this) {
                override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                    val dxToStart = super.calculateDxToMakeVisible(view, -1) // SNAP_TO_START
                    val dxToEnd = super.calculateDxToMakeVisible(view, 1)   // SNAP_TO_END
                    return (dxToStart + dxToEnd) / 2
                }
            }
            smoothScroller.targetPosition = clickedPosition
            layoutManager.startSmoothScroll(smoothScroller)
        }
        binding.rvFilters.adapter = filterAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvFilters)

        val nonePosition = FilterData.entries.indexOf(FilterData.NONE)
        val startPosition = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % FilterData.entries.size) + nonePosition
        layoutManager.scrollToPosition(startPosition)
        
        binding.rvFilters.post { 
            filter.setFilter(FilterData.NONE)
        }

        binding.rvFilters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager) ?: return
                    val position = layoutManager.getPosition(centerView)
                    val selectedFilter = filterAdapter.getFilterItemAt(position)
                    filter.setFilter(selectedFilter.type)
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
