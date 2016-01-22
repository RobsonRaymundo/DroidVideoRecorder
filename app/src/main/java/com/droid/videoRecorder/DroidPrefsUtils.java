package com.droid.videoRecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ResourceBundle;

/**
 * Created by Robson on 12/01/2016.
 */
public class DroidPrefsUtils {
    public static boolean exibeTelaInicial(final Context context)
    {
        boolean spf = false;
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            spf = sp.getBoolean("spf_exibeAoIniciar", true);
        }
        catch (Exception ex)
        {
            Log.d("DroidVideo", ex.getMessage());
        }
        return spf;

    }

    public static boolean exibeTempoGravacao(final Context context)
    {
        boolean spf = false;
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            spf = sp.getBoolean("spf_exibeTempoGravacao", true);
        }
        catch (Exception ex)
        {
            Log.d("DroidVideo", ex.getMessage());
        }
        return spf;

    }

    public static int obtemQualidadeCamera(final Context context,  DroidConstants.EnumTypeViewCam typeViewCam)
    {
        int qualid = 0; // QUALITY_LOW
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if ( typeViewCam == DroidConstants.EnumTypeViewCam.FacingFront) {
                qualid = Integer.parseInt(sp.getString("ltp_qualidadeCameraFrontal", "0"));
            }
            else qualid = Integer.parseInt(sp.getString("ltp_qualidadeCameraTraseira", "0"));

        }
        catch (Exception ex)
        {
            Log.d("DroidVideo", ex.getMessage());
        }
        return qualid;

    }

    public static int obtemLocalGravacao(final Context context)
    {
        int local = 0; // Interno
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            local = Integer.parseInt(sp.getString("ltp_localGravacaoVideo", "0"));
        }
        catch (Exception ex)
        {
            Log.d("DroidVideo", ex.getMessage());
        }
        return local;

    }

    public static String obtemDescricaoPreferencias (final Context context, String valor_selecionado, int nome_lista, int lista_valor ) {
        String nome_selecionado = "";

        String[] array_lista = context.getResources().getStringArray(nome_lista);
        String[] array_lista_valores = context.getResources().getStringArray(lista_valor);

        for (int i = 0; i < array_lista_valores.length ; i++) {
            if (array_lista_valores[i].equals(valor_selecionado))
            {
                nome_selecionado = array_lista[i].toString();
                break;
            }
        }
        return nome_selecionado;
    }
}
