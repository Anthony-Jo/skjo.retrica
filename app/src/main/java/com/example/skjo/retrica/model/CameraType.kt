package com.example.skjo.retrica.model

import androidx.camera.core.CameraSelector

enum class CameraType(val value: Int) {
    Front(CameraSelector.LENS_FACING_FRONT),
    Back(CameraSelector.LENS_FACING_BACK);
    companion object {
        fun toLensFacing(value: Int): CameraType {
            return when (value) {
                CameraSelector.LENS_FACING_FRONT -> Front
                CameraSelector.LENS_FACING_BACK -> Back
                else -> Back
            }
        }
    }
}