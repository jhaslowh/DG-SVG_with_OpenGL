package com.dividedgames.svg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.opengl.GLES20;

import com.dividedgames.svg.io.FileHelper;

/**
 * 
 * @author Jonathan Haslow-Hall
 */
public class Sprite {

	// OpenGL properties to draw the sprite 
	private FloatBuffer vertexBuffer = null;
	private FloatBuffer textureBuffer = null;
	private ShortBuffer indexBuffers;
	private int textureId = -1;
	private String texture = "";
	
	/** Create a new sprite **/
	public Sprite(float x, float y, float width, float height, String texture){
		this.texture = texture;
		
		// Some phones will only load textures that are a square of 2, 
		// So we need to convert the sent size to one of those sizes. 
		int texture_size = FileHelper.getSheetSize(Math.max(width, height));
		
		// Make our vertex buffer 
		// a float is 4 bytes, therefore we multiply the number of vertices with 4
		ByteBuffer vbb = ByteBuffer.allocateDirect(8*4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(new float[]{
			x,y, 		// Top left
			x, y+height, // Bottom left
			x+width, y+height, // Bottom right
			x+width, y // Top right 
		});
		vertexBuffer.position(0);
		
		// Create Texture Buffer
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(8 * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer  = byteBuf.asFloatBuffer();
		textureBuffer.put(new float[]{
			0,0,
			0,height/texture_size,
			width/texture_size,height/texture_size,
			width/texture_size,0
		});
		textureBuffer.position(0);
		
		// Create index buffer 
		ByteBuffer ibb = ByteBuffer.allocateDirect(12);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffers = ibb.asShortBuffer();
		indexBuffers.put(new short[]{
				0,1,2, // First face
				2,3,0  // Second face 
		});
		indexBuffers.position(0);
	}
	
	/** Load texture **/
	public void load(Context context){
		// Load the svg into opengl 
		textureId = FileHelper.loadTextureFromSVG(
				context.getAssets(), texture, 1.0f);
	}
	
	/** Draw the sprite to the screen **/
	public void draw(){
		/** Bind vertexes and textures uvs **/
		// Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(RenderView.mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
        		RenderView.mPositionHandle, 2,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);
        
        // Bind textures 
        GLES20.glEnableVertexAttribArray(RenderView.mTextCordHandle);
        GLES20.glVertexAttribPointer(
        		RenderView.mTextCordHandle, 2, 
        		GLES20.GL_FLOAT, false, 
        		0, textureBuffer);
        
        /** Bind texture **/
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     	// Bind the texture to this unit.
     	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureId);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(RenderView.mTextureHandle, 0);
        
        /** Draw **/

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(RenderView.mMVPMatrixHandle, 1, false, RenderView.projectionMatrix, 0);
    	// Draw the square
     	GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
     	  GLES20.GL_UNSIGNED_SHORT, this.indexBuffers);
        
        
        /** Unload **/
        GLES20.glDisableVertexAttribArray(RenderView.mPositionHandle);
        GLES20.glDisableVertexAttribArray(RenderView.mTextCordHandle);
	}
}
