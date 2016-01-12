package com.droid.videoRecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Robson on 12/01/2016.
 */
public class DroidPrefsUtils {
    public static boolean exibeTelaInicial(final Context context)
    {
        boolean chk = false;
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            chk = sp.getBoolean("chk_exibeAoIniciar", true);
        }
        catch (Exception ex)
        {
            Log.d("DroidVideo", ex.getMessage());
        }
        return chk;

    }
    public static int  obtemQualidadeCamera(final Context context)
    {
        int qualid = 0; // QUALITY_LOW
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (DroidVideoRecorder.TypeViewCam == DroidVideoRecorder.EnumTypeViewCam.FacingFront) {
                qualid = Integer.parseInt(sp.getString("ltp_qualCamFrontal", "0"));
            }
            else qualid = Integer.parseInt(sp.getString("ltp_qualCamTraseira", "0"));

        }
        catch (Exception ex)
        {
            Log.d("DroidVideo", ex.getMessage());
        }
        return qualid;

    }


}
