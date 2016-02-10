package com.droid.videoRecorder;

/**
 * Created by Robson on 12/01/2016.
 */
public class DroidConstants {

    public static enum EnumTypeViewCam
    {
        FacingBack,
        FacingFront
    }

    public enum EnumStateRecVideo {
        CLOSE,
        STOP,
        VIEW,
        RECORD
    }


    public static final String CHAMADAPELOSERVICO = "chamadaPeloServico";
    public static final String CHAMADAPORCOMANDOTEXTO = "chamadaPorComandoDeTexto";
    public static final String CHAVERECEIVER = "DVRREC";
    public static final String COMANDOINICIADOPOR = "DVR=";
    public static final String PASTADOSARQUIVOSGRAVADOS = "/DroidVideoRecorder/";

}
