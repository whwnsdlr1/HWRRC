//동영상 플레이어

package com.RemoteMediaPlayer;

import java.io.*;
import java.util.*;

import android.app.*;
import android.app.KeyguardManager.KeyguardLock;
import android.bluetooth.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class RemotePlayer extends Activity implements OnCompletionListener,
		 OnPreparedListener, OnBufferingUpdateListener, SurfaceHolder.Callback {
	// 동영상 디렉터리
	private static final String MEDIA_PATH = new String(Environment
			.getExternalStorageDirectory().getPath() + "/Video");

	private ArrayList<String> movies = new ArrayList<String>(); // 영상파일 목록
	private ArrayList<String> previewItem = new ArrayList<String>(); // 미리보기 영상파일 목록(확장자제거)
	
	// 영상 목록에 쓰이는 썸네일 이미지
	private ArrayList<Bitmap> movieThumbs = new ArrayList<Bitmap>();
	
	private int currentPosition = 0; // 현재 동영상 번호(채널번호)

	private MediaPlayer m_MediaPlayer; // 영상제어를 담당하는 객체
	private RemotePlayerSurfaceView m_Surface; // 영상을 띄울 서피스뷰 객체 및 서피스 홀더 객체
	private SurfaceHolder m_SurfaceHolder;

	private boolean isPaused = false; // 일시정지인지 확인하기 위한 변수
		
	private View m_Controller = null; // 플레이어 콘트롤러(재생, 정지, 앞으로, 뒤로, 시간)
	private View m_TopPanel = null; // 화면 상단바

	private long m_ControllerActionTime = 0L; // 화면에 컨트롤러가 보이고 사라지는 시간을 제는대 사용
		
	private int m_iVideoPlayTime = 0; // 동영상 플레이 길이(시간 1000/1초)
	private int m_iSkipTime = 60000; // 스킵 시간(1000=1초)
	private SeekBar m_TimeBar = null; // 볼륨바
	private SeekBar m_VolumeBar = null; // 재생바
	private ImageButton m_PlayButton = null; // 재생, 정지
	private ImageButton m_SkipPrev = null; // 뒤로
	private ImageButton m_SkipNext = null; // 앞으로
	private ProgressBar m_LoadingProgress = null; // 로딩프로그레스
	private TextView m_txtCurrentPlayTime = null; // 현제 재생시간 텍스트
	private TextView m_txtMaxPlayTime = null; // 전체 재생 시간 텍스트
	private TextView m_txtCurrentPlayTitle = null; //현재 재생중인 영상 제목

	private ImageButton m_PrevTrack = null;   // 이전 영상 버튼
	private ImageButton m_NextTrack = null;   // 다음 영상 버튼
	private ImageButton m_VideoOpen = null;   // 영상 목록 열기 버튼

	// 화면 터치시 상단바 및 하단 컨트롤 바 애니메이션 처리를 위한 객체들
	private Animation m_Anim_TopPanel_on = null;
	private Animation m_Anim_TopPanel_off = null;
	private Animation m_Anim_BottomPanel_on = null;
	private Animation m_Anim_BottomPanel_off = null;

	/*private int m_VideoWidth = 0;
	private int m_VideoHeight = 0;*/

	private int m_CurrentVolume = 0; // 현재 볼륨값
	
	private int[] m_MovieCurrentPositon; // 각 영상의 현재 위치
	
	private boolean m_First = true; // 처음인지 확인

	// BlueTooth Member Variables

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_DISCOVERABLE = 2;
	private static final int REQUEST_VIDEO_OPEN = 3;
	
	private static final int REQUEST_PREVIEW = 4;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mChatService = null;

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// 블루투스 상태 확인용 텍스트뷰
	private TextView mIncomCommand = null;
	private TextView mBlueToothState = null;
		
	private boolean mIsPanelShow; // 플레이어 컨트롤바, 상단바가 보이는지 확인하는 변수
	private boolean mIsAnotherActivity = false; // 다른 액티비티 상태인지 확인하는 변수
	
	private boolean[] previewItemChecked; // 미리보기 기능시 어느 영상이 체크되었는지 판단
	
	private boolean mIsRemoteCnt = false; // 리모콘으로 제어했는지 아닌지 판단
	
	private TextView mPlayerInfo = null; // 영상 위에 녹색 문자로 채널 및 기타 정보 표시
		
	private long mPlayerInfoViewTime = 0L; // 영상 녹색 정보 문자 사라지기 위한 시간 변수
	
	// 영상 리스트를 하나의 문자열로 보내기 위한 String 변수
	private String mMovieList = "";
	
	// 검색 완료된 채널 목록
	private ArrayList<String> mFindChannels = new ArrayList<String>();
	// 검색 완료된 채널의 인덱스
	private ArrayList<Integer>	mFindChannIndex = new ArrayList<Integer>();
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_player);
		
		// 전원키를 눌러도 잠금상태로 안가기 위한 코드
		KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
	    KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
	    lock.disableKeyguard();
	  		
		m_Surface = (RemotePlayerSurfaceView) findViewById(R.id.surface);
		m_Surface.addTapListener(onTap);

		m_SurfaceHolder = m_Surface.getHolder();
		m_SurfaceHolder.addCallback(this);
		m_SurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		m_Controller = findViewById(R.id.bottom_panel);
		m_TopPanel = findViewById(R.id.top_panel);

		m_TimeBar = (SeekBar) findViewById(R.id.timeline);
		m_TimeBar.setOnSeekBarChangeListener(onTimeBarSkip);

		m_VolumeBar = (SeekBar) findViewById(R.id.player_volume);
		m_VolumeBar.setOnSeekBarChangeListener(onVolume);

		m_LoadingProgress = (ProgressBar) findViewById(R.id.vodplayer_loading_progress);
		m_LoadingProgress.setVisibility(View.VISIBLE);

		m_txtCurrentPlayTime = (TextView) findViewById(R.id.text_playtime_current);
		m_txtMaxPlayTime = (TextView) findViewById(R.id.text_playtime_max);

		m_txtCurrentPlayTitle = (TextView) findViewById(R.id.text_current_play_title);

		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		m_CurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		m_VolumeBar.setProgress(m_CurrentVolume * 2);
		
		m_PlayButton = (ImageButton) findViewById(R.id.button_play_pause);
		m_PlayButton.setOnClickListener(onPlayPause);

		m_SkipPrev = (ImageButton) findViewById(R.id.button_play_prev);
		m_SkipPrev.setOnClickListener(onSkipPreve);

		m_SkipNext = (ImageButton) findViewById(R.id.button_play_next);
		m_SkipNext.setOnClickListener(onSkipNext);

		m_NextTrack = (ImageButton) findViewById(R.id.button_play_next_track);
		m_NextTrack.setOnClickListener(onNextTrack);

		m_PrevTrack = (ImageButton) findViewById(R.id.button_play_prev_track);
		m_PrevTrack.setOnClickListener(onPrevTrack);
		
		m_VideoOpen = (ImageButton) findViewById(R.id.button_open);
		m_VideoOpen.setOnClickListener(onVideoOpen);
		
		mPlayerInfo = (TextView)findViewById(R.id.playerInfo);
					
		m_Anim_TopPanel_on = AnimationUtils.loadAnimation(this,
				R.anim.top_panel_on);
		m_Anim_TopPanel_off = AnimationUtils.loadAnimation(this,
				R.anim.top_panel_off);
		m_Anim_BottomPanel_on = AnimationUtils.loadAnimation(this,
				R.anim.bottom_panel_on);
		m_Anim_BottomPanel_off = AnimationUtils.loadAnimation(this,
				R.anim.bottom_panel_off);

		
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// 블루투스 모듈이 없는 기기일 경우 오류 메세지를 출력
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "블루투스 지원을 안합니다.",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		updateMovieList();
		getVideoThumb.start();
		
	}
	
	// 썸네일을 얻기 위한 스레드
	private Thread getVideoThumb = new Thread(new Runnable() {
		
		@Override
		public void run() {
			Bitmap videoThumb = null;
			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inSampleSize = 4;
			option.inDither = true;
			option.inPurgeable = true;
			option.inPreferredConfig = Bitmap.Config.RGB_565;
			
			for( String movie : previewItem)
			{
				videoThumb = BitmapFactory.decodeFile(MEDIA_PATH + movie + "/00.jpg", option);
				movieThumbs.add(videoThumb);
				
			}
			
		}
	});

	// 기기안에 영상이 존재할 경우 오름차순으로 목록을 생성해준다.
	private void updateMovieList() {
		File sdcard = new File(MEDIA_PATH);
		
		if(!sdcard.exists())
		{
			Toast.makeText(getApplicationContext(), 
					"video 폴더가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
				
		File[] mp4List = sdcard.listFiles(new Mp4Filter());
		String fileName = "";
		String previewName = "";
		
		
		if (mp4List != null) {
			
			Arrays.sort(mp4List, new FileNameSort());
			
			for (File file : mp4List) {
				
				fileName = file.getName();
				movies.add(fileName);
				previewName = fileName.substring(0, fileName.length() - ".mp4".length());
				previewItem.add(previewName);
				
				mMovieList = mMovieList + "/" + previewName ;
			}
			
		} else {
			Toast.makeText(getApplicationContext(), 
					"영상이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		m_MovieCurrentPositon = new int[movies.size()];
	}
	
	// 파일들을 오름차순으로 정렬하기 위한 클래스
	private class FileNameSort implements Comparator<File> {
    	public int compare(File f1, File f2) {
    		return f1.getName().compareToIgnoreCase(f2.getName());
    	}
    }

	// mp4 또는 wmv 확장자를 가진 파일만 찾기 위한 클래스
	// 다른 확장자 사용시 리턴값 뒤에 추가
	private class Mp4Filter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String filename) {

			return (filename.endsWith(".mp4") || filename.endsWith(".wmv"));
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(m_First)	enableBluetooth();
	}

	private void enableBluetooth()
	{
		// If BT is not on, request that it be enabled.
				
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the command session
		} else {
			if (mChatService == null)
				setupCommand();
		}
	}
	private void setupCommand() {
		mIncomCommand = (TextView) findViewById(R.id.command_text);
		mBlueToothState = (TextView) findViewById(R.id.bluetooth_state);
			
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothService(this, mHandler);

	}

	@Override
	protected synchronized void onResume() {

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}

		isPaused = false;
		
		// onResume 함수 실행이 처음이 아닐 경우(액티비티 전환시 다시 불릴때) 
		if( m_First != true)
		{
			mIsAnotherActivity = false;
			m_MediaPlayer.start();
			m_PlayButton.setImageResource(R.drawable.button_stop);
			if(mBluetoothAdapter.isEnabled())
				sendMessageBT("btn_stop");
		}
				
		m_Surface.postDelayed(onEverySecond, 1000);

		super.onResume();
	}

	@Override
	protected synchronized void onPause() {
		isPaused = true;
		
		if (m_MediaPlayer != null)
			m_MediaPlayer.pause();
		
		m_First = false;
		
		mIsAnotherActivity = true;
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {

		if (mChatService != null)
			mChatService.stop();

		if (m_MediaPlayer != null) {
			m_MediaPlayer.release();
			m_MediaPlayer = null;
			
		}
		m_Surface.removeTapListener(onTap);
		
		super.onDestroy();
	}

	// 각 영상이 완료될 경우 위치 리셋
	public void onCompletion(MediaPlayer arg0) {
		
		m_MovieCurrentPositon[currentPosition] = 0;
		
		nextTrack();

	}

	// 동영상 재생준비 완료
	public void onPrepared(MediaPlayer mediaplayer) {
		
		m_iVideoPlayTime = mediaplayer.getDuration();
		
		// 기기 중 간혹 영상 준비도 되기전에 onPrepared로 들어오는 경우가 있음( 갤럭시탭 10.1)
		// 이럴 경우 m_iVideoPlayTime 값이 0이 되니 0일 경우 다시한번 플레이 시킴
		if(m_iVideoPlayTime == 0)
		{
			playVideo(MEDIA_PATH + movies.get(currentPosition));
			return;
		}
		
		// 기기 해상도를 받아 너비와 높이 값을 구함
		Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
		
		/*int width = m_MediaPlayer.getVideoWidth();
		int height = m_MediaPlayer.getVideoHeight();*/
		
		m_SurfaceHolder.setFixedSize(width, height);

		m_TimeBar.setMax(m_iVideoPlayTime);
		m_TimeBar.setProgress(m_MovieCurrentPositon[currentPosition]);
		m_TimeBar.setSecondaryProgress(m_MovieCurrentPositon[currentPosition]);
		mediaplayer.seekTo(m_MovieCurrentPositon[currentPosition]);
		SetTextTime(m_txtMaxPlayTime, m_iVideoPlayTime);
		
		// 재생중이 아니라면 영상을 재생한다.
		if(!mediaplayer.isPlaying())
		{
			mediaplayer.start();
			m_PlayButton.setImageResource(R.drawable.button_stop);
		}
		
		// 컨트롤바, 녹색정보 문자 사라지는 시간 지정하기 위해 현재시간을 저장
		m_ControllerActionTime = SystemClock.elapsedRealtime();
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		
		showPanels();
				
		m_LoadingProgress.setVisibility(View.GONE);
		m_txtCurrentPlayTitle.setText( (currentPosition + 1)	+ ". "
				+ previewItem.get(currentPosition));
		
		mPlayerInfo.setText("[채널 "+ (currentPosition + 1) + "] " 
				+ previewItem.get(currentPosition) );
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {

	}

	// 액티비티 전환할때 현재 재생중인 영상의 위치를 저장해놓는다.
	// 다시 복귀할때 이 값을 이용하여 다시 재생.
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		m_MovieCurrentPositon[currentPosition] = m_MediaPlayer.getCurrentPosition();
	}

	// 재생준비 완료
	public void surfaceCreated(SurfaceHolder holder) {
			
		playVideo(MEDIA_PATH + movies.get(currentPosition));

		m_Controller.setVisibility(View.GONE);
		m_TopPanel.setVisibility(View.GONE);
	}
	
	
	// 영상 재생
	private void playVideo(String url) {
		try {
			if (m_MediaPlayer == null) {
				m_MediaPlayer = new MediaPlayer();
				
				m_MediaPlayer.setOnPreparedListener(this);
				m_MediaPlayer.setOnCompletionListener(this);
				//m_MediaPlayer.setOnVideoSizeChangedListener(this);
				m_MediaPlayer.setOnBufferingUpdateListener(this);
							
				m_MediaPlayer.setScreenOnWhilePlaying(true);
				
				
			} else {
				m_MediaPlayer.stop();
				m_MediaPlayer.reset();

			}

			m_MediaPlayer.setDisplay(m_SurfaceHolder);
			m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			m_MediaPlayer.setDataSource(url);
			m_MediaPlayer.prepareAsync();
						
			m_LoadingProgress.setVisibility(View.VISIBLE);
			

		} catch (Throwable t) {
		}
	}

	// 재생기의 컨트롤바, 상단바를 감춰줌
	private void clearPanels() {
		mIsPanelShow = false;
		
		m_Controller.startAnimation(m_Anim_BottomPanel_off);
		m_TopPanel.startAnimation(m_Anim_TopPanel_off);
		m_Controller.setVisibility(View.GONE);
		m_TopPanel.setVisibility(View.GONE);
	}
	
	// 재생기의 컨트롤바, 상단바를 표시해줌
	private void showPanels() {
		
		if(!mIsRemoteCnt) {
			mIsPanelShow = true;
			m_Controller.startAnimation(m_Anim_BottomPanel_on);
			m_TopPanel.startAnimation(m_Anim_TopPanel_on);
			m_Controller.setVisibility(View.VISIBLE);
			m_TopPanel.setVisibility(View.VISIBLE);
		}
		mIsRemoteCnt = false;
	}

	// 전체 재생시간 텍스트 표시
	private void SetTextTime(TextView view, int time) {
		int hour = time / 3600000; // 시간
		time %= 3600000;
		int minute = time / 60000; // 분
		time %= 60000;
		int second = time / 1000; // 초

		String h = (hour < 10) ? "0" + Integer.toString(hour) : Integer
				.toString(hour);
		String m = (minute < 10) ? "0" + Integer.toString(minute) : Integer
				.toString(minute);
		String s = (second < 10) ? "0" + Integer.toString(second) : Integer
				.toString(second);
		view.setText(h + ":" + m + ":" + s);
	}

	// 화면 터치했을 경우
	private RemotePlayerSurfaceView.TapListener onTap = new RemotePlayerSurfaceView.TapListener() {
		public void onTap(MotionEvent event) {
			m_ControllerActionTime = SystemClock.elapsedRealtime();
			if (m_Controller.getVisibility() == View.VISIBLE) // 컨트롤러 보이는지 확인
				clearPanels(); // 메뉴 숨기기
			else {
							
				showPanels(); // 메뉴 보이기
			}
		}
	};

	// 매초마다 정보 갱신을 위한 스레드
	private Runnable onEverySecond = new Runnable() {
		public void run() {
			// 컨트롤러는 일정 시간후 사라지게 (1000=1초)
			if (m_ControllerActionTime > 0
					&& SystemClock.elapsedRealtime() - m_ControllerActionTime > 5000)
			{
				if( mIsPanelShow)
					clearPanels();
								
			}
			// 녹색 문자 정보도 일정시간후 사라지게 함
			if (mPlayerInfoViewTime > 0
					&& SystemClock.elapsedRealtime() - mPlayerInfoViewTime > 5000)
			{
				mPlayerInfo.setText("");
			}
			
			if (m_MediaPlayer != null) {
				int time = m_MediaPlayer.getCurrentPosition();
				m_TimeBar.setProgress(time);
				SetTextTime(m_txtCurrentPlayTime, time);
			}
			if (!isPaused)
				m_Surface.postDelayed(onEverySecond, 1000);
		}
	};
	
	
	// 플레이 및 일시정지 기능
	private void playPause() {
		
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		m_ControllerActionTime = SystemClock.elapsedRealtime();

		if (m_MediaPlayer != null) {
			if (m_MediaPlayer.isPlaying())// 재생 중이면
			{
				m_PlayButton.setImageResource(R.drawable.button_play);
				m_MediaPlayer.pause();
				mPlayerInfo.setText("일시 정지");
			} else // 재생중이지 않으면
			{
				m_PlayButton.setImageResource(R.drawable.button_stop);
				m_MediaPlayer.start();
				mPlayerInfo.setText("재생");
			}
		}
	}

	// 재생, 정지 버튼
	private View.OnClickListener onPlayPause = new View.OnClickListener() {
		public void onClick(View v) {
			playPause();
			
			if(mBluetoothAdapter.isEnabled())
				sendMessageBT("btn_playstop");
		}
	};
	
	// 일정시간 이전으로 스킵
	private void skipPrev()
	{
		m_ControllerActionTime = SystemClock.elapsedRealtime();
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		
		int time = m_MediaPlayer.getCurrentPosition() - m_iSkipTime;
		if (time < 0)
			time = 0;
		m_MediaPlayer.seekTo(time);
		mPlayerInfo.setText("1분 전으로 이동");
				
	}
	// 이전으로 스킵 버튼 이벤트
	private View.OnClickListener onSkipPreve = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			skipPrev();
		}
	};
	
	// 일정시간 이후로 스킵
	private void skipNext()
	{
		m_ControllerActionTime = SystemClock.elapsedRealtime();
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		
		int time = m_MediaPlayer.getCurrentPosition() + m_iSkipTime;
		if (time > m_iVideoPlayTime)
			time = m_iVideoPlayTime;
		m_MediaPlayer.seekTo(time);
		
		mPlayerInfo.setText("1분 후로 이동");
		
	}

	// 이후로 스킵버튼 이벤트
	private View.OnClickListener onSkipNext = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			skipNext();
		}
	};

	// 이전 영상 버튼 이벤트
	private View.OnClickListener onPrevTrack = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			prevTrack();
			
			if(mBluetoothAdapter.isEnabled())
				sendMessageBT("btn_stop");
		}
	};
    
	// 이전 영상으로
	private void prevTrack() {
		
		// m_MovieCurrentPositon = 0;
		
		m_MovieCurrentPositon[currentPosition] = m_MediaPlayer.getCurrentPosition();
		
		if (--currentPosition < 0) {
			currentPosition = movies.size() - 1;
		}

		playVideo(MEDIA_PATH + movies.get(currentPosition));
		//clearPanels();

	}
	
	// 다음 영상 버튼 이벤트
	private View.OnClickListener onNextTrack = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			nextTrack();
			
			if(mBluetoothAdapter.isEnabled())
				sendMessageBT("btn_stop");
		}
	};
	
	// 다음 영상으로
	private void nextTrack() {
		
		//m_MovieCurrentPositon = 0;
		
		m_MovieCurrentPositon[currentPosition] = m_MediaPlayer.getCurrentPosition();
		
		if (++currentPosition >= movies.size()) {
			currentPosition = 0;
		}

		playVideo(MEDIA_PATH + movies.get(currentPosition));
		//clearPanels();

	}
	
	// 영상 목록 열기 버튼
	private View.OnClickListener onVideoOpen = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 videoOpen();
		}
	};
	
	// 영상 목록을 다른 액티비티를 통해 띄움
	private void videoOpen()
	{
		Intent intent = new Intent(getApplicationContext(), VideoListViewActivity.class);
		intent.putExtra("MovieList", movies);
		intent.putExtra("Position", currentPosition);
		
		for(int i = 0 ; i< movieThumbs.size();i++)
		{
			intent.putExtra("Thumb"+ i, movieThumbs.get(i));
		}
		
		startActivityForResult(intent, REQUEST_VIDEO_OPEN);
	}

	// 플레이 타임바 (화면 상단바)
	private SeekBar.OnSeekBarChangeListener onTimeBarSkip = new SeekBar.OnSeekBarChangeListener() {
		
		// 시크바 위치가 변경되었을때 이벤트 처리
		@Override
		public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {
			if (flag) {
				m_ControllerActionTime = SystemClock.elapsedRealtime();
				m_MediaPlayer.seekTo(i);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekbar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {
		}
	};
	
	// 볼륨값을 한단계씩 증가
	private void setVolumeUp() {
		
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		m_ControllerActionTime = SystemClock.elapsedRealtime();
		
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if (m_CurrentVolume < maxVolume) {

			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					++m_CurrentVolume, AudioManager.FLAG_PLAY_SOUND);

			m_VolumeBar.setProgress(m_CurrentVolume * 2);
			mPlayerInfo.setText("음량 " + m_CurrentVolume);
		}
		
		
	}
	
	// 볼륨 값을 한단계씩 감소
	private void setVolumeDown() {
		
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		m_ControllerActionTime = SystemClock.elapsedRealtime();
		
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		if (m_CurrentVolume > 0) {

			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					--m_CurrentVolume, AudioManager.FLAG_PLAY_SOUND);

			m_VolumeBar.setProgress(m_CurrentVolume * 2);
			mPlayerInfo.setText("음량 " + m_CurrentVolume);
		}
			
		
	}
	
	// 임의 값으로 볼륨 값을 지정 ( 0 부터 15 사이 값 지정)
	private void setVolume(int vol) {
		
		mPlayerInfoViewTime = SystemClock.elapsedRealtime();
		m_ControllerActionTime = SystemClock.elapsedRealtime();
		
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		if (vol >= 0 && vol <= maxVolume) {

			m_CurrentVolume = vol;

			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					m_CurrentVolume, AudioManager.FLAG_PLAY_SOUND);

			m_VolumeBar.setProgress(m_CurrentVolume * 2);
			mPlayerInfo.setText("음량 " + m_CurrentVolume);
		} else {
			Toast.makeText(getApplicationContext(), "볼륨값이 잘못 설정되었습니다.",
					Toast.LENGTH_SHORT).show();
		}
		
		
	}

	// 볼륨바 조절
	private SeekBar.OnSeekBarChangeListener onVolume = new SeekBar.OnSeekBarChangeListener() {
		
		// SeekBar 변경시 볼륨값 조절
		@Override
		public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {
			if (flag) {
				mPlayerInfoViewTime = SystemClock.elapsedRealtime();
				m_ControllerActionTime = SystemClock.elapsedRealtime();
				AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

				m_CurrentVolume = i / 2;
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						m_CurrentVolume, AudioManager.FLAG_PLAY_SOUND);
				
				mPlayerInfo.setText("음량 " + m_CurrentVolume);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekbar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {
		}
	};

	// 버퍼링 표시 ( 파일 재생이므로 없어도 됨)
	@Override
	public void onBufferingUpdate(MediaPlayer mediaplayer, int i) {
		// i=100%로 넘어옴
		// 퍼센트지로 표시
		m_TimeBar.setSecondaryProgress((m_iVideoPlayTime / 100) * i);
	}

	// 안드로이드 기기의 물리버튼 설정
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		// 하드웨어 뒤로가기 버튼에 따른 이벤트 설정
		{
			// 기본 액티비티 일 경우 종료 메세지 다이얼로그 띄운후 종료
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.player_icon)
					.setTitle("플레이어 종료")
					.setMessage("종료 하시겠습니까?")
					.setPositiveButton("예",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,	int which) {
									// 프로세스 종료.
									
								if (mBluetoothAdapter.isEnabled()) {
							         mBluetoothAdapter.disable();
						        } 
						
								moveTaskToBack(true);
								android.os.Process.killProcess(android.os.Process.myPid());
						}
					})
				   .setNegativeButton("아니오", null).show();

			return true;
		} else if ( keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			//볼륨 증가 키 일 경우
			setVolumeUp();
			return true;
		} else if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			// 볼륨 감소 키 일 경우
			setVolumeDown();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	/*@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int width, int height) {
		// TODO Auto-generated method stub
		m_VideoWidth = width;
		m_VideoHeight = height;
	}*/
	
	// 리모콘을 통해 받은 명령 문자열(String형)을 가지고 명령어 수행
	private void actionCommand(String command) {
		
		// 명령 뒤에 옵션 값이 있으면 , 로 구분
		StringTokenizer comToken = new StringTokenizer(command, ",");
		
		// 미리보기 채널 값을 저장할 변수
		int[] ch = { 0,0,0,0};
		int tmp = 0;
		String action = comToken.nextToken();
		
		// 다른 액티비티 상태일 경우는 명령어를 받아도 수행되지 않음
		if(!mIsAnotherActivity){
			
			// 단일 명령어 일 경우 각 명령어들 수행
			if (command.equals("startnstop")) {
				playPause();
				return;
			} else if (command.equals("pre")) {
				mIsRemoteCnt = true;
				prevTrack();
				return;
			} else if (command.equals("next")) {
				mIsRemoteCnt = true;
				nextTrack();
				return;
			} else if (command.equals("up")) {
				setVolumeUp();
				return;
			} else if (command.equals("down")) {
				setVolumeDown();
				return;
			} else if (command.equals("preskip")) {
				skipPrev();
				return;
			} else if (command.equals("nextskip")) {
				skipNext();
				return;
			} else if (command.equals("p2")) {
				preview(1, 2);
				return;
			} else if (command.equals("p4")) {
				preview(1, 2, 3, 4);
				return;
			}
					
			// 명령어 뒤에 특정 옵션 값이 추가될 경우
			if (action.equals("volume")) {
				
				int vol = 0;
				
				try{
					vol = Integer.parseInt(comToken.nextToken());
				}catch (NumberFormatException e) {
					vol = m_CurrentVolume;
					Toast.makeText(getApplicationContext(), "잘못된 볼륨값입니다.",
							Toast.LENGTH_SHORT).show();
					return;
				}
							
				setVolume(vol);
				return;
			} else if (action.equals("channel")) {

				int pos = 0;
				
				try {
					pos = Integer.parseInt(comToken.nextToken()) - 1;
				} catch (NumberFormatException e) {
					pos = currentPosition;
					Toast.makeText(getApplicationContext(), "잘못된 채널입니다.", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				setChannel(pos);
				return;
			} else if (action.equals("find")) {
				if(comToken.hasMoreElements())
					findChannel(comToken.nextToken());
				else
					Toast.makeText(getApplicationContext(), "검색어를 입력하세요.", 
							Toast.LENGTH_SHORT).show();
				return;
			} else if (action.equals("p2")) {
				
				int idx = 0;
				while(comToken.hasMoreElements())
				{	
					try{
						tmp = Integer.parseInt(comToken.nextToken());
					}catch (NumberFormatException e) {
						tmp = 0;
					}
										
					if(tmp > 0 && tmp <= movies.size())
						ch[idx] = tmp;
					
					idx++;
				}	
				preview(ch[0], ch[1]);
				return;
			} else if (action.equals("p4")) {
				
				int idx = 0;
				while(comToken.hasMoreElements())
				{	
					try{
						tmp = Integer.parseInt(comToken.nextToken());
					}catch (NumberFormatException e) {
						tmp = 0;
					}
					
					if(tmp > 0 && tmp <= movies.size())
						ch[idx] = tmp;
					
					idx++;
				}	
				preview(ch[0], ch[1], ch[2], ch[3]);
				return;
			}
		}
		
		// 닫기 버튼 눌렀을시 처리
		if (command.equals("close")) {
			closeAnotherActivity();
			return;
		}

	}

	// 검색어로 채널 찾기
	private void findChannel(String str) {
		int position = 0;
		
		String findChannels = "";
		String lowerStr = str.toLowerCase();
		mFindChannels.clear();
		mFindChannIndex.clear();
		
		for (String movieName : movies) {
			
			movieName = movieName.toLowerCase();
			if (SoundSearcher.matchString(movieName, lowerStr)) {
				
				mFindChannels.add(movieName);
				mFindChannIndex.add(position);
				
			}

			position++;
		}
		
		// 검색된 영상이 하나면 바로 재생
		if(mFindChannels.size() == 1 )
		{
			mIsRemoteCnt = true;
			m_MovieCurrentPositon[currentPosition] = m_MediaPlayer.getCurrentPosition();
			currentPosition = mFindChannIndex.get(0);
			playVideo(MEDIA_PATH + movies.get(currentPosition));
			
			return;
		}
		
		// 검색된 영상이 2개 이상일 경우 String 하나로 묶어서 리모콘쪽으로 전송
		if(mFindChannels.size() > 1)
		{
			for(int i = 0 ; i < mFindChannels.size() ; i++)
			{
				findChannels = findChannels + "/"+ mFindChannIndex.get(i) + "/" + mFindChannels.get(i) ; 
			}
			
			sendMessageBT("findmulti"+ findChannels);
			return;
		}

		Toast.makeText(getApplicationContext(), "찾는 채널이 없습니다.",
				Toast.LENGTH_SHORT).show();
	}

	// 특정(채널) 영상으로 이동
	private void setChannel(int position) {
		if (position >= 0 && position < movies.size()) {
			//m_MovieCurrentPositon = 0;
			mIsRemoteCnt = true;
			m_MovieCurrentPositon[currentPosition] = m_MediaPlayer.getCurrentPosition();
			
			currentPosition = position;
			playVideo(MEDIA_PATH + movies.get(currentPosition));
		} else
			Toast.makeText(getApplicationContext(), "잘못된 채널입니다.",
					Toast.LENGTH_SHORT).show();
	}

	// 다른 액티비티가 종료 되고 전달된 값을 통해 결과 수행
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		
		// 영상 목록 액티비티에서 특정 영상을 선택했을 경우 액티비티가 종료되고 채널 번호를 전달
		case REQUEST_VIDEO_OPEN:
			if( resultCode == Activity.RESULT_OK)
			{
										
				currentPosition = data.getExtras().getInt("VideoName");
				m_MovieCurrentPositon[currentPosition] = 0;
						
				//clearPanels();
			}
			
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				Toast.makeText(this, R.string.bluetooth_on, Toast.LENGTH_SHORT)
						.show();
				setupCommand();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_LONG).show();
				//finish();
			}
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mBlueToothState.setText(R.string.title_connected_to);
					mBlueToothState.append(mConnectedDeviceName);

					break;
				case BluetoothService.STATE_CONNECTING:
					mBlueToothState.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					mBlueToothState.setText(R.string.title_not_connected);
					break;
				}
				break;

			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mIncomCommand.setText("명령 : "+ readMessage);
				
				actionCommand(readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"연결 됨 : " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				
				if(m_MediaPlayer.isPlaying())
					sendMessageBT("stopnlist"+ mMovieList);
				else
					sendMessageBT("playnlist"+ mMovieList);
				
					
				
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	// 옵션메뉴 생성
	// res/menu/option_menu.xml 참조
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}
	
	// 옵션메뉴의 항목이 선택되었을때 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// 다른 기기서 블루투스 검색 가능하게 만들어줌
		if (item.getItemId() == R.id.discoverable) {
			ensureDiscoverable();
			return true;
		}else if (item.getItemId() == R.id.bluetooth_on) { // 블루투스를 수동으로 키는 항목
			
			if(!mBluetoothAdapter.isEnabled())
				enableBluetooth();
			else
				Toast.makeText(getApplicationContext(),"블루투스가 이미 켜져 있습니다.",Toast.LENGTH_SHORT).show();
			return true;
		}else if (item.getItemId() == R.id.previewSelect) { // 미리보기 대화창을 연다.
			
			previewSelectDialog();
			return true;
		}
		

		return false;

	}

	// 다른 기기(리모콘) 해당 플레이어를 검색할 수 있도록 플레이어에서 검색가능한 상태로 전환
	private void ensureDiscoverable() {
		
		if(!mBluetoothAdapter.isEnabled())
		{
			Toast.makeText(getApplicationContext(),
					"블루투스가 비활성화된 상태입니다. 먼저 활성화 해주세요.", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
		}
	}
	
	 /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessageBT(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

           
        }
    }
    
    
    // 미리보기 액티비티로 전환 ( 2화면)	
	private void preview(int ch1, int ch2)
	{
		Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
		
		intent.putExtra("IsTwo", true);
		intent.putExtra("Ch1", (ch1 == 0)? "":previewItem.get(ch1-1));
		intent.putExtra("Ch2", (ch2 == 0)? "":previewItem.get(ch2-1));
		intent.putExtra("ChNum1", ch1);
		intent.putExtra("ChNum2", ch2);
		
		startActivityForResult(intent, REQUEST_PREVIEW);
			
	}
	
	// 미리보기 액티비티로 전환 (4화면)
	private void preview(int ch1, int ch2, int ch3, int ch4)
	{
		Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
		
		intent.putExtra("IsTwo", false);
		
		intent.putExtra("Ch1", (ch1 == 0)? "":previewItem.get(ch1-1));
		intent.putExtra("Ch2", (ch2 == 0)? "":previewItem.get(ch2-1));
		intent.putExtra("Ch3", (ch3 == 0)? "":previewItem.get(ch3-1));
		intent.putExtra("Ch4", (ch4 == 0)? "":previewItem.get(ch4-1));
		
		intent.putExtra("ChNum1", ch1);
		intent.putExtra("ChNum2", ch2);
		intent.putExtra("ChNum3", ch3);
		intent.putExtra("ChNum4", ch4);
				
		startActivityForResult(intent, REQUEST_PREVIEW);
		
		
	}
	
	// 기본 액티비티(영상 재생일때)에서 다른 액티비티를 닫는다.
	private void closeAnotherActivity()
	{
		mIsRemoteCnt = true;
		finishActivity(REQUEST_VIDEO_OPEN);
		finishActivity(REQUEST_PREVIEW);
	}
	
	// 미리보기시 선택할 채널을 체크 가능하도록 다이얼로그를 생성
	private void previewSelectDialog()
	{
		int listSize = previewItem.size();
		
		String[] previewList = new String[listSize];
				
		previewItemChecked = new boolean[listSize];
				
		for(int i = 0 ; i < previewList.length ; i++)
		{
			previewList[i] = (i+1) + ". " + previewItem.get(i); 
		}
		
		
		
		new AlertDialog.Builder(this)
        .setIcon(R.drawable.player_icon)
        .setTitle("미리보기할 영상을 선택하세요")
        .setPositiveButton("확인", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	int[] ch = {0,0,0,0};
            	int sel = 0;
            	
            	for(int i = 0 ; i < previewItemChecked.length; i++)
            	{
            		if(previewItemChecked[i] && (sel < ch.length) )
            		{
            			ch[sel] = i+1;
            			sel++;
            		}
                       		           			
            	}
            	
            	if(sel == 0) return;
            	
            	if( sel <= 2)
            		preview(ch[0], ch[1] );
            	else
            		preview(ch[0], ch[1], ch[2], ch[3]);
            }
        })
        .setNegativeButton("취소", null)
        .setMultiChoiceItems(previewList, previewItemChecked, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                
            }
        })
        .show();
	}
	
}
