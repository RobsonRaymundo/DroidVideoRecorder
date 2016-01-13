package com.droid.videoRecorder;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.OrientationEventListener;

public class DroidActivity extends Activity {
    OrientationEventListener myOrientationEventListener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startService(new Intent(this, DroidHeadService.class));
        finish();
    }
}

