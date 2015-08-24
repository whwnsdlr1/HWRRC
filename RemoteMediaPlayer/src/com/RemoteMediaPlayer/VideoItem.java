package com.RemoteMediaPlayer;

import android.graphics.*;

public class VideoItem {

	/**
	 * Icon
	 */
	private Bitmap mIcon;
	
	/**
	 * Data array
	 */
	private String mData;

	/**
	 * True if this item is selectable
	 */
	private boolean mNowPlaying = true;

	/**
	 * Initialize with icon and data array
	 * 
	 * @param icon
	 * @param obj
	 */
	public VideoItem(Bitmap icon, String obj, boolean nowPlaying) {
		mIcon = icon;
		mData = obj;
		mNowPlaying = nowPlaying;
	}

		

	public boolean isNowPlaying() {
		return mNowPlaying;
	}


	public void setNowPlaying(boolean nowPlaying) {
		mNowPlaying = nowPlaying;
	}

	/**
	 * Get data array
	 * 
	 * @return
	 */
	public String getData() {
		return mData;
	}

		
	/**
	 * Set data array
	 * 
	 * @param obj
	 */
	public void setData(String obj) {
		mData = obj;
	}

	/**
	 * Set icon
	 * 
	 * @param icon
	 */
	public void setIcon(Bitmap icon) {
		mIcon = icon;
	}

	/**
	 * Get icon
	 * 
	 * @return
	 */
	public Bitmap getIcon() {
		return mIcon;
	}

	

}
