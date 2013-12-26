package com.dividedgames.svg;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Jonathan Haslow-Hall
 */
public class MainActivity extends Activity{
	
	// The render view to draw the svg 
	private RenderView mRenderer;
	
	public MainActivity(){
		super();
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		// Create a new renderer
		mRenderer = new RenderView(this);
		// Set the render view as the default view 
		setContentView(mRenderer);
	}
}