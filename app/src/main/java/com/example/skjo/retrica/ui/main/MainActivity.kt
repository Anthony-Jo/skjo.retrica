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
import androidx.recyclerview.widget.LinearSnapHelper
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
        // 클릭 리스너는 이제 필터 적용이 아닌, 해당 아이템을 중앙으로 스크롤하는 역할을 합니다.
        filterAdapter = FilterAdapter(this) { clickedPosition ->
            binding.rvFilters.smoothScrollToPosition(clickedPosition)
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilters.layoutManager = layoutManager
        binding.rvFilters.adapter = filterAdapter

        // 1. 아이템이 중앙에 오도록 스냅하는 SnapHelper 추가
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvFilters)

        // 2. 무한 스크롤을 위해 중간 위치에서 시작
        val startPosition = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % FilterData.entries.size)
        layoutManager.scrollToPosition(startPosition)

        // 3. 스크롤이 멈췄을 때만 필터를 적용하는 리스너 추가
        binding.rvFilters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 스크롤이 완전히 멈췄을 때만 필터를 적용하여 리소스 낭비를 막습니다.
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
