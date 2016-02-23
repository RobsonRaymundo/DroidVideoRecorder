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
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;


public class DroidHeadService extends Service implements TextToSpeech.OnInitListener {
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
    private View.OnTouchListener onTouchListener;
    private String chamadaPorComandoTexto;
    private SensorManager sensorManager;
    public static boolean closeSensorProximity;
    public static boolean openSensorProximity;
    public static boolean currentCloseSensorProximity;

    private boolean necessarioComandoDepoisDoInit = false;
    private boolean aceitaComandoPorVoz;
    private SensorEventListener sensorEventListener;
    private SpeechRecognizer stt;
    private Intent mIntentRecognizer;
    private Intent mIntentService;
    private TextToSpeech tts;
    private ArrayList<DroidConstants.EnumStateRecVideo> stateRecVideoSTOP;
    private ArrayList<DroidConstants.EnumStateRecVideo> stateRecVideoVIEW;
    private ArrayList<DroidConstants.EnumStateRecVideo> stateRecVideoREC;
    private ArrayList<DroidConstants.EnumStateRecVideo> stateRecVideoCLOSE;

    OrientationEventListener myOrientationEventListener;

    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

    private void TimeSleep(Integer seg) {
        try {
            Thread.sleep(seg);
        } catch (Exception ex) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onInit(int status) {
        necessarioComandoDepoisDoInit = true;
        if (ComandoPorTexto("MIR")) {
            GravarModoOculto();
        } else if (ComandoPorTexto("R")) {
            Gravar();
        } else if (ComandoPorTexto("CFG")) {
            AbrirConfig();
        } else Abrir();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mIntentService = intent;

        if (ComandoPorTexto("MI")) {
            ModoOculto();
        } else if (ComandoPorTexto("MIR")) {
            ModoOcultoSemFala();
        } else if (ComandoPorTexto("MV")) {
            ModoVisivel();
        } else if (ComandoPorTexto("S")) {
            Parar();
        } else if (ComandoPorTexto("V")) {
            Visualizar();
        } else if (ComandoPorTexto("R")) {
            Gravar();
        } else if (ComandoPorTexto("C")) {
            Fechar();
        } else if (ComandoPorTexto("Q")) {
            Sair();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InicializarVariavel();
        InicializarAcao();
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

    private void InicializarVariavel() {
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
        tts = new TextToSpeech(context, this);

        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.STOP;
        DroidVideoRecorder.LocalGravacaoVideo = DroidPrefsUtils.obtemLocalGravacao(context);
        sensorEventListener = new sensorEventListener();
        onTouchListener = new TouchListener();
        aceitaComandoPorVoz = DroidPrefsUtils.aceitaComandoPorVoz(context);
        tts.setLanguage(Locale.getDefault());

        stateRecVideoSTOP = new ArrayList<>();
        stateRecVideoSTOP.add(DroidConstants.EnumStateRecVideo.VIEW);
        stateRecVideoSTOP.add(DroidConstants.EnumStateRecVideo.RECORD);
        stateRecVideoSTOP.add(DroidConstants.EnumStateRecVideo.CLOSE);

        stateRecVideoSTOP = new ArrayList<>();
        stateRecVideoSTOP.add(DroidConstants.EnumStateRecVideo.VIEW);
        stateRecVideoSTOP.add(DroidConstants.EnumStateRecVideo.RECORD);
        stateRecVideoSTOP.add(DroidConstants.EnumStateRecVideo.CLOSE);

        stateRecVideoVIEW = new ArrayList<>();
        stateRecVideoVIEW.add(DroidConstants.EnumStateRecVideo.VIEW);
        stateRecVideoVIEW.add(DroidConstants.EnumStateRecVideo.RECORD);
        stateRecVideoVIEW.add(DroidConstants.EnumStateRecVideo.STOP);

        stateRecVideoREC = new ArrayList<>();
        stateRecVideoREC.add(DroidConstants.EnumStateRecVideo.STOP);

        stateRecVideoCLOSE = new ArrayList<>();
        stateRecVideoCLOSE.add(DroidConstants.EnumStateRecVideo.CLOSE);
        stateRecVideoCLOSE.add(DroidConstants.EnumStateRecVideo.STOP);

    }

    private void InicializarAcao() {
        txtHead.setOnTouchListener(onTouchListener);
        chatHead.setOnTouchListener(onTouchListener);

        myOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int arg0) {
                // TODO Auto-generated method stub
                orientationEvent = arg0;
            }
        };

        if (aceitaComandoPorVoz) {
            EnabledSensorPriximity();
            stt = SpeechRecognizer.createSpeechRecognizer(context);
            mIntentRecognizer = DroidBaseRecognitionListener.getRecognizerIntent(context);
            stt.setRecognitionListener(new DroidBaseRecognitionListener() {
                public void onResults(Bundle results) {
                    // Recupera as possíveis palavras que foram pronunciadas
                    ArrayList<String> words = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    Log.d("DroidWakeUp", "onResults: " + words);
                    if (words.contains(getString(R.string.gravar))) {
                        Gravar();
                    } else if (words.contains(getString(R.string.parar))) {
                        Parar();
                    } else if (words.contains(getString(R.string.fechar))) {
                        Fechar();
                    } else if (words.contains(getString(R.string.sair))) {
                        Sair();
                    } else if (words.contains(getString(R.string.voltar))) {
                        ShowStop();
                    } else if (words.contains(getString(R.string.abrirConfiguracao))) {
                        AbrirConfig();
                    } else if (words.contains(getString(R.string.modoOculto))) {
                        ModoOculto();
                    } else if (words.contains(getString(R.string.modoVisivel))) {
                        ModoVisivel();
                    } else {
                        NaoEntendi();
                    }
                }
            });
        }
    }

    private void StopService() {
        context.stopService(mIntentService);
        tts.shutdown();
    }

    private void FalaComAtraso(final String text, int atrasoSeg) {
        if (DroidPrefsUtils.leComando(context)) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            TimeSleep(atrasoSeg * 1000);
        }
    }

