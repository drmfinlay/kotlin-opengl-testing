package com.danefinlay.opengltesting

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import com.danefinlay.util.OpenGLShape
import com.danefinlay.util.createGLESProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Example OpenGL surface view implementation
 * Created by dane on 4/11/16.
 */
class MyGLSurfaceView : GLSurfaceView {
    val myRenderer: MyGLRendererExample
    private var previousXPos: Float = 0f
    private var previousYPos: Float = 0f

    // Members used for pinch zooming in and out
    private val scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1f
    private var currentlyScaling: Boolean = false

    constructor(context: Context?): super(context) {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        myRenderer = MyGLRendererExample()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(myRenderer)

        // Render the view only when there is a change in the drawing data
        // To allow the triangle to rotate automatically, this line is commented out:
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Set up gesture detectors
        scaleGestureDetector = ScaleGestureDetector(this.context, object : OnScaleGestureListener {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                currentlyScaling = true
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                currentlyScaling = false
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor

                // Change the camera's Z eye coordinate
                myRenderer.cameraEyeZ *= scaleFactor

                // Don't let the object get too small or too large.
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f))

                // Limit the zooming based on the width and height of the View
                val orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"

                if ( orientation == "landscape" )
                    myRenderer.cameraEyeZ = Math.max(LANDSCAPE_NEAR_LIMIT, Math.min(myRenderer.cameraEyeZ, LANDSCAPE_FAR_LIMIT))
                else // portrait
                    myRenderer.cameraEyeZ = Math.max(PORTRAIT_NEAR_LIMIT, Math.min(myRenderer.cameraEyeZ, PORTRAIT_FAR_LIMIT))


                // Tell the renderer it is time to render the frame
                requestRender()

                // invalidate() - invalidates the current View so that android knows it needs to be
                // redrawn
                invalidate()
                return true
            }
        })
    }


    override fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen and other input controls.
        // Let the ScaleGestureDetector inspect all events.
        scaleGestureDetector.onTouchEvent(e)

        when {
            e.action == MotionEvent.ACTION_MOVE && !currentlyScaling -> {
                // Calculate the difference between the previous and current X and Y touch
                // coordinates
                var dx = e.x - previousXPos
                var dy = e.y - previousYPos

                // Determine the rotation direction based on where the user has touched the screen
                // reverse direction of rotation above the mid-line
                if ( y > height / 2 ) dx *= -1

                // reverse direction of rotation to left of the mid-line
                if ( x < width / 2 )  dy *= -1

                // Update the renderer angle used to rotate the triangles
                // This uses the getter and setter, and the variable is volatile
                myRenderer.angle += (dx + dy) * TOUCH_SCALE_FACTOR

                // Tell the renderer it is time to render the frame
                requestRender() // uses the renderer that is set by setRenderer
            }
        }
        previousXPos = e.x
        previousYPos = e.y
        return true
    }

    inner class MyGLRendererExample : GLSurfaceView.Renderer {
        private var triangle1: OpenGLShape? = null
        private var triangle2: OpenGLShape? = null

        // Changes to this variable need to be atomic because it runs on a different thread
        // from the main user interface. So it should be either:
        // 1) volatile because renderer code runs, or
        // 2) an AtomicFloat (which does not exist...)
        @Volatile var angle: Float = 0.0f
            get() = field
            set(value) { field = value }

        // Use this for changing the camera position when scaling (pinching in or out)
        @Volatile var cameraEyeZ: Float = 3f
            get() = field
            set(value) { field = value }

        // Matrices
        // These are here because the matrices are not really attributes of each object in OpenGL
        // they are operated on and then objects are drawn with respect to them
        private val mMVPMatrix = FloatArray(16) // model view projection matrix
        private val mProjectionMatrix = FloatArray(16)
        private val mViewMatrix = FloatArray(16)

        /** Equivalent to glutInit - called once to set up the view's OpenGL ES environment. */
        override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(255F, 0F, 0F, 1F)
            triangle1 = Triangle(createGLESProgram())
            triangle2 = Triangle(createGLESProgram())

        }

        /** Same as glut's displayCallback - called for each redraw of the View obj */
        override fun onDrawFrame(unused: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            // Apply a camera view transformation in order for anything to show up on screen.
            // Set the camera position (View matrix) looking at the origin from (0, 0, -3)
            // setLookAtM parameters: (float[] rm, int rmOffset,
            // float eyeX, float eyeY, float eyeZ,
            // float centerX, float centerY, float centerZ,
            // float upX, float upY, float upZ)
            Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, cameraEyeZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

            // Keep a copy of mMVPMatrix
            val mMVPMatrix2 = mMVPMatrix.clone()

            // Calculate the rotation angle
            // val time = (SystemClock.uptimeMillis() % 4000L)
            // angle = 0.09f * time.toInt()

            // TRaSheS - Translate, Rotate, Shear (not in Matrix class), and then Scale

            // Translate the first triangle to the left on the x-axis
            // (in-place method)
            Matrix.translateM(mMVPMatrix, 0, -0.5f, 0f, 0f)

            // Rotate the first triangle
            // rotateM - Rotates matrix m in place by angle a (in degrees) around the axis (x, y, z).
            // second parameter is "offset index into m where the matrix starts"
            Matrix.rotateM(mMVPMatrix, 0, angle, 0f, 0f, -1.0f)

            // Translate the second triangle
            Matrix.translateM(mMVPMatrix2, 0, 0.5f, 0f, 0f)

            // Rotate the second triangle in the opposite direction on the z-axis
            Matrix.rotateM(mMVPMatrix2, 0, angle, 0f, 0f, 1.0f)

            triangle1?.draw(mMVPMatrix)
            triangle2?.draw(mMVPMatrix2)
        }

        /** Same as glut's reshape callback function for when the window geometry changes
         * called when the screen is rotated, for example
         */
        override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val aspectRatio = width / height.toFloat()

            val orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"

            // frustumM - Defines a projection matrix in terms of six clip (clipping) planes.
            if ( orientation == "landscape" )
                Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, LANDSCAPE_NEAR_LIMIT, LANDSCAPE_FAR_LIMIT)
            else
                Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, PORTRAIT_NEAR_LIMIT, PORTRAIT_FAR_LIMIT)
        }
    }

    companion object {
        private val TOUCH_SCALE_FACTOR = 210.0f / 360
        // Values used for OpenGL projection and touch scaling gestures (pinch in-out)
        private val PORTRAIT_NEAR_LIMIT = 1.5F
        private val PORTRAIT_FAR_LIMIT = 12F
        private val LANDSCAPE_NEAR_LIMIT = 2.5F
        private val LANDSCAPE_FAR_LIMIT = 9F
    }
}
