package com.droid.videoRecorder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Robson on 12/01/2016.
 */
public class DroidConfigurationActivity extends PreferenceActivity {
    private Context context;

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
