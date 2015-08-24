package com.RemoteMediaPlayer;

import java.util.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/**
 * ListView를 띄울 다른 액티비티
 *
 * 
 */
public class VideoListViewActivity extends Activity {

	ListView list;
	VideoListAdapter adapter;
	private int position = 0;

    /** 액티비티가 생성될때  */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        // 액티비티에 리스트뷰 생성
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        list = new ListView(this);
        
        // 어댑터 객체 생성
        adapter = new VideoListAdapter(this);
        
        // 리스트 얻어옴
		getVideoList();
        		
		list.setAdapter(adapter);
		list.setSelection(position);
		
		// 리스트 내의 아이템 선택 이벤트 설정
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				// 현재 액티비티를 종료하기 전에 선택한 position 값을 전달
				Intent resultIntent = new Intent();
				resultIntent.putExtra("VideoName", position);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
			
		});


        // 메인 레이아웃에 디스플레이함
        setContentView(list, params);
               
        
    }
    
    
    // 영상 목록 액티비티로 바뀌면 영상 목록의 리스트뷰를 얻어옴
    private void getVideoList()
    {	
    	Intent receiveIntent = getIntent();
    	ArrayList<String> movies = receiveIntent.getStringArrayListExtra("MovieList");
    	position = receiveIntent.getExtras().getInt("Position");
    	
    	boolean isPlaying = false;
    	
    	for(int i = 0 ; i < movies.size() ; i++)
    	{	
    		if( i == position) 
    			isPlaying = true;
    		else
    			isPlaying = false;
    		
    		Bitmap videoThumb = (Bitmap)receiveIntent.getParcelableExtra("Thumb"+ i);
    		adapter.addItem(new VideoItem(videoThumb, (i+1) + ". " + 
	    			movies.get(i).substring(0, movies.get(i).length()- ".mp4".length() ),isPlaying));
    	}
    	
    	
    	
    }


    
}