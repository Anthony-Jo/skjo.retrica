package com.example.skjo.retrica.model

import com.example.skjo.retrica.R

/**
 * 필터의 종류를 정의하는 열거형 클래스.
 */
enum class FilterData(val title: Int, val thumbnail: Int) {
    /**
     * 원본
     */
    NONE(
        title = R.string.filter_title_none,
        thumbnail = R.mipmap.ic_launcher
    ),

    /**
     * 흑백
     */
    GRAYSCALE(
        title = R.string.filter_title_grayscale,
        thumbnail = R.mipmap.ic_launcher
    ),

    /**
     * 세피아
     */
    SEPIA(
        title = R.string.filter_title_sepia,
        thumbnail = R.mipmap.ic_launcher
    ),

    /**
     * 반전
     */
    INVERT(
        title = R.string.filter_title_invert,
        thumbnail = R.mipmap.ic_launcher
    ),

    /**
     * 바네트
     */
    VIGNETTE(
        title = R.string.filter_title_vignette,
        thumbnail = R.mipmap.ic_launcher
    ),

    /**
     * 포스터 효과
     */
    POSTERIZE(
        title = R.string.filter_title_posterize,
        thumbnail = R.mipmap.ic_launcher
    )
}