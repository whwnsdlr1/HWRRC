package com.RemoteMediaPlayer;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class PreviewActivity extends Activity {
	
	// 미리보기를 위한 4개의 이미지 뷰
	private ImageView videoView1 = null;
	private ImageView videoView2 = null;
	private ImageView videoView3 = null;
	private ImageView videoView4 = null;
	
	// 프레임 애니메이션을 만들기 위해 4개의 AnimationDrawabale 객체 선언
	private AnimationDrawable videoAnim1 = null;
	private AnimationDrawable videoAnim2 = null;
	private AnimationDrawable videoAnim3 = null;
	private AnimationDrawable videoAnim4 = null;
	
	private String videoPath = "";
	private File videoDirectory = null;
	
	// 액티비티가 닫힐때 bitmap 객체를 직접 메모리에서 해제하기 위해 
	// 생성된 bitmap 객체를 담는 Arraylist 생성
	private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
	
	private LinearLayout bottomPreview = null;
	
	private boolean isTwoPreview;
	
	private String ch1 = "";
	private String ch2 = "";
	private String ch3 = "";
	private String ch4 = "";
	
	private int chanNum1 = 0;
	private int chanNum2 = 0;
	private int chanNum3 = 0;
	private int chanNum4 = 0;
	
	private TextView chanName1 = null;
	private TextView chanName2 = null;
	private TextView chanName3 = null;
	private TextView chanName4 = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);
        
        // 미리보기 액티비티 기본 구조 생성        
        setPreview();
        
        String state = Environment.getExternalStorageState();
	    if ( !(Environment.MEDIA_MOUNTED.equals(state) || 
	          Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) ) {
	        return;
	    } 

	    // sdcard의 video 디렉토리에 접근
	    videoPath = Environment.getExternalStorageDirectory()+"/video/";
	       
	    videoDirectory = new File(videoPath); 
	    
	    // 액티비티 2곳 또는 4곳에 이미지파일을 이용해 애니메이션을 출력
	    setLoadFrames(videoAnim1, ch1 );
	    setLoadFrames(videoAnim2, ch2 );
	    
	    if(!isTwoPreview) {
	    	setLoadFrames(videoAnim3, ch3 );
		    setLoadFrames(videoAnim4, ch4 );
	    }
    }
    
    // 미리보기 출력을 위한 셋팅작업
    private void setPreview()
    {
    	Intent receiveIntent = getIntent();
    	
    	isTwoPreview = receiveIntent.getExtras().getBoolean("IsTwo");
    	
    	bottomPreview = (LinearLayout)findViewById(R.id.bottomPreview);
    	
    	ch1 = receiveIntent.getStringExtra("Ch1");
		ch2 = receiveIntent.getStringExtra("Ch2");
		
		chanNum1 = receiveIntent.getExtras().getInt("ChNum1");
		chanNum2 = receiveIntent.getExtras().getInt("ChNum2");
    	
    	videoView1 = (ImageView) findViewById(R.id.imageView1);
        videoView2 = (ImageView) findViewById(R.id.imageView2);
        
        chanName1 = (TextView)findViewById(R.id.chanName1);
        chanName2 = (TextView)findViewById(R.id.chanName2);
        
        videoView1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), ch1 , Toast.LENGTH_SHORT).show();
			}
		});
        
        videoView2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), ch2 , Toast.LENGTH_SHORT).show();
			}
		});
        
        videoAnim1 = new AnimationDrawable();
        videoAnim1.setOneShot(false);
        
        videoAnim2 = new AnimationDrawable();
        videoAnim2.setOneShot(false);
        
       
        // 2화면일때와 4화면 일때를 구분함
    	if(isTwoPreview) {
    		
			bottomPreview.setVisibility(View.GONE);
					
    	} else {
    		
    		videoView3 = (ImageView) findViewById(R.id.imageView3);
    	    videoView4 = (ImageView) findViewById(R.id.imageView4);
    	    
    	    chanNum3 = receiveIntent.getExtras().getInt("ChNum3");
    		chanNum4 = receiveIntent.getExtras().getInt("ChNum4");
        	           
            chanName3 = (TextView)findViewById(R.id.chanName3);
            chanName4 = (TextView)findViewById(R.id.chanName4);
    	    
    	    videoView3.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				// TODO Auto-generated method stub
    				Toast.makeText(getApplicationContext(), ch3 , Toast.LENGTH_SHORT).show();
    			}
    		});
    	    
    	    videoView4.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				// TODO Auto-generated method stub
    				Toast.makeText(getApplicationContext(), ch4 , Toast.LENGTH_SHORT).show();
    			}
    		});
    	        
    	    videoAnim3 = new AnimationDrawable();
    	    videoAnim3.setOneShot(false);
    	        
    	    videoAnim4 = new AnimationDrawable();
    	    videoAnim4.setOneShot(false);
    	    
    	    ch3 = receiveIntent.getStringExtra("Ch3");
    		ch4 = receiveIntent.getStringExtra("Ch4");
    	}
		
        
		
    }
	
    // 화면에 애니메이션 출력
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		
		 videoView1.setImageDrawable(videoAnim1);
		 videoView2.setImageDrawable(videoAnim2);
				 
		 videoAnim1.start();
		 videoAnim2.start();
		 
		 if(chanNum1 != 0)
			 chanName1.setText("CH " + chanNum1);
		 
		 if(chanNum2 != 0)
			 chanName2.setText("CH " + chanNum2);
		
		 if(!isTwoPreview)
		 {
			 videoView3.setImageDrawable(videoAnim3);
			 videoView4.setImageDrawable(videoAnim4);
			 videoAnim3.start();
			 videoAnim4.start();
			 
			 if(chanNum3 != 0)
				 chanName3.setText("CH " + chanNum3);
			 
			 if(chanNum4 != 0)
				 chanName4.setText("CH " + chanNum4);
		 }
	}

	// 각 AnimationDrawable 객체에 비트맵 객체(이미지 파일로 생성) 추가
	private void setLoadFrames(AnimationDrawable videoAnim, String channel )
	{
		 if(channel.length() == 0)
			 return;
		
		 File video = new File(videoDirectory, channel);
		 
		 if(!video.exists())	
		 {
			 Toast.makeText(getApplicationContext(), "\""+channel + "\"의 미리보기 폴더가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
			 return;
		 }
		 
		 File[] sNamelist = video.listFiles(filefilter);
		 
		 if(sNamelist.length == 0 )
		 {
			 Toast.makeText(getApplicationContext(), "\""+channel + "\"의 미리보기 이미지가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
			 return;
		 } 
		 
		 //Drawable frameFile;
		 Bitmap frameFile = null;
		 BitmapFactory.Options option = new BitmapFactory.Options();
		 
		 if(isTwoPreview)
			 option.inSampleSize = 1;
		 else
			 option.inSampleSize = 2;
		 
		 option.inDither = true;
		 option.inPurgeable = true;
		 option.inPreferredConfig = Bitmap.Config.RGB_565;
		 
		 if (sNamelist.length == 0) {
		        Log.d("Activity", "No pictures in directory.");
		        return;
		 }
		 
		 Arrays.sort(sNamelist, namesort);
		 
		 for (File filename : sNamelist) {
						 
			 frameFile = BitmapFactory.decodeFile(video.getPath() + '/' + filename.getName(), option);
			 
			 bitmaps.add(frameFile);
			 
		     Log.d("Activity", video.getPath() + '/' + filename.getName());
		     videoAnim.addFrame( new BitmapDrawable(frameFile) , 150);
		    	    
		 }
	}
	
	private FilenameFilter filefilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
        return (name.endsWith(".jpeg") || 
                name.endsWith(".jpg") || 
                name.endsWith(".png") );
        }
    };
    
    private Comparator<File> namesort = new Comparator<File>() {
		
		@Override
		public int compare(File f1, File f2) {
    		return f1.getName().compareToIgnoreCase(f2.getName());
    	}
	};

	@Override
	protected void onDestroy() {
		
		for(Bitmap bitmap : bitmaps)
		{
			bitmap.recycle();
		}
		
		super.onDestroy();
	}
	
	
}
