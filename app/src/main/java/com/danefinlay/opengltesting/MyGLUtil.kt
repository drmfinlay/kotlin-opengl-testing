package com.danefinlay.util

import android.opengl.GLES20
import android.util.Log
import com.danefinlay.opengltesting.sampleFragmentShaderCode
import com.danefinlay.opengltesting.sampleVertexShaderCode
import java.io.File
import java.nio.IntBuffer


/** Wrapper data class for OpenGL Shading Language (GLSL) source code */
data class GLSLSourceCode(val shaderCode: String)

/** Wrapper class for GLESPrograms */
data class GLESProgram(val programRef: Int)

/**
 * Factory function for creating wrapped GLES programs
 * This is here because passing around Ints as programs seems stupid to me
 */
fun createGLESProgram(): GLESProgram {
    // create empty OpenGL ES Program
    return GLESProgram(GLES20.glCreateProgram())
}

/**
 * Parent class of my OpenGL objects
 */
abstract class OpenGLShape {
    protected val myProgram: GLESProgram

    constructor(program: GLESProgram,
                vertexShaderCode: GLSLSourceCode = GLSLSourceCode(sampleVertexShaderCode),
                fragmentShaderCode: GLSLSourceCode = GLSLSourceCode(sampleFragmentShaderCode)) {

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        myProgram = program

        // add the vertex shader to program
        GLES20.glAttachShader(myProgram.programRef, vertexShader)

        // add the fragment shader to program
        GLES20.glAttachShader(myProgram.programRef, fragmentShader)

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(myProgram.programRef)
    }
    /** Function called to draw the shape on the device screen */
    abstract fun draw(mMVPMatrix: FloatArray)
}

/** Function that handles compilation of OpenGL Shading Language (GLSL) code
 * Converted to Kotlin from the code available here: https://developer.android.com/training/graphics/opengl/draw.html
 */

fun loadShader(type: Int, shaderSourceCode: GLSLSourceCode): Int {
    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
    val shader = GLES20.glCreateShader(type)

    // add the source code to the shader and compile it
    GLES20.glShaderSource(shader, shaderSourceCode.shaderCode)
    GLES20.glCompileShader(shader)

    val compilationStatus = IntArray(1) // Only holds one int

    // Note: The last parameter of this method is the index of the IntArray to use
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compilationStatus, 0)

    // Log an error message when a shader doesn't compile
    if ( compilationStatus[0] == GLES20.GL_FALSE ) {
        Log.e("OpenGL", "shader failed to compile")

    }

    return shader
}
