package ru.ssau.sanya.mettings.Service;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ru.ssau.sanya.mettings.Entity.Meeting;
import ru.ssau.sanya.mettings.MainActivity;
import ru.ssau.sanya.mettings.MeetingPreferences;
import ru.ssau.sanya.mettings.R;

public class NotificationService extends IntentService {
    private static final String TAG = "NotificationService";// = NotificationService.class.getName();

    private static final int NEW_MEETING_INTERVAL = 1000 * 60 * 1; //1 min

    public static Intent newIntent(Context context) {
        return new Intent(context, NotificationService.class);
    }

    public NotificationService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = NotificationService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            /*alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), NEW_MEETING_INTERVAL, pendingIntent);*/
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), NEW_MEETING_INTERVAL, pendingIntent);

        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        Log.i(TAG, "Received an intent: " + intent);
        String countMeetings = "0";
        if (MeetingPreferences.getCountMeetings(this) != null)
            countMeetings = MeetingPreferences.getCountMeetings(this);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mDatabase.getReference();

        final String finalCountMeetings = countMeetings;

        mRef.child("meetings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String nowDate = getCurDate();
                        Integer newCntMeetings = 0;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String stDate = ds.getValue(Meeting.class).getStartDate();
                            assert stDate != null;
                            if (stDate.equals(nowDate))
                                newCntMeetings++;
                        }

                        if (Integer.parseInt(finalCountMeetings) >= Integer.parseInt(newCntMeetings.toString())) {
                            Log.i(TAG, "No new meetings:" + newCntMeetings);
                        } else {
                            Integer cnt = (newCntMeetings - Integer.parseInt(finalCountMeetings));
                            Log.i(TAG, "Got " + cnt + " new meetings");

                            Resources resources = getResources();
                            Intent i = MainActivity.newIntent(NotificationService.this);
                            PendingIntent pendingIntent = PendingIntent
                                    .getActivity(NotificationService.this, 0, i, 0);
                            Notification notification = new NotificationCompat.Builder(NotificationService.this)
                                    .setTicker(resources.getString(R.string.new_meeting_title))
                                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                                    .setContentTitle(resources.getQuantityString(R.plurals.plurals_meeting_counts,cnt,cnt))
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                    .build();
                            NotificationManagerCompat notificationManager =
                                    NotificationManagerCompat.from(NotificationService.this);
                            notificationManager.notify(0,notification);

                        }
                        MeetingPreferences.setCountMeetings(NotificationService.this, newCntMeetings.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "onHandleIntent: listener", databaseError.toException());


                    }
                });


    }

    private String getCurDate() {
        String myFormat = "MM/dd/yy";
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        final Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert cm != null;
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
