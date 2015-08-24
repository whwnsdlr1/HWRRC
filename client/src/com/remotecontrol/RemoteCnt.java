package com.remotecontrol;

import java.util.*;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.*;
import android.view.animation.Animation.AnimationListener;
import android.widget.*;

public class RemoteCnt extends Activity implements OnClickListener{
	
	// Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECT_LOST = 6;
    

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
   
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mBluetoothService = null;
    
    private TextView mCommand = null;
    private TextView mBluetoothState = null;
    
    private ImageButton mPlayStop = null;
    private ImageButton mPrevTrack = null;
    private ImageButton mNextTrack = null;
    private ImageButton mVolumeUp = null;
    private ImageButton mVolumeDown = null;
    private ImageButton mPrevSkip = null;
    private ImageButton mNextSkip = null;
    
    private ImageButton mClose = null;
    private ImageButton mPreview = null;
    
    private boolean mIsPlaying = true;
    
    private PaintBoard mBoard;
    private ImageButton mPaintSend; 
    
    boolean[] previewItemChecked;
  
    private EditText mFindChannel = null;
    private ImageButton mSearchSend = null;
    
    //private int mPreviewListSize = 0;
    
    private ArrayList<String> mPreviewItems = new ArrayList<String>();
    
    
    
    private boolean mIsRcOpen = false;

	private Animation mTranslateLeftAnim = null;
	private Animation mTranslateRightAnim = null;

	private LinearLayout mRightController = null;
	private ImageButton mOpenRcBtn = null;
	private ImageButton mButtonClose2 = null;
	//private boolean is_pre_cmd_partition;
	
	private ArrayList<String> mFindItems = new ArrayList<String>();
    private ArrayList<Integer> mFindItemIndex = new ArrayList<Integer>();
    
