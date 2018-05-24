package com.tencent.testglsurface

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
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

        private var mVertices: FloatBuffer
        private var mIndices: ShortBuffer
        private var mTexture: FloatBuffer
        private var mTextureId: Int? = null
        private var mTextureW : Float = 0f
        private var mTextureH : Float = 0f

        init {
            Log.d(TAG, "init called")

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.mainecoon_cat, options)
            mTextureW = 160f
            mTextureH = mTextureW * options.outHeight / options.outWidth

            val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
            byteBuffer.order(ByteOrder.nativeOrder())
            mVertices = byteBuffer.asFloatBuffer()
            mVertices.put(floatArrayOf(-mTextureW/2, -mTextureH/2,
                    mTextureW/2, -mTextureH/2,
                    -mTextureW/2, mTextureH/2,
                    mTextureW/2, mTextureH/2))

            val indicesBuffer = ByteBuffer.allocateDirect(6 * 2)
            indicesBuffer.order(ByteOrder.nativeOrder())
            mIndices = indicesBuffer.asShortBuffer()
            mIndices.put(shortArrayOf(0, 1, 2, 1, 2, 3))

            val textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
            textureBuffer.order(ByteOrder.nativeOrder())
            mTexture = textureBuffer.asFloatBuffer()
            mTexture.put(floatArrayOf(0f, 1f,
                                     1f, 1f,
                                     0f, 0f,
                                     1f, 0f))

            mVertices.flip()
            mIndices.flip()
            mTexture.flip()
        }

        override fun onDrawFrame(gl: GL10?) {
            Log.d(TAG, "onDrawFrame mIndices:${mIndices.remaining()} ; mVertices:${mVertices.remaining()}")
            gl?.let {
                if (mTextureId == null) {
                    mTextureId = loadTexture(it)
                }
                //定义显示在屏幕上的什么位置(opengl 自动转换)
                it.glViewport(0, 0, mGLSurfaceView!!.width, mGLSurfaceView!!.height)
                it.glClear(GL10.GL_COLOR_BUFFER_BIT)
                it.glMatrixMode(GL10.GL_PROJECTION)
                it.glLoadIdentity()
                //设置视锥体的大小，一个很扁的长方体
                val widthForOrtho = mTextureW * 2f
                val heightForOrtho = widthForOrtho * mGLSurfaceView!!.height / mGLSurfaceView!!.width
                it.glOrthof(-widthForOrtho / 2,
                        widthForOrtho / 2,
                        -heightForOrtho / 2,
                        heightForOrtho / 2,
                        1f, -1f)
                it.glEnable(GL10.GL_TEXTURE_2D)
                it.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId!!)
                it.glEnableClientState(GL10.GL_VERTEX_ARRAY)
                it.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
                it.glVertexPointer( 2, GL10.GL_FLOAT, 0, mVertices)
                it.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexture)
                it.glDrawElements(GL10.GL_TRIANGLE_STRIP, 6, GL10.GL_UNSIGNED_SHORT, mIndices)
            }
            MainActivity.let {
                it::class.java.canonicalName
                it.javaClass.simpleName
            }
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceChanged w = $width ; h = $height")
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.d(TAG, "onSurfaceCreated")
        }

        private fun loadTexture(gl: GL10) : Int {
            val map = BitmapFactory.decodeResource(resources, R.drawable.mainecoon_cat)
            val textureIds = IntArray(1)
            gl.glGenTextures(1, textureIds, 0)
            val textureId : Int = textureIds[0]
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId)
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, map, 0)
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST.toFloat())
            gl.glBindTexture(GL10.GL_TEXTURE_2D, 0)
            map.recycle()
            return textureId
        }
    }
}