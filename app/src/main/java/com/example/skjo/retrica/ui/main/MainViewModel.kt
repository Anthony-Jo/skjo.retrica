package com.example.skjo.retrica.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skjo.retrica.data.SharedPrefWrapper
import com.example.skjo.retrica.model.FilterData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPrefWrapper: SharedPrefWrapper
) : ViewModel() {


    // 마지막으로 선택된 필터를 UI에 전달하기 위한 LiveData
    private val _lastSelectedFilter = MutableLiveData<FilterData>()
    val lastSelectedFilter: LiveData<FilterData> = _lastSelectedFilter

    private val _fps = MutableLiveData<String>()
    val fps: LiveData<String> = _fps

    init {
        loadLastFilter()
    }

    /**
     * 렌더링 스레드에서 호출되어 FPS 값을 업데이트합니다.
     */
    fun updateFps(fps: Double) {
        val fpsText = String.format("%.1f FPS", fps)
        _fps.postValue(fpsText)
    }

    /**
     * 사용자가 선택한 필터를 SharedPreferences에 저장합니다.
     */
    fun saveLastFilter(filterType: FilterData) {
        viewModelScope.launch {
            sharedPrefWrapper.setLastFilter(filterType)
        }
    }

    /**
     * SharedPreferences에서 마지막으로 사용한 필터를 불러와 LiveData에 설정합니다.
     */
    fun loadLastFilter() {
        val lastFilter = sharedPrefWrapper.getLastFilter()
        _lastSelectedFilter.value = lastFilter
    }
}
