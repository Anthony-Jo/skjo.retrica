package com.example.skjo.retrica.utils

import androidx.camera.core.Preview

/**
 * 카메라 필터 구현을 위한 범용 인터페이스.
 * 특정 그래픽스 라이브러리에 대한 의존성을 없애고,
 * Activity와 필터 구현체 간의 결합도를 낮추는 역할을 합니다.
 */
interface IFilter {
    /**
     * CameraX Preview에 연결할 SurfaceProvider.
     * 필터 구현체는 이 SurfaceProvider를 통해 카메라 프레임을 받습니다.
     */
    val surfaceProvider: Preview.SurfaceProvider

    /**
     * 필터 활성화/비활성화 상태를 외부에서 안전하게 설정합니다.
     * @param enabled true이면 필터 활성화, false이면 비활성화.
     */
    fun setFilterEnabled(enabled: Boolean)

    /**
     * 필터와 관련된 모든 리소스(예: GL 텍스처, 프로그램)를 해제합니다.
     */
    fun release()
}
