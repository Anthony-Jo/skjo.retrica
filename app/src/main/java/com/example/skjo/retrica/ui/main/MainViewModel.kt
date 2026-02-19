package com.example.skjo.retrica.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skjo.retrica.data.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

    fun fetchData() {
        viewModelScope.launch {
            // TODO: MainRepository를 통해 데이터 가져오는 로직 구현
            _data.value = "Hello from ViewModel"
        }
    }
}
