package com.RemoteMediaPlayer;

import java.util.*;

import android.content.*;
import android.view.*;
import android.widget.*;

public class VideoListAdapter extends BaseAdapter {

	private Context mContext;

	private ArrayList<VideoItem> mItems = new ArrayList<VideoItem>();

	public VideoListAdapter(Context context) {
		mContext = context;
	}

	public void addItem(VideoItem it) {
		mItems.add(it);
	}

	public void setListItems(ArrayList<VideoItem> lit) {
		mItems = lit;
	}

	public int getCount() {
		return mItems.size();
	}

	public Object getItem(int position) {
		return mItems.get(position);
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		VideoView itemView;
		if (convertView == null) {
			itemView = new VideoView(mContext, mItems.get(position));
		} else {
			itemView = (VideoView) convertView;
			
			itemView.setIcon(mItems.get(position).getIcon());
			itemView.setText(mItems.get(position).getData());
			itemView.setNowPlaying(mItems.get(position).isNowPlaying());
		}

		return itemView;
	}

}