    private int mSelectFindChann;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_cnt);
       
        
         mBoard = (PaintBoard) findViewById(R.id.paintBorad);
         
         mRightController = (LinearLayout) findViewById(R.id.rightController);

         mTranslateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
         mTranslateRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);

         SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
         mTranslateLeftAnim.setAnimationListener(animListener);
         mTranslateRightAnim.setAnimationListener(animListener);


         // Open Button
         mOpenRcBtn = (ImageButton) findViewById(R.id.openRcBtn);
         mOpenRcBtn.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {

         		// start animation
         		if (mIsRcOpen) {
         			mRightController.startAnimation(mTranslateRightAnim);
         			mOpenRcBtn.startAnimation(mTranslateRightAnim);
         			mButtonClose2.startAnimation(mTranslateRightAnim);
         		} else {
         			mRightController.setVisibility(View.VISIBLE);
         			mRightController.startAnimation(mTranslateLeftAnim);
         			mOpenRcBtn.startAnimation(mTranslateLeftAnim);
         			mButtonClose2.startAnimation(mTranslateLeftAnim);
         		}

         	}
         });
         
         
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        mButtonClose2 = (ImageButton)findViewById(R.id.button_close2);
        mButtonClose2.setOnClickListener( new OnClickListener() {
        	public void onClick( View v){
        		mBoard.clearVector();
        		mBoard.newImage();
        		sendMessage("close");
        	}
        });
        
        //is_pre_cmd_partition = false;
    }
    
    private class SlidingPageAnimationListener implements AnimationListener {

		public void onAnimationEnd(Animation animation) {
			if (mIsRcOpen) {
				mRightController.setVisibility(View.GONE);

				//mOpenRcBtn.setImageResource(R.drawable.left_arrow);
				mOpenRcBtn.setImageResource(R.drawable.button_left_arrow);
				mIsRcOpen = false;
			} else {
				mOpenRcBtn.setImageResource(R.drawable.button_right_arrow);
				mIsRcOpen = true;
			}
		}

		public void onAnimationRepeat(Animation animation) {

		}

		public void onAnimationStart(Animation animation) {

		}

    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mBluetoothService == null) setupCommand();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
              mBluetoothService.start();
            }
        }
    }
    
    private void setupCommand() {
    	
    	mCommand = (TextView)findViewById(R.id.commandView);
    	mBluetoothState = (TextView)findViewById(R.id.bluetoothState);
    	    
    	mPlayStop = (ImageButton)findViewById(R.id.playNstopBtn);
    	mPrevTrack = (ImageButton)findViewById(R.id.prevTrack);
    	mNextTrack = (ImageButton)findViewById(R.id.nextTrack);
    	mVolumeUp = (ImageButton)findViewById(R.id.volumeUp);
    	mVolumeDown = (ImageButton)findViewById(R.id.volumeDown);
    	mPrevSkip = (ImageButton)findViewById(R.id.prevSkip);
    	mNextSkip = (ImageButton)findViewById(R.id.nextSkip);
    	mClose = (ImageButton)findViewById(R.id.closeBtn);
    	mPreview = (ImageButton)findViewById(R.id.previewBtn);
    	
    	mPaintSend = (ImageButton)findViewById(R.id.button_radio_on);
    	
    	mFindChannel = (EditText)findViewById(R.id.findChannel);
    	mSearchSend = (ImageButton)findViewById(R.id.button_find);
    	
    	
    	mPlayStop.setOnClickListener(this);
    	mPrevTrack.setOnClickListener(this);
    	mNextTrack.setOnClickListener(this);
    	mVolumeUp.setOnClickListener(this);
    	mVolumeDown.setOnClickListener(this);
    	mPrevSkip.setOnClickListener(this);
    	mNextSkip.setOnClickListener(this);
    	mClose.setOnClickListener(this);
    	mPreview.setOnClickListener(this);
    	
    	   	
    	mPaintSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBoard.newImage();
				EditText dR = (EditText)findViewById( R.id.decisionResult);

				String CRresult = mBoard.callCR();
				mBoard.clearVector();
				if( CRresult.equals( "입력 없음") == false)
				{
					//if( is_pre_cmd_partition == true)
					//	sendMessage( "close");
					
					//if( CRresult.charAt( 0) == 'p')
					//	is_pre_cmd_partition = true;
					sendMessage( CRresult);
				}
				dR.setText( CRresult);
			}
		});
    	
    	mSearchSend.setOnClickListener(this);
    	 
        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
       // mOutStringBuffer = new StringBuffer("");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mBluetoothService != null) mBluetoothService.stop();
       
    }
    
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);

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
                    mBluetoothState.setText(R.string.title_connected_to);
                    mBluetoothState.append(mConnectedDeviceName);
                    
                    break;
                case BluetoothService.STATE_CONNECTING:
                	mBluetoothState.setText(R.string.title_connecting);
                    break;
                
                case BluetoothService.STATE_NONE:
                	mBluetoothState.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mCommand.setText("명령 :  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                
                mCommand.setText("받은 명령 :  " + readMessage);
                
                actionCommand(readMessage);
                break;
           
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "연결 됨 : "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_CONNECT_LOST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                mPreviewItems.clear();
                break;
                
                
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupCommand();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        
        }
        return false;
    }
    
    private void actionCommand(String comm)
    {
    	StringTokenizer comToken = new StringTokenizer(comm, "/");
    	String action = comToken.nextToken();
    	int findIndex = 0;
    	
    	if(comm.equals("btn_playstop")) {
    		togglePlayStopIcon();
    		return;
    	}
    	else if(comm.equals("btn_stop")) {
    		showStopIcon();
    		return;
    	}
    	else if(comm.equals("btn_play")) {
    		showPlayIcon();
    		return;
    	}
    	
    	if(action.equals("playnlist") || action.equals("stopnlist"))
    	{
    		if(action.equals("playnlist"))
    			showPlayIcon();
    		else
    			showStopIcon();
    		
    		while(comToken.hasMoreElements())
    		{
    			mPreviewItems.add(comToken.nextToken());
    		}
    		    		
    		return;
    	}
    	
    	if(action.equals("findmulti"))
    	{
    		mFindItems.clear();
    		mFindItemIndex.clear();
    		
    		while(comToken.hasMoreElements())
    		{
    			findIndex = Integer.parseInt(comToken.nextToken());
    			
    			mFindItemIndex.add(findIndex);
    			mFindItems.add(comToken.nextToken());
    		}
    		
    		
    		findChanSelectDialog();
    		
    		return;
    	}
    }
    
    private void togglePlayStopIcon()
    {
    	if(mIsPlaying)
			showPlayIcon();
		else
			showStopIcon();
	}
    
    private void showPlayIcon()
    {
    	mPlayStop.setImageResource(R.drawable.button_play);
		mIsPlaying = false;
    }
    
    private void showStopIcon()
    {
    	mPlayStop.setImageResource(R.drawable.button_stop);
		mIsPlaying = true;
    }
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.playNstopBtn:
			togglePlayStopIcon();				
			sendMessage("startnstop");
			break;
		case R.id.prevTrack:
			showStopIcon();
			sendMessage("pre");
			break;
		case R.id.nextTrack:
			showStopIcon();
			sendMessage("next");
			break;
		case R.id.volumeUp:
			sendMessage("up");
			break;
		case R.id.volumeDown:
			sendMessage("down");
			break;
		case R.id.prevSkip:
			sendMessage("preskip");
			break;
		case R.id.nextSkip:
			sendMessage("nextskip");
			break;
		case R.id.closeBtn:
			sendMessage("close");
			break;
		case R.id.previewBtn:
			previewSelectDialog();
			break;
		case R.id.button_find:
			String keyword = mFindChannel.getText().toString();
			if(keyword.length() == 0)
				Toast.makeText(getApplicationContext(),
						"검색할 단어를 입력하세요.", Toast.LENGTH_SHORT).show();
			
			sendMessage("find,"+ keyword);
			mFindChannel.setText("");
			break;
		default: break;
			
		}
	}
	
	private void previewSelectDialog()
	{
		//int listSize = 20;
		
		String[] previewList = new String[mPreviewItems.size()];
				
		previewItemChecked = new boolean[mPreviewItems.size()];
				
		for(int i = 0 ; i < previewList.length ; i++)
		{
			previewList[i] = (i+1) + ". " + mPreviewItems.get(i); 
		}
		
		
		
		new AlertDialog.Builder(this)
        .setIcon(R.drawable.remote_cnt)
        .setTitle("미리보기 채널을 선택하세요")
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
            		sendMessage("p2," + ch[0] + "," + ch[1]);
            	else
            		sendMessage("p4," + ch[0] + "," + ch[1] + ","
            					      + ch[2] + "," + ch[3] );
            	
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
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
		.setTitle("리모콘 종료")
		.setIcon(R.drawable.remote_cnt)
		.setMessage("종료 하시겠습니까?")
		.setPositiveButton("예",
		new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog,	int which) {
						// 프로세스 종료.
						/*moveTaskToBack(true);
						android.os.Process.killProcess(android.os.Process.myPid());*/
						if (mBluetoothAdapter.isEnabled()) {
					         mBluetoothAdapter.disable();
				        } 
						finish();
			}
		})
	   .setNegativeButton("아니오", null).show();
		
	}

	
	public void putString( String str)
	{
		EditText dR = (EditText)findViewById( R.id.decisionResult);
		dR.setText( str);
	}
	
	private void findChanSelectDialog()
	{	
		mSelectFindChann = -1;
		
		String[] findItems = new String[mFindItems.size()];
				
		for(int i = 0 ; i < findItems.length ; i++)
		{
			findItems[i] = ( mFindItemIndex.get(i) + 1 ) + ". " + mFindItems.get(i); 
		}
		
		
		
		new AlertDialog.Builder(this)
	    .setIcon(R.drawable.remote_cnt)
	    .setTitle("검색 완료. 원하는 채널을 선택하세요")
	    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
	
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	
	        	if(mSelectFindChann > 0)
	        		sendMessage("channel," + mSelectFindChann);
	        	
	        }
	    })
	    .setNegativeButton("취소", null)
	    .setSingleChoiceItems(findItems, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mSelectFindChann = mFindItemIndex.get(which) + 1;
			}
		})
	    .show();
	}
}
