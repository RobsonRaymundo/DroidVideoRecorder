package com.droid.videoRecorder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class DroidHeadService extends Service {
    private WindowManager windowManager;
    private ImageView chatHead;
    private TextView txtHead;
    private SurfaceView mSurfaceView;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private int orientationEvent;
    private Context context;
    private AsyncTask asyncTask;
    private String chamadaPeloDNP;
    private SensorManager sensorManager;
    public static boolean closeSensorProximity;
    public static boolean openSensorProximity;
    public static boolean currentCloseSensorProximity;
    private boolean checkSensorGesture;
    private boolean checkSensorProx;
    private SensorEventListener sensorEventListener;
    private SpeechRecognizer stt;
    private Intent mIntentRecognizer;
    private Intent mIntentService;

    OrientationEventListener myOrientationEventListener;

    private void TimeSleep(Integer seg) {
        try {
            Thread.sleep(seg);
        } catch (Exception ex) {
        }
    }

    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    private void StopService() {
        context.stopService(mIntentService);
    }


    private void ConfigChamadaPeloDNP(Intent intent)
    {
        chamadaPeloDNP = intent.getStringExtra(DroidConstants.CHAMADAPELODNP);

        if (chamadaPeloDNP != null) {
            switch (chamadaPeloDNP) {
                case "DVR=INVISIBLE":
                {
                    chatHead.setVisibility(View.INVISIBLE);
                    txtHead.setVisibility(View.INVISIBLE);
                    break;
                }
                case "DVR=STOP":
                {
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                    break;
                }
                case "DVR=VIEW":
                {
                    SetDrawRec(DroidConstants.EnumStateRecVideo.VIEW);
                    break;
                }
                case "DVR=REC":
                {
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                    break;
                }
                case "DVR=CLOSE":
                {
                    SetDrawRec(DroidConstants.EnumStateRecVideo.CLOSE);
                    break;
                }
                case "DVR=QUIT":
                {
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                    break;
                }

            }
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mIntentService = intent;
        ConfigChamadaPeloDNP(intent);
     //   TimeSleep(2000);
     //   SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
    }



    @Override
    public void onCreate() {
        super.onCreate();
        context = getBaseContext();


        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.setLayoutParams(params);
        mSurfaceView.getHolder().setFixedSize(1, 1);

        chatHead = new ImageView(context);
        chatHead.setImageResource(R.drawable.stoprec);
        txtHead = new TextView(context);
        txtHead.setText("00:00");
        txtHead.setVisibility(View.INVISIBLE);

        params.gravity = Gravity.CENTER;
        windowManager.addView(mSurfaceView, params);
        windowManager.addView(chatHead, params);
        windowManager.addView(txtHead, params);

        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.STOP;
        //DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidConstants.EnumTypeViewCam.FacingBack);


        DroidVideoRecorder.LocalGravacaoVideo = DroidPrefsUtils.obtemLocalGravacao(context);

        sensorEventListener = new sensorEventListener();

        View.OnTouchListener onTouchListener = new TouchListener();

        txtHead.setOnTouchListener(onTouchListener);
        chatHead.setOnTouchListener(onTouchListener);

        myOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int arg0) {
                // TODO Auto-generated method stub
                orientationEvent = arg0;
            }
        };


        checkSensorProx = true;
        checkSensorGesture = true;

        EnabledSensorPriximity();

        stt = SpeechRecognizer.createSpeechRecognizer(context);
        mIntentRecognizer = getRecognizerIntent();
        stt.setRecognitionListener(new BaseRecognitionListener() {
            public void onResults(Bundle results) {
                // Recupera as possíveis palavras que foram pronunciadas
                ArrayList<String> words = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d("DroidWakeUp", "onResults: " + words);
                if (words.contains("gravar")) {
                    Log.d("DroidWakeUp", "Comando: gravar");
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                }
                else if (words.contains("parar")) {
                    Log.d("DroidWakeUp", "Comando: parar");
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                }
                else if (words.contains("fechar")) {
                    Log.d("DroidWakeUp", "Comando: fechar");
                    SetDrawRec(DroidConstants.EnumStateRecVideo.CLOSE);
                }
                else if (words.contains("sair")) {
                    Log.d("DroidWakeUp", "Comando: sair");
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                }
                else {
                    Log.d("DroidWakeUp", "Nao entendeu");
                }
            }

            @Override
            public void onError(int error) {
                super.onError(error);
            }
        });

    }

    protected Intent getRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        return intent;
    }

    public void SetSensorProximity(boolean turnOn)
    {
        try {

            if (turnOn && sensorManager == null)
            {
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

                if(proximitySensor != null )
                {
                    sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                    TimeSleep(1000);
                }
            }

            if (turnOn == false && sensorManager != null)
            {
                sensorManager.unregisterListener(sensorEventListener);
                //  timeSleep(700);
                sensorManager = null;
            }

        }
        catch (Exception ex)
        {
            String msg = ex.getMessage();

        }
    }

    private void EnabledSensorPriximity()
    {
        if (checkSensorGesture || checkSensorProx)
        {
            SetSensorProximity(true);
        }
    }

    private void DisabledSensorPriximity()
    {
        if (checkSensorGesture || checkSensorProx)
        {
            SetSensorProximity(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //call widget update methods/services/broadcasts
        if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
            DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.TypeViewCam);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
        if (txtHead != null) windowManager.removeView(txtHead);
        if (mSurfaceView != null) windowManager.removeView(mSurfaceView);
        DisabledSensorPriximity();
        Vibrar(100);
    }

    private void ShowView() {
      //  DisabledSensorPriximity();
        chatHead.setImageResource(R.drawable.viewrec);
        mSurfaceView.getHolder().setFixedSize(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);
        DroidVideoRecorder.OnInitRec(context.getResources().getConfiguration(), orientationEvent, DroidConstants.EnumTypeViewCam.FacingBack);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.VIEW;
        Vibrar(100);
    //    EnabledSensorPriximity();
    }

    private void ChangeTypeViewCam() {
        chatHead.setImageResource(R.drawable.viewrec);
        mSurfaceView.getHolder().setFixedSize(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);

        if (DroidVideoRecorder.TypeViewCam == DroidConstants.EnumTypeViewCam.FacingBack) {
            DroidVideoRecorder.TypeViewCam = DroidConstants.EnumTypeViewCam.FacingFront;
        } else {
            DroidVideoRecorder.TypeViewCam = DroidConstants.EnumTypeViewCam.FacingBack;
        }

        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.TypeViewCam);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.VIEW;
        Vibrar(100);
    }

    private void ShowRec() {
        chatHead.setImageResource(R.drawable.rec);
        mSurfaceView.getHolder().setFixedSize(1, 1);
        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.TypeViewCam);
        DroidVideoRecorder.OnStartRecording(mSurfaceView.getHolder(), orientationEvent, DroidPrefsUtils.obtemQualidadeCamera(this, DroidConstants.EnumTypeViewCam.FacingBack));
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.RECORD;
        Vibrar(50);
        if (DroidPrefsUtils.exibeTempoGravacao(this)) {
            asyncTask = new Sincronizar().execute();
        }
    }

    private void ShowStopRecord(boolean record) {
        DroidVideoRecorder.OnStopRecording(record);
        if (record && asyncTask != null) {
            asyncTask.cancel(true);
        }
        ShowStop();
        Vibrar(50);
    }

    private void GetDefaultStop() {
        mSurfaceView.getHolder().setFixedSize(1, 1);
        chatHead.setImageResource(R.drawable.stoprec);
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.STOP;
        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidConstants.EnumTypeViewCam.FacingBack);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.OnStopRecording(false);
    }

    private void ShowStop() {
        chatHead.setImageResource(R.drawable.stoprec);
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.STOP;
    }

    private void ShowClose() {
        chatHead.setImageResource(R.drawable.closerec);
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.CLOSE;
        Vibrar(50);
    }

    private void ShowActivity() {
        //context = getBaseContext();
        Intent mItent = new Intent(context, DroidConfigurationActivity.class);

        mItent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mItent.putExtra(DroidConstants.CHAMADAPELOSERVICO, true);
        startActivity(mItent);
    }

    private void SetDrawRec(DroidConstants.EnumStateRecVideo stateRecVideo) {

        if (stateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
            if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
                ShowView();
            } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
                ShowStopRecord(false);
                ChangeTypeViewCam();
            }
        } else if (stateRecVideo == DroidConstants.EnumStateRecVideo.CLOSE) {
            if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
                ShowClose();
            } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.CLOSE) {
                ShowActivity();
                ShowStop();
            } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
                GetDefaultStop();
            }
        } else {
            if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
                ShowRec();
            } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.RECORD) {
                ShowStopRecord(true);
            } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
                ShowRec();
            } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.CLOSE) {
                TimeSleep(300);
                StopService();
            }
        }
    }

    private void Vibrar(int valor) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(valor);
        } catch (Exception ex) {
        }
    }

    private class Sincronizar extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int minutes = 0;
                int second = 0;
                while (second <= 60) {
                    Thread.sleep(1000);
                    second++;
                    if (second == 60)
                    {
                        minutes++;
                        second = 0;
                    }
                    publishProgress(second, minutes);

                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (chatHead.getVisibility() == View.VISIBLE) {
                txtHead.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            txtHead.setText("00:00");
            txtHead.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DecimalFormat df = new DecimalFormat("00");
            txtHead.setText(df.format(values[1]) + ":" + df.format(values[0]));
        }
    }

    public class TouchListener implements View.OnTouchListener {

        private GestureDetector gestureDetector = new GestureDetector(DroidHeadService.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                SetDrawRec(DroidConstants.EnumStateRecVideo.CLOSE);
                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                SetDrawRec(DroidConstants.EnumStateRecVideo.VIEW);
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                return super.onSingleTapConfirmed(e);
            }
        });

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    if (myOrientationEventListener.canDetectOrientation()) {
                        myOrientationEventListener.enable();
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    Integer totalMoveX = (int) (event.getRawX() - initialTouchX);
                    params.x = initialX + totalMoveX;
                    Integer totalMoveY = (int) (event.getRawY() - initialTouchY);
                    params.y = initialY + totalMoveY;
                    windowManager.updateViewLayout(chatHead, params);
                    windowManager.updateViewLayout(txtHead, params);
                    return true;
            }

            return true;
        }

    }

    public class sensorEventListener  implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if(event.values[0] < event.sensor.getMaximumRange()) {
                    closeSensorProximity = true;
                    currentCloseSensorProximity = true;
                } else {
                    openSensorProximity = true;
                    currentCloseSensorProximity = false;
                }
            }

            if (currentCloseSensorProximity && closeSensorProximity && openSensorProximity) {

                if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.RECORD) {
                    SetDrawRec(DroidConstants.EnumStateRecVideo.RECORD);
                }
                else {
                    // Inicia o Listener do reconhecimento de voz
                    stt.startListening(mIntentRecognizer);
                    currentCloseSensorProximity = false;
                    closeSensorProximity = false;
                    openSensorProximity = false;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


}





