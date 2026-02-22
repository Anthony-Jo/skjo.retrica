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

    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

    // 마지막으로 선택된 필터를 UI에 전달하기 위한 LiveData
    private val _lastSelectedFilter = MutableLiveData<FilterData>()
    val lastSelectedFilter: LiveData<FilterData> = _lastSelectedFilter

    init {
        loadLastFilter()
    }

    fun fetchData() {
        viewModelScope.launch {
            _data.value = "Hello from ViewModel"
        }
    }

    /**
     * 사용자가 선택한 필터를 SharedPreferences에 저장합니다.
     */
    fun saveLastFilter(filterType: FilterData) {
        sharedPrefWrapper.setLastFilter(filterType)
    }

    /**
     * SharedPreferences에서 마지막으로 사용한 필터를 불러와 LiveData에 설정합니다.
     */
    private fun loadLastFilter() {
        val lastFilter = sharedPrefWrapper.getLastFilter()
        _lastSelectedFilter.value = lastFilter
    }
}
