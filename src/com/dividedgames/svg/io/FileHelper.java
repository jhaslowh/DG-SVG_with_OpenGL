package com.dividedgames.svg.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import svg.android.SVG;
import svg.android.SVGParseException;
import svg.android.SVGParser;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.PictureDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * 
 * @author Jonathan Haslow-Hall
 */
public class FileHelper {
	/**
     * Load and compile a shader file
     * @param type shader type
     * @param shaderCode string containing shader code
     * @return shader handle
     */
    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    /**
	 * Reads the contents of a resource file and adds them into a string.
	 * @param context Android context of the app
	 * @param resourceId the resource id of the file to load
	 * @return a string containing the contents of the file
	 */
    public static String readTextFileFromRawResource(final Context context,
            final int resourceId)
    {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);
 
        String nextLine;
        final StringBuilder body = new StringBuilder();
 
        try
        {
            while ((nextLine = bufferedReader.readLine()) != null)
            {
                body.append(nextLine);
                body.append(' ');
            }
        }
        catch (IOException e)
        {
            return null;
        }
 
        return body.toString();
    }
    
    /**
     * Loads a texture from a svg asset files
     * @param assets app assets 
     * @param asset the asset location
     * @param size the size of the bitmap the sheet was put it. This is a return value not a set value
     * @param scale the desired scale of the drawable
     * @return The texture id of the bitmap for use by OpenGL bindTexture()
     */
    public static int loadTextureFromSVG(AssetManager assets, String asset, float scale){
    	int textureId = -1;
    	try {
    		
    		// Load the SVG
			SVG svg = SVGParser.getSVGFromAsset(assets, asset);
			
			// Convert the SVG to a bitmap
			Bitmap bitmap = FileHelper.pictureDrawable2Bitmap(svg.getPicture(), scale);
			
			// Release and delete svg
			svg.release();
			svg = null;
			
			// Load the bitmap
			textureId = FileHelper.loadTexture(bitmap);
			
		} catch (SVGParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return textureId;
    }
    
    /**
     * Converts a drawable to a bitmap. 
     * @param picture the drawable
     * @param scale the desired scale of the drawable
     * @return the bitmap containing the drawable data
     */
    public static Bitmap pictureDrawable2Bitmap(Picture picture, float scale){
    	// Get the scalable picture drawable
        PictureDrawable pictureDrawable = new PictureDrawable(picture);
        
    	// Target 
    	float target = Math.max(
    			pictureDrawable.getIntrinsicWidth() * scale,
    			pictureDrawable.getIntrinsicHeight() * scale);

        // Starting width
    	int sheet_width = getSheetSize(target);
    	
    	// Convert the bitmap
        Bitmap bitmap = Bitmap.createBitmap(sheet_width, sheet_width, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pictureDrawable.getPicture(), new RectF(0f,0f,
        		(float)pictureDrawable.getIntrinsicWidth() * scale, 
        		(float)pictureDrawable.getIntrinsicHeight() * scale));
        
        // Delete Canvas
        canvas = null;
        pictureDrawable = null;
        
        return bitmap;
    }
    
    /**
	 * Load texture to opengl, will recycle the bitmap 
	 * @param game
	 * @param bitmap
	 * @param recycle
	 * @return
	 */
	public static int loadTexture(Bitmap bitmap)
    {
        int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
     
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            
            // Reset Bound Texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
     
        if (textureHandle[0] == 0)
        {
        	throw new RuntimeException("Error loading texture.");
        }
        
    	// Recycle the bitmap, since its data has been loaded into OpenGL.
    	bitmap.recycle();
    	bitmap = null;
     
        return textureHandle[0];
    }
	

    /**
     * Get the sheet size as a value of 2^n
     * @param target
     * @return
     */
    public static int getSheetSize(float target){
    	int sheet_width = 16;
    	
    	// Find the smallest width that is an element of 2^x 
    	// that fits the image. The width must be an element of 
    	// 2^x or it will not be loaded by opengl.
    	while (sheet_width < target){
    		sheet_width *= 2;
    	}
    	
    	return sheet_width;
    }
}
