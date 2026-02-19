package com.example.skjo.retrica.utils

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.example.skjo.retrica.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // 1. 정점 데이터 정의 (삼각형의 세 꼭짓점 좌표)
    //    좌표는 -1.0 ~ 1.0 사이의 값으로 표현됩니다. (NDC: Normalized Device Coordinates)
    private val triangleCoords = floatArrayOf(
        0.0f, 0.5f,   // 상단 중앙
        -0.5f, -0.5f, // 왼쪽 하단
        0.5f, -0.5f   // 오른쪽 하단
    )

    private var program: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 배경색 설정 (검은색)
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 3. 셰이더 파일을 읽어서 GPU 프로그램을 생성합니다.
        val vertexShaderSource = ShaderUtils.readShaderSource(context, R.raw.vertex_shader)
        val fragmentShaderSource = ShaderUtils.readShaderSource(context, R.raw.fragment_shader)
        program = ShaderUtils.createProgram(vertexShaderSource, fragmentShaderSource)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    // 1. 정점 데이터 수정 (전체 화면을 덮는 사각형)
    //    사각형은 두 개의 삼각형으로 이루어짐 (총 6개의 정점)
    private val squareCoords = floatArrayOf(
        -1.0f,  1.0f, // 왼쪽 위
        -1.0f, -1.0f, // 왼쪽 아래
        1.0f, -1.0f, // 오른쪽 아래
        -1.0f,  1.0f, // 왼쪽 위
        1.0f, -1.0f, // 오른쪽 아래
        1.0f,  1.0f  // 오른쪽 위
    )

    // 2. 텍스처 좌표 정의
    //    이미지의 어느 부분을 각 정점에 매핑할지 정의합니다. (0,0)은 왼쪽 아래.
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f, // 왼쪽 위
        0.0f, 0.0f, // 왼쪽 아래
        1.0f, 0.0f, // 오른쪽 아래
        0.0f, 1.0f, // 왼쪽 위
        1.0f, 0.0f, // 오른쪽 아래
        1.0f, 1.0f  // 오른쪽 위
    )

    // 버퍼들도 새로운 데이터에 맞게 수정
    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }

    private val textureBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(textureCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(textureCoords)
                position(0)
            }
        }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(program)

        // --- 정점 데이터 전달 ---
        val positionHandle = GLES30.glGetAttribLocation(program, "a_Position")
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(
            positionHandle,
            2, GLES30.GL_FLOAT, false, 0, // Stride를 0으로 변경
            vertexBuffer
        )

        // --- 텍스처 좌표 데이터 전달 (새로 추가) ---
        val texCoordHandle = GLES30.glGetAttribLocation(program, "a_TexCoord")
        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(
            texCoordHandle,
            2, GLES30.GL_FLOAT, false, 0,
            textureBuffer
        )

        // 삼각형 대신 사각형(정점 6개)을 그립니다.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)

        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texCoordHandle) // 비활성화 추가
    }
}
