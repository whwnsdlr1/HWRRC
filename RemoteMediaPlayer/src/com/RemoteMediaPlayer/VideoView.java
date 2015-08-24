package com.RemoteMediaPlayer;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

public class VideoView extends LinearLayout {

	/**
	 * Icon
	 */
	private ImageView mIcon;

	/**
	 * TextView 01
	 */
	private TextView mText01;
	
	private ImageView mNowPlaying;

	
	public VideoView(Context context, VideoItem aItem) {
		super(context);

		// Layout Inflation
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.listitem, this, true);

		// Set Icon
		mIcon = (ImageView) findViewById(R.id.videoThumb);
		//mIcon.setImageBitmap(aItem.getIcon());
		setIcon(aItem.getIcon());

		// Set Text 01
		mText01 = (TextView) findViewById(R.id.videoName);
		mText01.setText(aItem.getData());
		
		mNowPlaying = (ImageView)findViewById(R.id.nowPlaying);
		setNowPlaying(aItem.isNowPlaying());
		
	}

	/**
	 * set Text
	 *
	 * @param index
	 * @param data
	 */
	public void setText(String data) {
		mText01.setText(data);
	}

	/**
	 * set Icon
	 *
	 * @param icon
	 */
	public void setIcon(Bitmap icon) {
		
		if( icon != null)
			mIcon.setImageBitmap(icon);
		else
			mIcon.setImageResource(R.drawable.icon);
	}
	
	public void setNowPlaying(boolean isPlaying)
	{
		if ( isPlaying)
		{
			mNowPlaying.setVisibility(View.VISIBLE);
			mNowPlaying.setImageResource(R.drawable.bt_play_color);
		}
		else
			mNowPlaying.setVisibility(View.INVISIBLE);
		
	}

}
