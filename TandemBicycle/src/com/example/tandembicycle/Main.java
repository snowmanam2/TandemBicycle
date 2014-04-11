package com.example.tandembicycle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Main extends Activity {

	private Button button1, button2, button3;
	private LinearLayout linearLayout;
	private List<Button> buttons;
	private TextView textView;
	private ImageView imageView;
	private Story story;
	private List<String> choices;
	private MediaPlayer mPlayer;
	private boolean pressed;
	public static final String PREFS_NAME = "GameSettings";
	
	/*
	 * Class: ButtonListener
	 * Purpose: Advance the story when a choice button is pressed
	 */
	protected class ButtonListener implements View.OnClickListener {
		private int index;
		public ButtonListener(int i) {
			index = i;
		}
		
		@Override
		public void onClick(View v) {
			if (!pressed) {
				System.out.println("Ignored out-of-order button press event");
				return;
			}
			
			if (choices.size() <= index){
				return;
			}
			
			story.choose(index);
			updateView();
			pressed = false;
		}
	}
	
	private void updateButtonLayout (Configuration config) {
		if (config.orientation==Configuration.ORIENTATION_LANDSCAPE) {
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		} else {
			linearLayout.setOrientation(LinearLayout.VERTICAL);
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        
        
        /* Touch event handler to prevent button race conditions
         * Valid presses are registered only on the initial touch
         */
        View.OnTouchListener tl = new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) pressed = true;
				return false;
			}
		};
		pressed = false;
		
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        
        buttons = new ArrayList<Button>();
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        
        int i = 0;
        for (i = 0; i < buttons.size(); i++){
        	Button b = buttons.get(i);
        	b.setOnClickListener(new ButtonListener(i));
        	b.setOnTouchListener(tl);
        }
        
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout1);
        
        updateButtonLayout(getResources().getConfiguration());
        
        textView = (TextView) findViewById(R.id.scenarioText);
        
        imageView = (ImageView) findViewById(R.id.imageView1);

        mPlayer = null;
        
        /* Load the story */
        String node = settings.getString("storyNode", "Beginning");
        
        InputStream is = getResources().openRawResource(R.raw.tandem_bicycle_story);
        story = new Story(is, node);
        
        updateView();

    }
    
    @Override
    public void onConfigurationChanged (Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	
    	updateButtonLayout (newConfig);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	if (mPlayer != null) mPlayer.stop();
    
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("storyNode", story.getNodeName());
    	editor.commit();
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	
    	if (mPlayer != null) mPlayer.pause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if (mPlayer != null) mPlayer.start();
    }
    
    protected void updateView () {
    	choices = story.getChoices();
    	
    	int i = 0;
    	for (i = 0; i < buttons.size(); i++) {
    		
    		Button b = buttons.get(i);
    		
    		if (i < choices.size()) {
    			b.setText(choices.get(i));
    			b.setVisibility(View.VISIBLE);
    		} else {
    			b.setText("");
    			b.setVisibility(View.INVISIBLE);
    		}
    	}
    	
    	/* Set the text */
    	textView.setText(story.getPrompt());
    	
    	/* Set the image */
    	String imageName = story.getImageName();
    	int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
    	if (imageId != 0) imageView.setImageResource(imageId);
    	
    	/* Set the music */
    	if (mPlayer != null) {
    		mPlayer.stop();
    		mPlayer.release();
    		mPlayer = null;
    	}
    	
    	String musicName = story.getMusicName();
    	
    	int musId = getResources().getIdentifier(musicName, "raw", getPackageName());
        if (musId != 0) {
        	mPlayer = MediaPlayer.create(getApplicationContext(), musId);
            mPlayer.start();
        }
    	
    }
}
