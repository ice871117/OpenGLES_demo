package com.tencent.testglsurface

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
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

    private var mGLSurfaceView: GLSurfaceView? = null
    private var mRenderer: SimpleRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        mGLSurfaceView = findViewById(R.id.gl_surface_view) as GLSurfaceView
        mGLSurfaceView?.setEGLContextClientVersion(2)
        mRenderer = SimpleRenderer()
        mGLSurfaceView?.setRenderer(mRenderer)
        mGLSurfaceView?.visibility = View.VISIBLE
        mGLSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRenderer?.destroy()
    }

    inner class SimpleRenderer : GLSurfaceView.Renderer {

        private val VERTEX_SHADER =
                // 这个矩阵成员变量提供了一个勾子来操控
                // 使用这个顶点着色器的对象的坐标
                ("uniform mat4 uMVPMatrix;   \n" +
                        "attribute vec4 vPosition;  \n" +
                        "void main(){               \n" +
                        // the matrix must be included as part of gl_Position
                        " gl_Position = uMVPMatrix * vPosition; \n" +
                        "}  \n")

        private val FRAGMENT_SHADER = ("precision mediump float;\n"
                + "void main() {\n"
                + " gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                + "}")

        private val VERTEX_SHADER_TEXTURE = (
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec2 a_texCoord;" +
                        "varying vec2 v_texCoord;" +
                        "void main() {" +
                        " gl_Position = uMVPMatrix * vPosition;" +
                        " v_texCoord = a_texCoord;" +
                        "}")

        private val FRAGMENT_SHADER_TEXTURE = (
                "precision mediump float;" +
                        "varying vec2 v_texCoord;" +
                        "uniform sampler2D s_texture;" +
                        "void main() {" +
                        " gl_FragColor = texture2D(s_texture, v_texCoord);" +
                        "}")

        private var mVertices: FloatBuffer
        private var mIndices: ShortBuffer
        private var mTextures: FloatBuffer
        private var mTextureW: Float = 0f
        private var mTextureH: Float = 0f
        private var mProgram: Int = 0
        private var mPositionHandle: Int = 0
        // 二维坐标矩阵
        // by definition             in memory
        //          y                  0_____________x
        //          |                   |
        //          |                   |
        //          |                   |
        //          |__________x        |
        //         0                    y
        //
        private val VERTEX_INDEX_ARRAY = shortArrayOf(0, 1, 2, 0, 2, 3) // 这里顺序非常重要，不然纹理对应的图片就割裂了
        private val TEXTURE_INDEX = floatArrayOf(1f, 0f, // bottom right
                                                 0f, 0f, // bottom left
                                                 0f, 1f, // top left
                                                 1f, 1f) // top right

        private var mTexCoordHandle: Int = 0
        private var mTexSamplerHandle: Int = 0

        // matrix transformation
        private var mViewPorjMatrixHandle: Int = 0
        // 视口矩阵
        private val mViewMatrix = FloatArray(16)
        // 合并投影视口矩阵
        private val mViewPorjMatrix = FloatArray(16)
        // 投影矩阵
        private val mProjMatrix = FloatArray(16)

        // for texture
        private var mTexName: Int = 0

        init {
            Log.d(TAG, "init called")

            mTextureW = 1f
            mTextureH = 1f

            mVertices = ByteBuffer.allocateDirect(4 * 2 * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(floatArrayOf(mTextureW / 2, mTextureH / 2, // top right
                            -mTextureW / 2, mTextureH / 2, // top left
                            -mTextureW / 2, -mTextureH / 2, // bottom left
                            mTextureW / 2, -mTextureH / 2)) // bottom right
            mVertices.flip()

            mIndices = ByteBuffer.allocateDirect(VERTEX_INDEX_ARRAY.size * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer()
                    .put(VERTEX_INDEX_ARRAY)
            mIndices.flip()

            mTextures = ByteBuffer.allocateDirect(TEXTURE_INDEX.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(TEXTURE_INDEX)
            mTextures.flip()
        }

        override fun onDrawFrame(gl: GL10?) {
            Log.d(TAG, "onDrawFrame mVertices:${mVertices.remaining()}")
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            // 合并投影和视口矩阵
            Matrix.multiplyMM(mViewPorjMatrix, 0, mProjMatrix, 0, mViewMatrix, 0)
            // 应用合并后的投影和视口变换
            GLES20.glUniformMatrix4fv(mViewPorjMatrixHandle, 1, false, mViewPorjMatrix, 0)
            // 应用纹理
            GLES20.glUniform1i(mTexSamplerHandle, 0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX_ARRAY.size, GLES20.GL_UNSIGNED_SHORT, mIndices)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceChanged w = $width ; h = $height")
            GLES20.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height
            // 跟据设备屏幕的几何特征创建投影矩阵
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.d(TAG, "onSurfaceCreated")
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            loadTexture()
            mProgram = GLES20.glCreateProgram()
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_TEXTURE)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_TEXTURE)
            GLES20.glAttachShader(mProgram, vertexShader)
            GLES20.glAttachShader(mProgram, fragmentShader)
            GLES20.glLinkProgram(mProgram)
            GLES20.glUseProgram(mProgram)
            // 创建位置handle
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
            // 创建坐标handle
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord")
            // 创建纹理handle
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture")
            // 创建投影矩阵handle
            mViewPorjMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            // 创建一个视口矩阵
            //                  y
            //                  |
            //                  |
            //                  |__________x
            //                 /
            //                /
            //               /
            //              z
            Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

            // 加载顶点
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false,
                    0, mVertices)

            // 加载纹理
            GLES20.glEnableVertexAttribArray(mTexCoordHandle)
            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false,
                    0, mTextures)
        }

        fun destroy() {
            GLES20.glDeleteTextures(1, intArrayOf(mTexName), 0)
        }

        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        private fun loadTexture() {
            val texNames = IntArray(1)
            GLES20.glGenTextures(1, texNames, 0)
            mTexName = texNames[0]
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.mainecoon_cat)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }
    }
}