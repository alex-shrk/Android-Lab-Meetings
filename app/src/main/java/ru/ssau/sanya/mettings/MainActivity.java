package ru.ssau.sanya.mettings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ru.ssau.sanya.mettings.Adapter.MeetingListAdapter;
import ru.ssau.sanya.mettings.Adapter.MeetingsViewHolder;
import ru.ssau.sanya.mettings.Entity.Meeting;
import ru.ssau.sanya.mettings.Service.NotificationService;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mMeetRef;
    private DatabaseReference mDatabase;
    private String mUserId;
    private FloatingActionButton fab;
    private FirebaseRecyclerAdapter meetingFirebaseAdapter;
    private RecyclerView meetingsRecyclerView;
    private boolean useSearchAdapter =false;
    //for search by description
    private MeetingListAdapter meetingListAdapter;

    private List<Meeting> meetingList;
    //private Map<String,Member> membersMap;

    public static Intent newIntent(Context context){
        return new Intent(context,MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        meetingList = new ArrayList<>();
       // membersMap = new HashMap<>();
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/


        NotificationService.setServiceAlarm(MainActivity.this,true);

        //floationActionButton
        fab = findViewById(R.id.fabCreateMeeting);
        //fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateUpdateMeetingActivity.class);
                startActivity(intent);
            }
        });


        meetingsRecyclerView = findViewById(R.id.recycler_view_meetings);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        meetingsRecyclerView.setLayoutManager(linearLayoutManager);


        //init firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            //user not logged
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();

            String myFormat = "MM/dd/yy";
            final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            final Calendar calendar = Calendar.getInstance();
            Query meetingsQuery = mDatabase
                    .child("meetings")
                    .orderByChild("startDate")
                    .equalTo(sdf.format(calendar.getTime()))
                    .limitToLast(50);

            FirebaseRecyclerOptions<Meeting> meetingsOptions = new FirebaseRecyclerOptions.Builder<Meeting>()
                    .setQuery(meetingsQuery, Meeting.class)
                    .build();
            meetingFirebaseAdapter = new FirebaseRecyclerAdapter<Meeting, MeetingsViewHolder>(meetingsOptions){

                @Override
                public MeetingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.meetings_list_layout, parent, false);
                    return new MeetingsViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(MeetingsViewHolder holder, int position, Meeting model) {
                    holder.meetingPriority.setText(model.getPriority());
                    holder.meetingName.setText(model.getName());
                    holder.meetingDescr.setText(model.getDescription());
                    holder.meetingStartTime.setText(model.getStartTime());

                }



            };
            meetingsRecyclerView.setAdapter(meetingFirebaseAdapter);
            meetingsRecyclerView.addOnItemTouchListener(
                    new RecyclerTouchListener(this, meetingsRecyclerView, new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            Intent intent = new Intent(MainActivity.this, AboutMeetingActivity.class);
                            Meeting meeting;
                            if (useSearchAdapter)
                                meeting = meetingListAdapter.getItem(position);
                            else
                                meeting= (Meeting)meetingFirebaseAdapter.getItem(position);
                            intent.putExtra("meeting_uid", meeting.getUid());
                            startActivity(intent);
                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));


        }


    }

    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editPath = (EditText) promptView.findViewById(R.id.csv_path_input_edit);
        final EditText editName = (EditText) promptView.findViewById(R.id.csv_name_input_edit);
        editPath.setText(Environment.getExternalStorageDirectory() + "/csv/");
        editName.setText(R.string.meeting_filename);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            if (editPath.getText().toString().length()>0 && editName.getText().toString().length()>0)
                                saveCsv(editPath.getText().toString(),editName.getText().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void saveCsv(String path, String filename) throws IOException {



        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path, filename);

        file.createNewFile();

        if (file.exists()) {

            CSVWriter writer = new CSVWriter(new FileWriter(file), ',');
            int cntFields = 7;
            for (Meeting meeting : meetingList) {
                String[] meetValues = new String[cntFields];
                meetValues[0] = "Name:" + meeting.getName();
                meetValues[1] = "Description:" + meeting.getDescription();
                meetValues[2] = "StartDate:" + meeting.getStartDate();
                meetValues[3] = "StartTime:" + meeting.getStartTime();
                meetValues[4] = "EndDate:" + meeting.getEndDate();
                meetValues[5] = "EndTime:" + meeting.getEndTime();
                meetValues[6] = "Priority:" + meeting.getPriority();
                writer.writeNext(meetValues);
            }
            writer.close();
        }
        if (file.length()>0)
            Toast.makeText(this, getString(R.string.meeting_saved) + path + filename,Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.meeting_save_failed),Toast.LENGTH_LONG).show();


    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert cm != null;
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //This navigates to the Login view and clears the activity stack
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mFirebaseAuth.signOut();
            loadLogInView();
        }
        if (id == R.id.action_search_by_descr) {
            searchMode(true);

            //Snackbar.make(getWindow().getDecorView().getRootView(), "Search by descr", Snackbar.LENGTH_LONG).show();
            final MenuItem search = item;
            final SearchView searchView = (SearchView) search.getActionView();
            search(searchView);

            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b){
                        searchMode(false);
                        search.collapseActionView();
                        searchView.setQuery("",false);

                    }
                }
            });
            meetingListAdapter.notifyDataSetChanged();
            return true;
        }
        if (id == R.id.action_save_to_csv){
            showInputDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                meetingListAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                meetingListAdapter.getFilter().filter(newText);
                meetingListAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    /*private void setMeetingList(){
        DatabaseReference mRef = mDatabase;
        //Log.i(TAG+"CSV",mRef.child("meetings").toString());
        mRef.child("meetings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.i(TAG+"meeting value",ds.getValue(Meeting.class).toString());
                            meetingList.add(ds.getValue(Meeting.class));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }
    private void setMembersList(){
        DatabaseReference mRef = mDatabase;
        //Log.i(TAG+"CSV",mRef.child("meetings").toString());
        mRef.child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.i(TAG+"member key",ds.getKey());
                            Log.i(TAG+"member value",ds.getValue(Member.class).toString());
                            membersMap.put(ds.getKey(),ds.getValue(Member.class));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }*/


    private void searchMode(boolean tumbler){
        if (tumbler) {
            meetingsRecyclerView.setAdapter(meetingListAdapter);
            useSearchAdapter =true;
            fab.hide();
        }
        else{
            meetingsRecyclerView.setAdapter(meetingFirebaseAdapter);
            useSearchAdapter = false;
            fab.show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!isNetworkAvailableAndConnected()) {
            Toast.makeText(this, R.string.internet_connection_not_available,Toast.LENGTH_LONG).show();
        }
        if (meetingFirebaseAdapter !=null)
            meetingFirebaseAdapter.startListening();
        mMeetRef = mDatabase.child("meetings");
        //Log.i(TAG+"CSV",mRef.child("meetings").toString());
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (meetingList.size()<dataSnapshot.getChildrenCount())
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.i(TAG + "meeting value", ds.getValue(Meeting.class).toString());
                        meetingList.add(ds.getValue(Meeting.class));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMeetRef.addValueEventListener(listener);

        meetingListAdapter = new MeetingListAdapter(getApplicationContext(),meetingList);



    }



    @Override
    protected void onStop() {
        super.onStop();
        if (meetingFirebaseAdapter !=null)
            meetingFirebaseAdapter.stopListening();
    }
}
