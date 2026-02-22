package com.example.skjo.retrica.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skjo.retrica.data.SharedPrefWrapper
import com.example.skjo.retrica.model.CameraType
import com.example.skjo.retrica.model.FilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPrefWrapper: SharedPrefWrapper
) : ViewModel() {

    // 마지막으로 선택된 필터를 UI에 전달하기 위한 LiveData
    private val _lastSelectedFilter = MutableLiveData<FilterType>()
    val lastSelectedFilter: LiveData<FilterType> = _lastSelectedFilter

    private val _fps = MutableLiveData<String>()
    val fps: LiveData<String> = _fps

    private val _currentFilter = MutableLiveData<String>()
    val currentFilter: LiveData<String> = _currentFilter

    init {
        loadInitialData()
    }

    /**
     * 렌더링 스레드에서 호출되어 FPS 값을 업데이트합니다.
     */
    fun updateFps(fps: Double) {
        val fpsText = String.format("%.1f FPS", fps)
        _fps.postValue(fpsText)
    }

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized
    // ---

    private fun loadInitialData() {
        viewModelScope.launch {
            // 여러 비동기 로딩이 있다면 여기서 한 번에 처리
            val lastCamera = sharedPrefWrapper.getLastCamera()
            val lastFilter = sharedPrefWrapper.getLastFilter()

            _lastUsedCamera.value = lastCamera
            _lastSelectedFilter.value = lastFilter

            // 모든 데이터 로딩이 끝나면 상태를 true로 변경
            _isInitialized.value = true
        }
    }

    /**
     * 사용자가 선택한 필터를 SharedPreferences에 저장합니다.
     */
    fun saveLastFilter(filterType: FilterType) {
        viewModelScope.launch {
            sharedPrefWrapper.setLastFilter(filterType)
            _currentFilter.postValue("Filter: ${filterType.name}")
        }
    }

    private val _lastUsedCamera = MutableLiveData<Int>()
    val lastUsedCamera: LiveData<Int> = _lastUsedCamera

    fun saveLastCamera(lensFacing: CameraType) {
        viewModelScope.launch {
            sharedPrefWrapper.setLaseCamera(lensFacing)
        }
    }
}
