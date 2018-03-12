package ru.ssau.sanya.mettings.Adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import ru.ssau.sanya.mettings.Entity.Meeting;
import ru.ssau.sanya.mettings.R;

public class MeetingListAdapter extends RecyclerView.Adapter<MeetingsViewHolder>  implements Filterable{
    private  Context context;
    private  List<Meeting> meetingList;
    private  List<Meeting> defaultMeetingList;


    public MeetingListAdapter(Context context, List<Meeting> meetingList) {
        this.context = context;
        this.meetingList = meetingList;
        this.defaultMeetingList = meetingList;
    }

    public Meeting getItem(int i) {
        return meetingList.get(i);
    }
    @Override
    public MeetingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.meetings_list_layout, parent, false);

        return new MeetingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MeetingsViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);

        holder.meetingPriority.setText(meeting.getPriority());
        holder.meetingName.setText(meeting.getName());
        holder.meetingDescr.setText(meeting.getDescription());
        holder.meetingStartTime.setText(meeting.getStartTime());
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                meetingList=defaultMeetingList;
                if (constraint != null && constraint.length() > 0) {
                    List<Meeting> filterList = new ArrayList<>();
                    for (int i = 0; i < meetingList.size(); i++) {
                        if ((meetingList.get(i).getDescription().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                            filterList.add(meetingList.get(i));
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                } else {
                    results.count = meetingList.size();
                    results.values = meetingList;
                }
                return results;

            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                meetingList = (ArrayList<Meeting>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
