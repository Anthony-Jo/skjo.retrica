package com.example.skjo.retrica.model

import androidx.annotation.DrawableRes

/**
 * 필터의 종류를 정의하는 열거형 클래스.
 */
enum class FilterType {
    NONE,       // 원본
    GRAYSCALE,  // 흑백
    SEPIA,      // 세피아
    INVERT,     // 색상 반전
    VIGNETTE,   // 비네트
    POSTERIZE   // 포스터 효과
}

/**
 * RecyclerView에 표시될 필터의 UI 정보를 담는 데이터 클래스.
 * @param name 필터 이름 (예: "Sepia")
 * @param type 필터 종류 (FilterType.SEPIA)
 * @param thumbnail 필터 미리보기 이미지의 리소스 ID
 */
data class FilterInfo(
    val name: String,
    val type: FilterType,
    @DrawableRes val thumbnail: Int
)
