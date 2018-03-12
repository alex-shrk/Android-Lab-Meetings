package ru.ssau.sanya.mettings;


import android.content.Context;
import android.preference.PreferenceManager;

public class MeetingPreferences {
    public static final String PREF_COUNT_MEETINGS = "countMeetings";

    public static String getCountMeetings(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_COUNT_MEETINGS,null);
    }

    public static void setCountMeetings(Context context,String countMeetings) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_COUNT_MEETINGS,countMeetings)
                .apply();
    }
}
