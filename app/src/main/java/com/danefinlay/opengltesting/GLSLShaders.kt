package com.danefinlay.opengltesting

/**
 * GLSL shaders as strings
 * "//" Comments and new line characters in GLSL code are completely fine, no compilation errors.
 * Created by dane on 4/11/16.
 */
val sampleVertexShaderCode =
        """
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
           void main() {
               // the matrix must be included as a modifier of gl_Position
               // Note that the uMVPMatrix factor *must be first* in order
               // for the matrix multiplication product to be correct.
               gl_Position = uMVPMatrix * vPosition;
           }
        """

val sampleFragmentShaderCode =
        """precision mediump float;
            uniform vec4 vColor;
            void main() {
              gl_FragColor = vColor;
            }
        """