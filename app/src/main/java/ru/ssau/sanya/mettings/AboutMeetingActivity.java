package ru.ssau.sanya.mettings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ru.ssau.sanya.mettings.Adapter.MembersAdapter;
import ru.ssau.sanya.mettings.Entity.Meeting;
import ru.ssau.sanya.mettings.Entity.Member;

public class AboutMeetingActivity extends AppCompatActivity {
    private static final String TAG = AboutMeetingActivity.class.getName();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private DatabaseReference mMeetingRef;
    private DatabaseReference mMembersRef;
    private String uid;
    private ValueEventListener mMeetingListener;
    private ValueEventListener mMemberListener;

    // --Commented out by Inspection (16.12.2017 1:55):private TextView name;
    private TextView descr;
    private TextView startDate;
    private TextView startTime;
    private TextView endDate;
    private TextView endTime;
    private TextView priority;
    private ListView membersList;

    private List<String> membersUid;
    private boolean alreadyAccept = false;

    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_meeting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about_meeting);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab_accept_meeting);

        //name = findViewById(R.id.about_meetingName);
        descr = findViewById(R.id.about_meetingDescr);
        startDate = findViewById(R.id.about_startDateMeeting);
        startTime = findViewById(R.id.about_startTimeMeeting);
        endDate  = findViewById(R.id.about_endDateMeeting);
        endTime = findViewById(R.id.about_endTimeMeeting);
        priority = findViewById(R.id.about_priorityMeeting);
        membersList = findViewById(R.id.about_list_members);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        final String meeting_uid = "meeting_uid";
        if (b!=null) {
            if (b.get(meeting_uid)!=null) {
                uid = (String) b.get(meeting_uid);
                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mMeetingRef = mDatabase.child("meetings").child(uid);
                mMembersRef = mDatabase.child("members");

            }
        }

        /*mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMeetingRef = mDatabase.child("meetings").child(uid);
        mMembersRef = mDatabase.child("members");*/

        final ValueEventListener meetingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String tmp = dataSnapshot.child("meetings").getValue();
                Meeting meeting = dataSnapshot.getValue(Meeting.class);
                if (meeting!=null) {
                    //name.setText()
                    setTitle(meeting.getName());
                    //name.setText(meeting.getName());
                    descr.setText(meeting.getDescription());
                    startDate.setText(meeting.getStartDate());
                    startTime.setText(meeting.getStartTime());
                    endDate.setText(meeting.getEndDate());
                    endTime.setText(meeting.getEndTime());
                    priority.setText(meeting.getPriority());
                    membersUid = meeting.getMembersUid();
                    if (meeting.getMemberList()!=null)
                        alreadyAccept = meeting.getMemberList().containsValue(mFirebaseUser.getUid());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(AboutMeetingActivity.this, "Failed to load meeting.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]

            }
        };

        mMeetingRef.addValueEventListener(meetingListener);
// Keep copy of post listener so we can remove it when app stops
        mMeetingListener = meetingListener;




        mMemberListener = getMembersListener();
        mMembersRef.addValueEventListener(mMemberListener); 




        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                mMeetingRef.child("memberList").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!alreadyAccept) {
                            mMeetingRef.child("memberList").push().setValue(mFirebaseUser.getUid());
                            Snackbar.make(view, "You accept meeting", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            mMemberListener = getMembersListener();
                            mMembersRef.addValueEventListener(mMemberListener);


                        }
                        else{
                            Snackbar.make(view, "You already accept meeting", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Snackbar.make(view, "Oops... error", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                });




            }

        });



    }
    private ValueEventListener getMembersListener(){
        ValueEventListener membersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String tmp = dataSnapshot.child("meetings").getValue();
                List<Member> memberList = new ArrayList<>();
                for (String memUid:membersUid){
                    memberList.add(dataSnapshot.child(memUid).getValue(Member.class));
                }

                membersList.setAdapter(new MembersAdapter(AboutMeetingActivity.this,memberList));



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "aboutMeetingActivity:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(AboutMeetingActivity.this, "Failed to load meeting.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]

            }
        };
        return membersListener;
    }
    @Override
    protected void onStop() {
        super.onStop();

        if (mMeetingListener !=null){
            mMeetingRef.removeEventListener(mMeetingListener);
        }
        if (mMemberListener !=null){
            mMembersRef.removeEventListener(mMemberListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about_meeting,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_meeting) {
            mMeetingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        dataSnapshot.getRef().removeValue();
                    }
                    Intent intent = new Intent(AboutMeetingActivity.this,MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Oops... error", Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        }
        if (id == R.id.action_update_meeting){
            Intent intent = new Intent(this,CreateUpdateMeetingActivity.class);
            intent.putExtra("meeting_uid",uid);
            intent.putExtra("UPDATE_FLAG",true);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
