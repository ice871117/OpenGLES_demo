package com.tencent.testglsurface

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by wiizhang on 2018/5/22.
 */

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var mGLSurfaceView : GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        mGLSurfaceView = findViewById(R.id.gl_surface_view) as GLSurfaceView
        mGLSurfaceView?.setRenderer(SimpleRenderer())
        mGLSurfaceView?.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }

    inner class SimpleRenderer : GLSurfaceView.Renderer {

        private var vertices : FloatBuffer
        private var indices : ShortBuffer

        init {
            Log.d(TAG, "init called")
            val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
            byteBuffer.order(ByteOrder.nativeOrder())
            vertices = byteBuffer.asFloatBuffer()
            vertices.put(floatArrayOf(-80f, -120f,
                    80f, -120f,
                    -80f, 120f,
                    80f, 120f))

            val indicesBuffer = ByteBuffer.allocateDirect(6 * 2)
            indicesBuffer.order(ByteOrder.nativeOrder())
            indices = indicesBuffer.asShortBuffer()
            indices.put(shortArrayOf(0, 1, 2, 1, 2, 3))

            vertices.flip()
            indices.flip()
        }

        override fun onDrawFrame(gl: GL10?) {
            Log.d(TAG, "onDrawFrame indices:${indices.remaining()} ; vertices:${vertices.remaining()}")
            //定义显示在屏幕上的什么位置(opengl 自动转换)
            gl?.glViewport(0, 0, mGLSurfaceView!!.width, mGLSurfaceView!!.height)
            gl?.glClear(GL10.GL_COLOR_BUFFER_BIT)
            gl?.glMatrixMode(GL10.GL_PROJECTION)
            gl?.glLoadIdentity()
            //设置视锥体的大小，一个很扁的长方体
            gl?.glOrthof(-160f, 160f, -240f, 240f, 1f, -1f)
            //颜色设置为红色
            gl?.glColor4f(1f, 0f, 0f, 1f)
            gl?.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl?.glVertexPointer( 2, GL10.GL_FLOAT, 0, vertices)
            gl?.glDrawElements(GL10.GL_TRIANGLE_STRIP, 6, GL10.GL_UNSIGNED_SHORT, indices)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceChanged w = $width ; h = $height")
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.d(TAG, "onSurfaceCreated")
        }
    }
}