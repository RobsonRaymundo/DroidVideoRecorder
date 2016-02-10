package com.droid.videoRecorder;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.util.Log;

public class DroidVideoRecorder {
    private static Camera mServiceCamera;
    private static MediaRecorder mMediaRecorder;

    public static DroidConstants.EnumStateRecVideo StateRecVideo;
    public static DroidConstants.EnumTypeViewCam TypeViewCam;
    public static int LocalGravacaoVideo = 0;

    private static void TimeSleep(Integer seg) {
        try {
            Thread.sleep(seg);
        } catch (Exception ex) {
        }
    }

    public static boolean isExternalStorageMediaMounted() {
        return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
    }

    public static String GetPathStorage() {
        String strSDCardPath = "";
        String strDirectory = "";

        try {

            if (LocalGravacaoVideo == 1) { // Cartao SD
                if (isExternalStorageMediaMounted()) {
                    strSDCardPath = System.getenv("SECONDARY_STORAGE");
                    if ((null == strSDCardPath) || (strSDCardPath.length() == 0)) {
                        strSDCardPath = System.getenv("EXTERNAL_SDCARD_STORAGE");
                    }
                    strDirectory = CreateGetDirectory(strSDCardPath + DroidConstants.PASTADOSARQUIVOSGRAVADOS);
                }
            }
        } catch (Exception e) {
        } finally {
            if (strSDCardPath == "" || strDirectory == "") {
                strSDCardPath = Environment.getExternalStorageDirectory().toString();
                strDirectory = CreateGetDirectory(strSDCardPath + DroidConstants.PASTADOSARQUIVOSGRAVADOS);
            }
        }
        return strDirectory;
    }


    public static String CreateGetDirectory(String pathStorage)
    {
        String pathDirectory = "";

        try {

            File myNewFolder = new File(pathStorage);

            if (!myNewFolder.exists()) {
                myNewFolder.mkdir();
                TimeSleep(1000);
            }
            if (myNewFolder.exists())
            {
                pathDirectory = pathStorage;
            }
        }
        catch (Exception e)
        {

        }
        return pathDirectory;
    }



    private static String NameFileRecDateNow()
    {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String dateformat = simpleFormat.format( new Date( System.currentTimeMillis() ));
        return GetPathStorage() + "/DVR_" + dateformat +  ".mp4";
    }

    private static int GetDisplayOrientationRec(int orientation)
    {
        int displayOrient = 0;

        if (orientation > 315 || orientation <= 45)
        {
            if (TypeViewCam == DroidConstants.EnumTypeViewCam.FacingFront)
            {
                displayOrient = 270;
            }
            else {
                displayOrient = 90;
            }
        }
        else if (orientation > 45 && orientation <= 135)
        {
            displayOrient =  180;
        }
        else if (orientation > 135 && orientation <= 225)
        {
            displayOrient =  270;
        }
        else if (orientation > 225 && orientation <= 315)
        {
            displayOrient = 0;
        }
        return displayOrient;

    }


    private static int GetDisplayOrientationView(Configuration orient, int orientation )
    {
        int displayOrient = 0;

        if (orient.orientation != Configuration.ORIENTATION_LANDSCAPE) {

            displayOrient = 90;

        } else {

            if (orientation > 45 && orientation <= 135)
            {
                displayOrient =  180;
            }
            else if (orientation > 135 && orientation <= 225)
            {
                displayOrient =  270;
            }

        }

        return displayOrient;
    }

    public static void OnInitRec (Configuration orient, int orientation, DroidConstants.EnumTypeViewCam typeViewCam)
    {
        try {
            int currentCameraId;
            if (typeViewCam == DroidConstants.EnumTypeViewCam.FacingFront)
            {
                currentCameraId=Camera.CameraInfo.CAMERA_FACING_FRONT;
                TypeViewCam = DroidConstants.EnumTypeViewCam.FacingFront;
            }
            else {
                currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                TypeViewCam = DroidConstants.EnumTypeViewCam.FacingBack;
            }

            if(mServiceCamera == null) {
                mServiceCamera = Camera.open(currentCameraId);

                Camera.Parameters params = mServiceCamera.getParameters();
                params.set("cam_mode", 1);
                mServiceCamera.setParameters(params);
                Camera.Parameters p = mServiceCamera.getParameters();

                final List<Camera.Size> listSize = p.getSupportedPreviewSizes();
                Camera.Size mPreviewSize = listSize.get(0);

                int previewWidth = mPreviewSize.width;
                int previewHeight = mPreviewSize.height;

                mServiceCamera.setDisplayOrientation(GetDisplayOrientationView(orient, orientation));

                p.setPreviewSize(previewWidth, previewHeight);
                mServiceCamera.setParameters(p);
            }



        }
        catch (Exception ex)
        {
            Log.d("ViewRec", ex.getMessage() );
        }

    }

    public static void OnViewRec(SurfaceHolder surfaceHolder)
    {
        try {

            try {
                mServiceCamera.setPreviewDisplay(surfaceHolder);
                mServiceCamera.startPreview();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
        catch (Exception ex)
        {
            Log.d("ViewRec", ex.getMessage() );
        }

    }


    public static void OnStartRecording(SurfaceHolder surfaceHolder, int orientation, int qualidadeCamera)
    {
        try {

            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFile(NameFileRecDateNow());

            mMediaRecorder.setProfile(CamcorderProfile.get(qualidadeCamera));

            mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mMediaRecorder.setOrientationHint(GetDisplayOrientationRec(orientation));

            mMediaRecorder.prepare();
            mMediaRecorder.start();
        }
        catch (Exception ex)
        {
            Log.d("StartRecording", ex.getMessage() );
        }
    }

    private static void ResetRecord(boolean record)
    {
        if (record) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        mServiceCamera.stopPreview();
        mServiceCamera.release();
        mServiceCamera = null;

    }

    public static void OnStopRecording(boolean record) {

        try {
            mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ResetRecord(record);
    }
}
