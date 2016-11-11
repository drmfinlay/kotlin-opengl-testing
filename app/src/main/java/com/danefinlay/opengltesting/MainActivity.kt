package com.danefinlay.opengltesting

import android.content.Context
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    private var myGLView: MyGLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and
        myGLView = MyGLSurfaceView(this)

        // Set it as the ContentView for this Activity.
        setContentView(myGLView)
        // setContentView(R.layout.activity_main)
        // DON'T restore any state information here. Do so in onRestoreInstanceState
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        // Save the state of myGLView for when, for example, the user rotates the screen
        savedInstanceState?.putFloat(STATE_ROTATION_ANGLE, myGLView!!.myRenderer.angle)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        // Restore any state info here
        myGLView?.myRenderer?.angle = savedInstanceState?.getFloat(STATE_ROTATION_ANGLE) as Float
        super.onRestoreInstanceState(savedInstanceState)
    }

    companion object {
        private val STATE_ROTATION_ANGLE = "angle"
    }

}


