package com.example.skjo.retrica.data

import android.content.SharedPreferences
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import com.example.skjo.retrica.model.FilterData
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton // Hilt가 이 클래스도 싱글톤으로 관리하도록 합니다.
class SharedPrefWrapper @Inject constructor(
    private val prefs: SharedPreferences
) {

    companion object {
        private const val KEY_LAST_FILTER = "last_filter"
        private const val KEY_LAST_CAMERA = "last_camera"
    }

    /**
     * 마지막으로 사용한 필터 타입을 저장합니다.
     */
    fun setLastFilter(filterType: FilterData) {
        prefs.edit { putString(KEY_LAST_FILTER, filterType.name) }
    }

    /**
     * 마지막으로 사용한 필터 타입을 불러옵니다.
     * @return 저장된 FilterType. 없으면 기본값으로 NONE을 반환합니다.
     */
    fun getLastFilter(): FilterData {
        val filterName = prefs.getString(KEY_LAST_FILTER, FilterData.NONE.name)
        return try {
            FilterData.valueOf(filterName ?: FilterData.NONE.name)
        } catch (_: IllegalArgumentException) {
            FilterData.NONE
        }
    }


    /**
     * 마지막으로 사용한 카메라 방향(Int)을 저장합니다.
     */
    fun setLastCamera(cameraLensFacing: Int) {
        // SharedPreferences에 정수 값을 저장합니다.
        prefs.edit { putInt(KEY_LAST_CAMERA, cameraLensFacing) }
    }

    /**
     * 마지막으로 사용한 카메라 방향을 불러옵니다.
     * @return 저장된 카메라 방향. 없으면 기본값으로 후면 카메라를 반환합니다.
     */
    fun getLastCamera(): Int {
        // 저장된 값을 불러오고, 없으면 CameraSelector.LENS_FACING_BACK을 기본값으로 사용합니다.
        return prefs.getInt(KEY_LAST_CAMERA, CameraSelector.LENS_FACING_BACK)
    }
}
