package com.example.blutooth_voice02;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.WindowManager;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;
import static android.media.AudioManager.STREAM_MUSIC;

public class MainActivity extends AppCompatActivity
{
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    public static final int MSG_VOICE_RECO_SONG = 1;
    public static final int MSG_VOICE_RECO_START = 2;
    public static final int MSG_VOICE_RECO_HARU = 3;
    private long time= 0;

    Random rnd;
    int k=0;
    int shock=0;
    int mew, shy, song, angry;

    private TextView mConnectionStatus;
    TextView mtvHaru;
    TextView mtvNotData;
    TextView mtvReciveData;
    TextView mtvConnecting;
    TextView tvname;
    ImageView Face;
    ImageView Empty;
    Handler mHdrVoiceRecoState;
    MediaPlayer mpmew;
    AudioManager mAudioManager;
    AnimationDrawable ani;
    DigitalClock dclock;

    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    static boolean isConnectionError = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
        }
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        mtvNotData = (TextView)findViewById(R.id.tvnotdata);
        mtvReciveData = (TextView)findViewById(R.id.tvrecivedata);
        mtvHaru = (TextView)findViewById(R.id.tvHaru);
        tvname = (TextView)findViewById(R.id.tvname);
        mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);
        mtvConnecting = (TextView)findViewById(R.id.connecting);
        Face = (ImageView)findViewById(R.id.imgFace);
        Empty = (ImageView)findViewById(R.id.imgempty);
        dclock = (DigitalClock)findViewById(R.id.dclock);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        ani = (AnimationDrawable)Face.getDrawable();
        rnd = new Random();

        mpmew = MediaPlayer.create(MainActivity.this,R.raw.mew6);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("블루투스를 지원하지 않는 기기 입니다.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        }
        else {
            showPairedDevicesListDialog();
        }

        mHdrVoiceRecoState = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_VOICE_RECO_SONG: {
                        song=rnd.nextInt(2);
                        if(song==0){
                            mConnectedTask.write("l\n");
                            sing_nabi();
                        } else{
                            mConnectedTask.write("k\n");
                            sing_nyang();
                        }
                        break;
                    }
                    case MSG_VOICE_RECO_HARU: {
                        mAudioManager.setStreamVolume(STREAM_MUSIC,0,FLAG_REMOVE_SOUND_AND_VIBRATE);
                        mpmew.stop();
                        mpmew.reset();
                        inputVoiceHaru(mtvHaru);
                        break;
                    }
                    case MSG_VOICE_RECO_START: {
                        mAudioManager.setStreamVolume(STREAM_MUSIC,0,FLAG_REMOVE_SOUND_AND_VIBRATE);
                        mpmew.stop();
                        mpmew.reset();
                        inputVoice(mtvNotData);
                        break;
                    }
                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            toast("취소 버튼을 한번 더 누르시면 종료됩니다.");
        }else if(System.currentTimeMillis()-time<2000){
            finish();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }

    public void inputVoiceHaru(final TextView txt) {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("'하루야'라고 불러보세요.");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onError(int error) {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
                    stt.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    txt.setText(result.get(0));
                    replyHaru(result.get(0));
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        } catch (Exception e) {
            toast(e.toString());
        }
    }
    public void replyHaru(String input){
        try {
            if (input.indexOf("하루")>-1) {
                if (k==1) {
                    k=0;
                    nomal01();
                    mConnectedTask.write("z\n");
                }
                else {
                    mConnectedTask.write("z\n");
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAudioManager.setStreamVolume(STREAM_MUSIC,10,FLAG_REMOVE_SOUND_AND_VIBRATE);
                        mew=rnd.nextInt(5);
                        switch (mew){
                            case 0: {
                                mpmew = MediaPlayer.create(MainActivity.this,R.raw.mew1);
                                mpmew.start();
                                mHdrVoiceRecoState.sendEmptyMessageDelayed(MSG_VOICE_RECO_START, 680);
                                break;
                            }
                            case 1:{
                                mpmew = MediaPlayer.create(MainActivity.this,R.raw.mew2);
                                mpmew.start();
                                mHdrVoiceRecoState.sendEmptyMessageDelayed(MSG_VOICE_RECO_START, 780);
                                break;
                            }
                            case 2: {
                                mpmew = MediaPlayer.create(MainActivity.this,R.raw.mew3);
                                mpmew.start();
                                mHdrVoiceRecoState.sendEmptyMessageDelayed(MSG_VOICE_RECO_START, 525);
                                break;
                            }
                            case 3: {
                                mpmew = MediaPlayer.create(MainActivity.this,R.raw.mew4);
                                mpmew.start();
                                mHdrVoiceRecoState.sendEmptyMessageDelayed(MSG_VOICE_RECO_START, 553);
                                break;
                            }
                            case 4: {
                                mpmew = MediaPlayer.create(MainActivity.this, R.raw.mew5);
                                mpmew.start();
                                mHdrVoiceRecoState.sendEmptyMessageDelayed(MSG_VOICE_RECO_START, 734);
                                break;
                            }
                        }
                    }
                },250);
            } else {
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }catch (Exception e){
            toast(e.toString());
        }
    }

    public void inputVoice(final TextView txt) {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("음성입력 시작");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    toast("음성입력 종료");
                }

                @Override
                public void onError(int error) {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
                    stt.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    txt.setText(result.get(0));
                    replyAnswer(result.get(0));
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void toast(String msg){Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
}

    private void replyAnswer(String input){
        try {
            if (input.indexOf("안녕") > -1) {
                mConnectedTask.write("a\n");
                laugh();
            } else if (input.indexOf("앞") > -1) {
                mConnectedTask.write("b\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("뒤") > -1) {
                mConnectedTask.write("c\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("오른쪽") > -1) {
                mConnectedTask.write("d\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("왼쪽") > -1) {
                mConnectedTask.write("e\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("한 바퀴") > -1 || input.indexOf("돌아") > -1) {
                mConnectedTask.write("f\n");
                mero();
            } else if (input.indexOf("도리도리") > -1) {
                mConnectedTask.write("g\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("세수") > -1) {
                mConnectedTask.write("h\n");
                close();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nomal01();
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
                    }
                }, 6685);

            } else if (input.indexOf("좋아") > -1 || input.indexOf("사랑") > -1) {
                mConnectedTask.write("i\n");
                shy = rnd.nextInt(10);
                if (shy >= 8) {
                    shy();
                } else {
                    heart();
                }
            } else if (input.indexOf("싫어") > -1 || input.indexOf("미워") > -1) {
                mConnectedTask.write("j\n");
                angry();
            } else if (input.indexOf("노래") > -1) {
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_SONG);
            } else if (input.indexOf("빵")>-1){
                mConnectedTask.write("m\n");
                k=1;
                bang();
            } else if (input.indexOf("몇 시")>-1||input.indexOf("시간")>-1){
                mConnectedTask.write("n\n");
                if(ani.isRunning()){
                    ani.stop();
                }
                Face.setVisibility(View.GONE);
                dclock.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        dclock.setVisibility(View.GONE);
                        Face.setVisibility(View.VISIBLE);
                        nomal01();
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
                    }
                }, 1000);
            } else if (input.indexOf("이름")>-1){
                mConnectedTask.write("o\n");
                if(ani.isRunning()){
                    ani.stop();
                }
                Face.setVisibility(View.GONE);
                tvname.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        tvname.setVisibility(View.GONE);
                        Face.setVisibility(View.VISIBLE);
                        nomal01();
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
                    }
                }, 1000);
            } else if (input.indexOf("도망") > -1) {
                mConnectedTask.write("p\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("손") > -1) {
                mConnectedTask.write("q\n");
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            } else if (input.indexOf("종료") > -1) {
                finish();
            } else{
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        } catch (Exception e){
            toast(e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( mConnectedTask != null ) {
            mConnectedTask.cancel(true);
        }
        finish();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
            }
            mConnectionStatus.setText("연결 중...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            if ( isSucess ) {
                connected(mBluetoothSocket);
            }
            else{
                isConnectionError = true;
            }
        }
    }

    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
            }
            mConnectionStatus.setText(mConnectedDeviceName + "에 연결되었습니다.");
            nomal01();
            mAudioManager.setStreamVolume(STREAM_MUSIC,0,FLAG_REMOVE_SOUND_AND_VIBRATE);
            mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte [] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            while (true) {
                if ( isCancelled() ) return false;
                try {
                    int bytesAvailable = mInputStream.available();
                    if(bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);
                        for(int i=0;i<bytesAvailable;i++) {
                            byte b = packetBytes[i];
                            if(b == '\n')
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                String recvMessage = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;
                                publishProgress(recvMessage);
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
            }

        }

        @Override
        protected void onProgressUpdate(String... RecMessage) {
            mtvReciveData.setText(RecMessage[0]);
            int meslength = RecMessage[0].length();
            switch (meslength){
                case 2:
                    if(shock==0){
                        angry=rnd.nextInt(10);
                        if(angry>=8){
                            frown();
                        } else {
                            cry();
                        }
                    }
                    break;

                case 3:
                    if(k==0){
                    close();
                    k=1;
                    }
                    break;

                case 4:
                    break;

                case 5:
                    break;
            }

        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);
            if ( !isSucess ) {

                closeSocket();
                isConnectionError = true;
                showErrorDialog("블루투스 연결이 끊어졌습니다.");
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            closeSocket();
        }

        void closeSocket(){
            try {
                mBluetoothSocket.close();
            } catch (IOException e2) {
            }
        }

        void write(String msg){
            msg += "\n";
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
            }
        }
    }

    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            showQuitDialog( "페어린된 장치가 없습니다.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("연결할 장치를 선택하여주십시오");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }

    public void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("확인",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }

    public void showQuitDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("확인",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                showPairedDevicesListDialog();
            }
            if (resultCode == RESULT_CANCELED) {
                showQuitDialog("블루투스를 활성화 해주시기 바랍니다");
            }
        }
    }

    public void angry(){
        if(ani.isRunning()) {
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_angry);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
               nomal01();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 1480);
    }

    public void close(){
        if(ani.isRunning()) {
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_close);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
    }

    public void cry() {
        shock = 1;
        if (ani.isRunning()) {
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_cry);
        ani = (AnimationDrawable) Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nomal01();
                shock=0;
            }
        }, 2650);
    }
    public void laugh(){
        if(ani.isRunning()) {
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_laugh);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal02();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 6000);
    }

    public void heart(){
        if(ani.isRunning()) {
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_heart);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal01();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 2400);
    }

    public void nomal01(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_nomal01);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
    }

    public void nomal02(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_nomal02);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
    }

    public void shy(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_shy);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal02();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 2100);
    }

    public void frown(){
        shock=1;
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_frown);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal02();
                shock=0;
            }
        }, 1740);
    }

    public void mero(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_mero);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal01();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 5270);
    }

    public void sing_nyang(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_nyang);
        ani = (AnimationDrawable)Face.getDrawable();
        mpmew = MediaPlayer.create(MainActivity.this,R.raw.nyang);
        mAudioManager.setStreamVolume(STREAM_MUSIC,10,FLAG_REMOVE_SOUND_AND_VIBRATE);
        mpmew.start();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal02();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 10000);
    }

    public void sing_nabi(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_nabi);
        ani = (AnimationDrawable)Face.getDrawable();
        mpmew = MediaPlayer.create(MainActivity.this,R.raw.nabi);
        mAudioManager.setStreamVolume(STREAM_MUSIC,10,FLAG_REMOVE_SOUND_AND_VIBRATE);
        mpmew.start();
        ani.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                nomal02();
                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_HARU);
            }
        }, 15910);
    }

    public void bang(){
        if(ani.isRunning()){
            ani.stop();
        }
        Face.setImageResource(R.drawable.ani_bang);
        ani = (AnimationDrawable)Face.getDrawable();
        ani.start();
        mHdrVoiceRecoState.sendEmptyMessageDelayed(MSG_VOICE_RECO_HARU,2000);
    }
}