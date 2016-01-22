package com.droid.videoRecorder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.prefs.Preferences;

/**
 * Created by Robson on 12/01/2016.
 */
public class DroidConfigurationActivity extends PreferenceActivity {
    private Context context;
    private ListPreference ltp_qualidadeCameraFrontal;
    private ListPreference ltp_qualidadeCameraTraseira;

    private boolean ExibeTelaInicial() {
        boolean exibeTelaInicial = true;
        try {
            exibeTelaInicial = DroidPrefsUtils.exibeTelaInicial(context);
        } catch (Exception ex) {

        }
        return exibeTelaInicial;
    }

    private boolean ChamadaPeloServico() {
        boolean chamadaPeloServico = false;
        try {
            chamadaPeloServico = getIntent().getBooleanExtra(DroidConstants.CHAMADAPELOSERVICO, false);
        } catch (Exception ex) {

        }
        return chamadaPeloServico;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getBaseContext();
        boolean exibeTelaInicial = ExibeTelaInicial();
        boolean chamadaPeloServico = ChamadaPeloServico();
        if (exibeTelaInicial || chamadaPeloServico) {
            setTheme(R.style.DefaultTheme);
        } else {
            setTheme(R.style.TranslucentTheme);
        }
        super.onCreate(savedInstanceState);
        if (exibeTelaInicial || chamadaPeloServico) {
            addPreferencesFromResource(R.xml.preferences);

            ltp_qualidadeCameraFrontal = (ListPreference) findPreference("ltp_qualidadeCameraFrontal");
            ltp_qualidadeCameraFrontal.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, String.valueOf(DroidPrefsUtils.obtemQualidadeCamera(context, DroidConstants.EnumTypeViewCam.FacingFront)), R.array.qualidadeCameraFrontal, R.array.valor_qualidadeCameraFrontal));
            ltp_qualidadeCameraFrontal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, newValue.toString(), R.array.qualidadeCameraFrontal, R.array.valor_qualidadeCameraFrontal));
                    return true;
                }
            });

            ltp_qualidadeCameraTraseira = (ListPreference) findPreference("ltp_qualidadeCameraTraseira");
            ltp_qualidadeCameraTraseira.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, String.valueOf(DroidPrefsUtils.obtemQualidadeCamera(context, DroidConstants.EnumTypeViewCam.FacingBack)), R.array.qualidadeCameraTraseira, R.array.valor_qualidadeCameraTraseira));
            ltp_qualidadeCameraTraseira.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, newValue.toString(), R.array.qualidadeCameraTraseira, R.array.valor_qualidadeCameraTraseira ));
                    return true;
                }
            });

        } else finish();

        if (!chamadaPeloServico) {
            Intent intentService = new Intent(context, DroidHeadService.class);
            startService(intentService);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!ExibeTelaInicial() && !ChamadaPeloServico()) {
           finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
