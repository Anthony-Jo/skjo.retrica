package com.example.skjo.retrica.utils

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(private val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    private val vertices = floatArrayOf(
        -1.0f, -1.0f,  // 0, Bottom Left
        1.0f, -1.0f,   // 1, Bottom Right
        -1.0f, 1.0f,    // 2, Top Left
        1.0f, 1.0f     // 3, Top Right
    )

    // 표준 텍스처 좌표 (상하 반전 없음). transformMatrix가 변환을 처리합니다.
    private val textureVertices = floatArrayOf(
        0.0f, 0.0f, // 0, Bottom Left
        1.0f, 0.0f, // 1, Bottom Right
        0.0f, 1.0f, // 2, Top Left
        1.0f, 1.0f  // 3, Top Right
    )

    private var vertexBuffer: FloatBuffer
    private var textureBuffer: FloatBuffer

    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var transformMatrixHandle = 0

    private var textureId = 0
    private lateinit var surfaceTexture: SurfaceTexture

    private val transformMatrix = FloatArray(16)

    private val vertexShaderCode = "#version 100\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec2 a_TexCoord;\n" +
            "varying vec2 v_TexCoord;\n" +
            "uniform mat4 u_TransformMatrix;\n" +
            "void main() {\n" +
            "  gl_Position = a_Position;\n" +
            "  v_TexCoord = (u_TransformMatrix * vec4(a_TexCoord, 0.0, 1.0)).xy;\n" +
            "}"

    private val fragmentShaderCode = "#version 100\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 v_TexCoord;\n" +
            "uniform samplerExternalOES u_Texture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoord);\n" +
            "}"

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(vertices)
            position(0)
        }

        textureBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(textureVertices)
            position(0)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "u_Texture")
        transformMatrixHandle = GLES20.glGetUniformLocation(program, "u_TransformMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setOnFrameAvailableListener { glSurfaceView.requestRender() }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(transformMatrix)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

        GLES20.glUniformMatrix4fv(transformMatrixHandle, 1, false, transformMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Could not compile shader $type: $info")
        }
        return shader
    }

    val surfaceProvider = Preview.SurfaceProvider { request -> onSurfaceRequested(request) }

    private fun onSurfaceRequested(request: SurfaceRequest) {
        surfaceTexture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
        val surface = android.view.Surface(surfaceTexture)
        request.provideSurface(surface, ContextCompat.getMainExecutor(glSurfaceView.context)) {}
    }
}
