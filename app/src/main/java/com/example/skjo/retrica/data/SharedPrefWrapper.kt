package com.example.skjo.retrica.data

import android.content.SharedPreferences
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
        } catch (e: IllegalArgumentException) {
            FilterData.NONE // 저장된 이름이 enum에 없으면 NONE을 반환
        }
    }
}
