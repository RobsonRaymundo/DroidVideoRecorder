package com.droid.videoRecorder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Robson on 12/01/2016.
 */
public class DroidConfigurationActivity extends PreferenceActivity {
    private Context context;
    private ListPreference ltp_qualidadeCameraFrontal;
    private ListPreference ltp_qualidadeCameraTraseira;
    private ListPreference ltp_localGravacaoVideo;
    private SwitchPreference spf_aceitaComandoPorTexto;
    private boolean canFinish;

    private boolean ExibeTelaInicial() {
        return DroidPrefsUtils.exibeTelaInicial(context);
    }

    private boolean ChamadaPeloServico() {
        return DroidPrefsUtils.chamadaPeloServico(getIntent());
    }

    private boolean ChamadaConfigPorComandoTexto() {
        return DroidPrefsUtils.chamadaPorComandoTexto(getIntent());
    }

    private String ChamadaBroadCastPorComandoTexto() {
        return DroidPrefsUtils.chamadaBroadCastPorComandoTexto(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getBaseContext();
        boolean exibeTelaInicial = ExibeTelaInicial();
        boolean chamadaPeloServico = ChamadaPeloServico();
        boolean chamadaConfigPorComandoTexto = ChamadaConfigPorComandoTexto();

        if (exibeTelaInicial || chamadaPeloServico || chamadaConfigPorComandoTexto) {
            setTheme(R.style.DefaultTheme);
        } else {
            setTheme(R.style.TranslucentTheme);
        }
        super.onCreate(savedInstanceState);
        canFinish = true;

        if (exibeTelaInicial || chamadaPeloServico || chamadaConfigPorComandoTexto) {
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
                    preference.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, newValue.toString(), R.array.qualidadeCameraTraseira, R.array.valor_qualidadeCameraTraseira));
                    return true;
                }
            });

            ltp_localGravacaoVideo = (ListPreference) findPreference("ltp_localGravacaoVideo");
            ltp_localGravacaoVideo.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, String.valueOf(DroidPrefsUtils.obtemLocalGravacao(context)), R.array.localArquivosGravados, R.array.valor_localArquivosGravados));
            ltp_localGravacaoVideo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(DroidPrefsUtils.obtemDescricaoPreferencias(context, newValue.toString(), R.array.localArquivosGravados, R.array.valor_localArquivosGravados));
                    DroidVideoRecorder.LocalGravacaoVideo = Integer.parseInt(newValue.toString());
                    return true;
                }
            });

            spf_aceitaComandoPorTexto = (SwitchPreference) findPreference("spf_aceitaComandoPorTexto");


            spf_aceitaComandoPorTexto.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        Boolean aceita = (Boolean) newValue;
                        Boolean status = DroidPrefsUtils.statusComandoPorTexto(context);
                        if (aceita != status) {
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            canFinish = false;
                        }


                    } catch (Exception ex) {
                        Log.d("DVR", ex.getMessage());
                    }
                    return true;
                }
            });

        } else finish();

        if (!chamadaPeloServico) {
            Intent intentService = new Intent(context, DroidHeadService.class);
            intentService.putExtra(DroidConstants.CHAMADAPORCOMANDOTEXTO, ChamadaBroadCastPorComandoTexto());
            startService(intentService);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!ExibeTelaInicial() && !ChamadaPeloServico() && !ChamadaConfigPorComandoTexto()) {
            finish();
        } else {
            spf_aceitaComandoPorTexto.setChecked(DroidPrefsUtils.statusComandoPorTexto(context));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (canFinish) finish();
    }
}
