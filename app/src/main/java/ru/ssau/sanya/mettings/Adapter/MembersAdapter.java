package ru.ssau.sanya.mettings.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ru.ssau.sanya.mettings.Entity.Member;

public class MembersAdapter extends BaseAdapter implements Adapter {

    private Context context;
    private List<Member> members;

    public MembersAdapter(Context context, List<Member> members) {
        this.context = context;
        this.members = members;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Member getItem(int i) {
        return members.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TwoLineListItem twoLineListItem;
        if (view ==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2,null);
        }
        else {
            twoLineListItem = (TwoLineListItem) view;
        }
        TextView text1 = twoLineListItem.getText1();
        TextView text2 = twoLineListItem.getText2();

        text1.setText(members.get(i).getFio());
        text2.setText(members.get(i).getPosition());

        return twoLineListItem;

    }
}
