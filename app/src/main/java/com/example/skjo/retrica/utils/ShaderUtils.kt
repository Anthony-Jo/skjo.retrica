package com.example.skjo.retrica.utils

import android.content.Context
import android.opengl.GLES30
import java.io.BufferedReader
import java.io.InputStreamReader

object ShaderUtils {

    fun readShaderSource(context: Context, resourceId: Int): String {
        val builder = StringBuilder()
        val inputStream = context.resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.close()
        }
        return builder.toString()
    }

    fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 프로그램 생성
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        // 링킹 상태 확인
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
            throw RuntimeException("Could not link program: $info")
        }

        // 셰이더 객체는 프로그램에 연결 후 삭제 가능
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        return program
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        // 컴파일 상태 확인
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("Could not compile shader $type: $info")
        }

        return shader
    }
}