    private void Fala(final String text) {
        if (DroidPrefsUtils.leComando(context)) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private boolean ComandoPorTexto(String cmd) {
        boolean ret = false;
        if (DroidPrefsUtils.aceitaComandoPorTexto(context)) {
            chamadaPorComandoTexto = mIntentService.getStringExtra(DroidConstants.CHAMADAPORCOMANDOTEXTO);
            if (chamadaPorComandoTexto != null) {
                ret = chamadaPorComandoTexto.equalsIgnoreCase(DroidConstants.COMANDOINICIADOPOR + cmd);
            }
        }
        return ret;
    }

    private void Abrir() {
        Fala(getString(R.string.abrindoDVR));
    }

    private void AbrirConfig() {
        Fala(getString(R.string.abrindoDVRConfig));
        ShowActivity();
    }

    private void Gravar() {
        Gravacao(false);
    }

    private void Gravacao(boolean gravarModoOculto) {
        if (necessarioComandoDepoisDoInit) {
            if (Permite(DroidVideoRecorder.StateRecVideo.RECORD)) {
                if (gravarModoOculto) {
                    Fala(getString(R.string.gravandoModoOculto));
                } else {
                    Fala(getString(R.string.gravando));
                }
            }
            ShowRec();
        }
    }

    private void Parar() {
        if (Permite(DroidVideoRecorder.StateRecVideo.STOP)) {
            Fala(getString(R.string.parandoGravacao));
            ShowStopRecord(true);
        }
    }

    private void Visualizar() {
        if (Permite(DroidVideoRecorder.StateRecVideo.VIEW)) {
            Fala(getString(R.string.visualizando));
            ShowView();
        }
    }

    private void VisualizarTrocandoCamera() {
        if (Permite(DroidVideoRecorder.StateRecVideo.VIEW)) {
            ShowStopRecord(false);
            ChangeTypeViewCam();
        }
    }

    private void Fechar() {
        if (Permite(DroidVideoRecorder.StateRecVideo.CLOSE)) {
            Fala(getString(R.string.fechando));
            ShowClose();
        }
    }

    private void Sair() {
        if (Permite(DroidVideoRecorder.StateRecVideo.CLOSE)) {
            FalaComAtraso(getString(R.string.saindoDVR), 2);
            StopService();
        }
    }

    private void NaoEntendi() {
        Fala(getString(R.string.naoEntendi));
    }

    private void GravarModoOculto() {
        Gravacao(true);
    }

    private void ModoOculto() {
        Fala(getString(R.string.modoOculto));
        chatHead.setVisibility(View.INVISIBLE);
        txtHead.setVisibility(View.INVISIBLE);
    }
    private void ModoOcultoSemFala() {
        chatHead.setVisibility(View.INVISIBLE);
        txtHead.setVisibility(View.INVISIBLE);
    }

    private void ModoVisivel() {
        Fala(getString(R.string.modoVisivel));
        chatHead.setVisibility(View.VISIBLE);
        if (DroidPrefsUtils.exibeTempoGravacao(this))
        {
            txtHead.setVisibility(View.VISIBLE);
        }

    }

    public void SetSensorProximity(boolean turnOn) {
        try {

            if (turnOn && sensorManager == null) {
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

                if (proximitySensor != null) {
                    sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                    TimeSleep(1000);
                }
            }

            if (turnOn == false && sensorManager != null) {
                sensorManager.unregisterListener(sensorEventListener);
                //  timeSleep(700);
                sensorManager = null;
            }

        } catch (Exception ex) {
            String msg = ex.getMessage();

        }
    }

    private void EnabledSensorPriximity() {
        if (aceitaComandoPorVoz) {
            SetSensorProximity(true);
        }
    }

    private void DisabledSensorPriximity() {
        if (aceitaComandoPorVoz) {
            SetSensorProximity(false);
        }
    }

    private void ShowView() {
        chatHead.setImageResource(R.drawable.viewrec);
        mSurfaceView.getHolder().setFixedSize(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);
        DroidVideoRecorder.OnInitRec(context.getResources().getConfiguration(), orientationEvent, DroidConstants.EnumTypeViewCam.FacingBack);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.StateRecVideo = DroidConstants.EnumStateRecVideo.VIEW;
        Vibrar(100);
    }

    private void ChangeTypeViewCam() {
        chatHead.setImageResource(R.drawable.viewrec);
        mSurfaceView.getHolder().setFixedSize(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);

        if (DroidVideoRecorder.TypeViewCam == DroidConstants.EnumTypeViewCam.FacingBack) {
            Fala(getString(R.string.visualizandoCameraFronta));
            DroidVideoRecorder.TypeViewCam = DroidConstants.EnumTypeViewCam.FacingFront;
        } else {
            Fala(getString(R.string.visualizandoCameraTraseira));
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

    private boolean Permite(DroidConstants.EnumStateRecVideo stateRecVideo) {

        if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
            return stateRecVideoSTOP.contains(stateRecVideo);
        } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
            return stateRecVideoVIEW.contains(stateRecVideo);
        } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.RECORD) {
            return stateRecVideoREC.contains(stateRecVideo);
        } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.CLOSE) {
            return stateRecVideoCLOSE.contains(stateRecVideo);
        } else return false;
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
                    if (second == 60) {
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

                if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
                    Fechar();
                } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.CLOSE) {
                    AbrirConfig();
                    ShowStop();
                } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
                    GetDefaultStop();
                }

                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
                    Visualizar();
                } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
                    VisualizarTrocandoCamera();
                }
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.STOP) {
                    Gravar();
                } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.RECORD) {
                    Parar();
                } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.VIEW) {
                    Gravar();
                } else if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.CLOSE) {
                    Sair();
                }

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

    public class sensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] < event.sensor.getMaximumRange()) {
                    closeSensorProximity = true;
                    currentCloseSensorProximity = true;
                } else {
                    openSensorProximity = true;
                    currentCloseSensorProximity = false;
                }
            }

            if (currentCloseSensorProximity && closeSensorProximity && openSensorProximity) {

                if (DroidVideoRecorder.StateRecVideo == DroidConstants.EnumStateRecVideo.RECORD) {
                    Parar();
                } else {
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
    }
}





