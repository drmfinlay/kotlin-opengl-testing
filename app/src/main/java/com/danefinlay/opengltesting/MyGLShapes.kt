package com.danefinlay.opengltesting

import android.opengl.GLES20
import com.danefinlay.util.GLESProgram
import com.danefinlay.util.OpenGLShape
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/** Shapes to be used with OpenGL ES
 * Created by dane on 4/11/16.
 */

class Triangle : OpenGLShape {
    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle: Int = 0 // Used to access and set the view transformation
    private val vertexBuffer: FloatBuffer
    private var triangleCoords = floatArrayOf(// in counterclockwise order:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    )
    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    constructor(mGLESProgram: GLESProgram) : super(mGLESProgram)

    init {
        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords)
        // set the buffer to read the first coordinate
        vertexBuffer.position(0)
    }

    override fun draw(mMVPMatrix: FloatArray) {
        val programRef = myProgram.programRef

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(programRef)

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(programRef, "vPosition")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(programRef, "vColor")

        // Get the handle to the shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programRef, "uMVPMatrix")

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, colour, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        // number of coordinates per vertex in this array
        val COORDS_PER_VERTEX = 3
        internal val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
        // Set color with red, green, blue and alpha (opacity) values
        internal val colour = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
    }
}

