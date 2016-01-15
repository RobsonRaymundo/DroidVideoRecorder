package com.droid.videoRecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

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
        this.stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mSurfaceView = new SurfaceView(this);
        mSurfaceView.setLayoutParams(params);
        mSurfaceView.getHolder().setFixedSize(1, 1);

        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.stoprec);
        txtHead = new TextView(this);
        txtHead.setText("00:00");
        txtHead.setVisibility(View.INVISIBLE);

        params.gravity = Gravity.CENTER;
        windowManager.addView(mSurfaceView, params);
        windowManager.addView(chatHead, params);
        windowManager.addView(txtHead, params);

        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.STOP;
        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.EnumTypeViewCam.FacingBack);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(DroidHeadService.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    SetDrawRec(DroidVideoRecorder.EnumStateRecVideo.CLOSE);
                    return super.onDoubleTap(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    SetDrawRec(DroidVideoRecorder.EnumStateRecVideo.VIEW);
                    super.onLongPress(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    SetDrawRec(DroidVideoRecorder.EnumStateRecVideo.RECORD);
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
        };

        txtHead.setOnTouchListener(onTouchListener);
        chatHead.setOnTouchListener(onTouchListener);

        myOrientationEventListener
                = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int arg0) {
                // TODO Auto-generated method stub
                orientationEvent = arg0;
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //call widget update methods/services/broadcasts
        if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.VIEW) {
            DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.TypeViewCam);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
        if (txtHead != null) windowManager.removeView(txtHead);
        if (mSurfaceView != null) windowManager.removeView(mSurfaceView);
        Vibrar(100);
    }

    private void ShowView() {
        chatHead.setImageResource(R.drawable.viewrec);
        mSurfaceView.getHolder().setFixedSize(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);
        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.EnumTypeViewCam.FacingBack);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.VIEW;
        Vibrar(100);
    }


    private void ChangeTypeViewCam() {
        chatHead.setImageResource(R.drawable.viewrec);
        mSurfaceView.getHolder().setFixedSize(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.MATCH_PARENT);

        if (DroidVideoRecorder.TypeViewCam == DroidVideoRecorder.EnumTypeViewCam.FacingBack) {
            DroidVideoRecorder.TypeViewCam = DroidVideoRecorder.EnumTypeViewCam.FacingFront;
        } else {
            DroidVideoRecorder.TypeViewCam = DroidVideoRecorder.EnumTypeViewCam.FacingBack;
        }

        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.TypeViewCam);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.VIEW;
        Vibrar(100);
    }

    private void ShowRec() {
        chatHead.setImageResource(R.drawable.rec);
        mSurfaceView.getHolder().setFixedSize(1, 1);
        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.TypeViewCam);
        DroidVideoRecorder.OnStartRecording(mSurfaceView.getHolder(), orientationEvent, DroidPrefsUtils.obtemQualidadeCamera(this));
        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.RECORD;
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
        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.STOP;
        DroidVideoRecorder.OnInitRec(getResources().getConfiguration(), orientationEvent, DroidVideoRecorder.EnumTypeViewCam.FacingBack);
        DroidVideoRecorder.OnViewRec(mSurfaceView.getHolder());
        DroidVideoRecorder.OnStopRecording(false);
    }

    private void ShowStop() {
        chatHead.setImageResource(R.drawable.stoprec);
        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.STOP;
    }

    private void ShowClose() {
        chatHead.setImageResource(R.drawable.closerec);
        DroidVideoRecorder.StateRecVideo = DroidVideoRecorder.EnumStateRecVideo.CLOSE;
        Vibrar(50);
    }

    private void ShowActivity() {
        context = getBaseContext();
        Intent mItent = new Intent(context, DroidConfigurationActivity.class);

        mItent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mItent.putExtra(DroidConstants.CHAMADAPELOSERVICO, true);
        startActivity(mItent);
    }

    private void SetDrawRec(DroidVideoRecorder.EnumStateRecVideo stateRecVideo) {

        if (stateRecVideo == DroidVideoRecorder.EnumStateRecVideo.VIEW) {
            if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.STOP) {
                ShowView();
            } else if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.VIEW) {
                ShowStopRecord(false);
                ChangeTypeViewCam();
            }
        } else if (stateRecVideo == DroidVideoRecorder.EnumStateRecVideo.CLOSE) {
            if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.STOP) {
                ShowClose();
            } else if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.CLOSE) {
                ShowActivity();
                ShowStop();
            } else if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.VIEW) {
                GetDefaultStop();
            }
        } else {
            if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.STOP) {
                ShowRec();
            } else if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.RECORD) {
                ShowStopRecord(true);
            } else if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.VIEW) {
                ShowRec();
            } else if (DroidVideoRecorder.StateRecVideo == DroidVideoRecorder.EnumStateRecVideo.CLOSE) {
                TimeSleep(300);
                StopService();
            }
        }
    }

    private void Vibrar(int valor) {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
            txtHead.setVisibility(View.VISIBLE);

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

}





