package com.example.skjo.retrica.utils

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.core.content.ContextCompat
import com.example.skjo.retrica.model.FilterData
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(private val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer, IFilter {
    private var performanceMonitor: PerformanceMonitor? = null
    private var lastFrameTimeNs: Long = 0
    private val frameTimes = LongArray(10) // Moving average window
    private var frameTimeIndex = 0

    interface PerformanceMonitor {
        fun onFpsUpdated(fps: Double)
    }

    fun setPerformanceMonitor(monitor: PerformanceMonitor) {
        performanceMonitor = monitor
    }

    @Volatile
    private var currentFilterType: FilterData = FilterData.NONE

    private val vertices = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)
    private val textureVertices = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)

    private var vertexBuffer: FloatBuffer
    private var textureBuffer: FloatBuffer

    private val programs = mutableMapOf<FilterData, Int>()
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

    private fun baseShader(body: String) =
        "#version 100\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 v_TexCoord;\n" +
                "uniform samplerExternalOES u_Texture;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(u_Texture, v_TexCoord);\n" +
                "  $body\n" +
                "}"

    private val fragmentShaderBodies = mapOf(
        FilterData.NONE to "gl_FragColor = color;",
        FilterData.GRAYSCALE to "float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));\n    gl_FragColor = vec4(gray, gray, gray, 1.0);",
        FilterData.SEPIA to "vec3 sepiaColor = vec3(dot(color.rgb, vec3(0.393, 0.769, 0.189)), dot(color.rgb, vec3(0.349, 0.686, 0.168)), dot(color.rgb, vec3(0.272, 0.534, 0.131)));\n    gl_FragColor = vec4(sepiaColor, 1.0);",
        FilterData.INVERT to "gl_FragColor = vec4(1.0 - color.r, 1.0 - color.g, 1.0 - color.b, 1.0);",
        FilterData.VIGNETTE to "float d = distance(v_TexCoord, vec2(0.5, 0.5));\n    float vignette = smoothstep(0.8, 0.4, d);\n    gl_FragColor = vec4(color.rgb * vignette, 1.0);",
        FilterData.POSTERIZE to "float numColors = 8.0;\n    gl_FragColor = vec4(floor(color.r * numColors) / numColors, floor(color.g * numColors) / numColors, floor(color.b * numColors) / numColors, 1.0);"
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices).position(0) }
        textureBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(textureVertices).position(0) }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        fragmentShaderBodies.forEach { (type, body) ->
            val fragmentShader = baseShader(body)
            programs[type] = createProgram(vertexShaderCode, fragmentShader)
        }

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setOnFrameAvailableListener { glSurfaceView.requestRender() }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) { GLES20.glViewport(0, 0, width, height) }

    override fun onDrawFrame(gl: GL10?) {
        val currentTimeNs = System.nanoTime()
        if (lastFrameTimeNs > 0) {
            val frameTimeNs = currentTimeNs - lastFrameTimeNs
            frameTimes[frameTimeIndex % frameTimes.size] = frameTimeNs
            frameTimeIndex++

            if (frameTimeIndex > frameTimes.size) { // Avarage after 10 frames
                val averageFrameTimeNs = frameTimes.average()
                val fps = 1_000_000_000.0 / averageFrameTimeNs
                performanceMonitor?.onFpsUpdated(fps)
            }
        }
        lastFrameTimeNs = currentTimeNs

        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(transformMatrix)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val program = programs[currentFilterType] ?: programs[FilterData.NONE] ?: return
        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "u_Texture")
        transformMatrixHandle = GLES20.glGetUniformLocation(program, "u_TransformMatrix")

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

    override val surfaceProvider = Preview.SurfaceProvider { request -> onSurfaceRequested(request) }

    private fun onSurfaceRequested(request: SurfaceRequest) {
        surfaceTexture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
        val surface = android.view.Surface(surfaceTexture)
        request.provideSurface(surface, ContextCompat.getMainExecutor(glSurfaceView.context)) {}
    }

    override fun setFilter(type: FilterData) {
        glSurfaceView.queueEvent {
            currentFilterType = type
        }
    }

    override fun release() {
        glSurfaceView.queueEvent {
            programs.values.forEach { GLES20.glDeleteProgram(it) }
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            surfaceTexture.release()
        }
    }

    private fun createProgram(vertexShader: String, fragmentShader: String): Int {
        val vs = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }
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
}
