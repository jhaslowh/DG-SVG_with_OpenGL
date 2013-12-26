package com.dividedgames.svg;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.dividedgames.svg.io.FileHelper;
import com.example.dg_svg_with_opengl.R;

/**
 * 
 * @author Jonathan Haslow-Hall
 */
public class RenderView extends GLSurfaceView implements Renderer{
	// The sprite to draw to the screen
	private Sprite mSprite;
	
	// Global drawing variables 
	public static float[] projectionMatrix = new float[16];
	public final float[] mMVPMatrix = new float[16];
	public int shaderProgram = 0;
	
	// Shader Handles
	public static int mPositionHandle;
	public static int mColorHandle;
	public static int mTextureHandle;
	public static int mTextCordHandle;
	public static int mMVPMatrixHandle;
	
	public RenderView(Context context) {
		super(context);
		
		
		// Create an OpenGL ES 2.0 context
		setEGLContextClientVersion(2);
		setEGLConfigChooser(8, 8, 8, 8, 0, 0); 
        getHolder().setFormat(PixelFormat.RGBA_8888);
        
        // Grab focus 
        requestFocus();
        
        // Tell system that this is the renderer for the app
      	setRenderer(this);
	}
	
	/** 
	 * Call to load any textures that need loading 
	 */
	public void load(){
		// Set up the shaders 
		loadShaderProgram(getContext());
		// Load the sprite 
		// Make a new sprite 
		mSprite = new Sprite(0,0,200,200, "box.svg");
		mSprite.load(getContext());
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		/** Setup opengl **/ 
		// Enable face culling
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		// What faces to remove with the face culling
		GLES20.glCullFace(GLES20.GL_BACK);
		
		// Enable Blending
		GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Disable Z Buffer 
        GLES20.glDepthMask(false);
		
        /** Draw Scene**/
		// Clear scene
		GLES20.glClearColor(0.5f,0.5f,0.5f,1);
    	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);  
    	
    	// Draw sprite 
		GLES20.glUniform4fv(mColorHandle, 1, new float[]{1,1,1,1}, 0);
    	mSprite.draw();
    	
    	/** Unload opengl **/
		// Disable face culling 
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// Generate camera projection matrix for game
		Matrix.orthoM(projectionMatrix, 0, 0f, (float)width, (float)height, 0, 1.0f, -1.0f);	

		// Load opengl content 
		load();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}
	
	/**
	 * Load Shader Program 
	 * @param context
	 */
	public void loadShaderProgram(Context context){
		// Make shader program
		int vertexShader = 
				FileHelper.loadShader(
						GLES20.GL_VERTEX_SHADER,
						FileHelper.readTextFileFromRawResource(context,R.raw.vertexshader));
		int fragmentShader = 
				FileHelper.loadShader(
						GLES20.GL_FRAGMENT_SHADER,
						FileHelper.readTextFileFromRawResource(context,R.raw.fragshader));
		
		shaderProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(shaderProgram, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(shaderProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(shaderProgram);                  // create OpenGL program executables
		
		// Setup shader information
		// Add program to OpenGL environment
		GLES20.glUseProgram(shaderProgram);
		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");
		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
		mTextureHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
		mTextCordHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");
	}

}
