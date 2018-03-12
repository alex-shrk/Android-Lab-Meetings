package ru.ssau.sanya.mettings.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.ssau.sanya.mettings.R;


public class MeetingsViewHolder extends RecyclerView.ViewHolder {

    public TextView meetingName;
    public TextView meetingDescr;
    public TextView meetingStartTime;
    public TextView meetingPriority;

    public MeetingsViewHolder(View view) {
        super(view);
        meetingName = view.findViewById(R.id.list_meeting_name);
        meetingDescr = view.findViewById(R.id.list_meeting_descr);
        meetingStartTime = view.findViewById(R.id.list_meeting_start_time);
        meetingPriority = view.findViewById(R.id.list_meeting_priority);

    }

}
