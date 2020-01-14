package com.turen.reflektd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONObject;

import java.util.Map;

public class MyJobService extends JobService {

    Map<String, String> data;

    @Override
    public boolean onStartJob(JobParameters job) {

        Bundle bundle = job.getExtras();

        String strData = bundle.getString("data");

        String title="", message="", conversationId="";
        try {

            JSONObject obj = new JSONObject(strData);

            title = obj.getString("From");
            message = obj.getString("Message");
            conversationId = obj.getString("conversation_id");

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + strData + "\"");
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fromNotification", true);
        intent.putExtra("CONVERSATIONID", conversationId);

        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification n  = new Notification.Builder(this)
                .setContentTitle("New message from " + title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setSound(soundUri)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
