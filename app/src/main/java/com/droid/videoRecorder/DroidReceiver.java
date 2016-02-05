package com.droid.videoRecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Robson on 22/01/2016.
 */
public class DroidReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Intent mIntent = new Intent(context, DroidConfigurationActivity.class);
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mIntent.setAction(Intent.ACTION_MAIN);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String messageBroadCast = intent.getStringExtra(DroidConstants.CHAVERECEIVER);
            //Toast.makeText(context, messageBroadCast, Toast.LENGTH_LONG).show();
            mIntent.putExtra(DroidConstants.CHAMADAPELODNP, messageBroadCast );
            context.startActivity(mIntent);
        }catch (Exception ex)
        {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}
