package ru.ssau.sanya.mettings.Service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.ssau.sanya.mettings.MainActivity;

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //context.startService(new Intent(context,NotificationService.class));
        NotificationService.setServiceAlarm(context,true);
    }
}
