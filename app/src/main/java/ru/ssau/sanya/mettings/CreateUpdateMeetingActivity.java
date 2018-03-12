package ru.ssau.sanya.mettings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ru.ssau.sanya.mettings.Entity.Meeting;


public class CreateUpdateMeetingActivity extends AppCompatActivity {
    private static final String TAG = CreateUpdateMeetingActivity.class.getName();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;

    private EditText name;
    private EditText description;
    private TextView startTime;
    private TextView startDate;
    private TextView endDate;
    private TextView endTime;
    private Spinner priority;

    Button btnOk;
    AlertDialog.Builder ad;

    private String meetingUid;
    private DatabaseReference mMeetingRef;
    private boolean updateFlag=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update_meeting);
        //toolbar
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new_contact);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //init firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            name = (EditText)findViewById(R.id.meetingNameEdit);
            description = (EditText)findViewById(R.id.meetingDescrEdit);
            startDate = (TextView) findViewById(R.id.startDateMeeting);
            startTime = (TextView) findViewById(R.id.startTimeMeeting);
            endDate = (TextView) findViewById(R.id.endDateMeeting);
            endTime = (TextView) findViewById(R.id.endTimeMeeting);
            priority = (Spinner) findViewById(R.id.priorityVariants);


        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b!=null) {
            if (b.get("meeting_uid")!=null) {
                meetingUid = (String) b.get("meeting_uid");
                mMeetingRef = mDatabase.child("meetings").child(meetingUid);
            }
            if (b.get("UPDATE_FLAG")!=null){
                updateFlag=(Boolean)b.get("UPDATE_FLAG");
            }
        }
        if (updateFlag) {
            final ValueEventListener meetingListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //String tmp = dataSnapshot.child("meetings").getValue();
                    Meeting meeting = dataSnapshot.getValue(Meeting.class);
                    if (meeting != null) {
                        //name.setText()
                        name.setText(meeting.getName());
                        description.setText(meeting.getDescription());
                        startDate.setText(meeting.getStartDate());
                        startTime.setText(meeting.getStartTime());
                        endDate.setText(meeting.getEndDate());
                        endTime.setText(meeting.getEndTime());
                        //for change spinner position to already exist variant
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                                .createFromResource(CreateUpdateMeetingActivity.this,
                                        R.array.priority_list, android.R.layout.simple_spinner_item);
                        priority.setAdapter(adapter);
                        if (!meeting.getPriority().equals(null)) {
                            int spinnerPosition = adapter.getPosition(meeting.getPriority());
                            priority.setSelection(spinnerPosition);
                        }


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // [START_EXCLUDE]
                    Toast.makeText(CreateUpdateMeetingActivity.this, "Failed to load meeting.",
                            Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]

                }
            };
            mMeetingRef.addValueEventListener(meetingListener);
        }

        String myFormat = "MM/dd/yy"; //In which you need put here
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener startDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                calendar.set(Calendar.YEAR,i);
                calendar.set(Calendar.MONTH,i1);
                calendar.set(Calendar.DAY_OF_MONTH,i2);
                startDate.setText(sdf.format(calendar.getTime()));


            }
        };

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(CreateUpdateMeetingActivity.this,
                        startDatePicker,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        final DatePickerDialog.OnDateSetListener endDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                calendar.set(Calendar.YEAR,i);
                calendar.set(Calendar.MONTH,i1);
                calendar.set(Calendar.DAY_OF_MONTH,i2);
                endDate.setText(sdf.format(calendar.getTime()));


            }
        };

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(CreateUpdateMeetingActivity.this,
                        endDatePicker,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(CreateUpdateMeetingActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                        startTime.setText(i + ":" + i1);


                                    }
                                },hour,minute,true);//true - 24 format
                timePickerDialog.setTitle(R.string.select_time);
                timePickerDialog.show();
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog =
                        new TimePickerDialog(CreateUpdateMeetingActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                        endTime.setText(i + ":" + i1);

                                    }
                                },hour,minute,true);//true - 24 format
                timePickerDialog.setTitle(R.string.select_time);
                timePickerDialog.show();
            }
        });

        btnOk= (Button)findViewById(R.id.btnCreateNewMeeting);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ad = new AlertDialog.Builder(CreateUpdateMeetingActivity.this);
                ad.setTitle(R.string.accept_action);
                if (updateFlag)
                    ad.setMessage(R.string.accept_update_meeting);
                else
                    ad.setMessage(R.string.accept_add_new_meeting);
                ad.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {



                                final Meeting meeting = new Meeting();
                                meeting.setName(name.getText().toString());
                                meeting.setDescription(description.getText().toString());

                                //Spinner spinner = (Spinner) findViewById(R.id.priorityVariants);
                                meeting.setPriority(priority.getSelectedItem().toString());
                                meeting.setStartDate(startDate.getText().toString());
                                meeting.setEndTime(endTime.getText().toString());
                                meeting.setStartTime(startTime.getText().toString());
                                meeting.setEndDate(endDate.getText().toString());

                                if (!meeting.getName().isEmpty() && !meeting.getDescription().isEmpty() &&
                                        !meeting.getStartDate().isEmpty() && !meeting.getStartTime().isEmpty()
                                        && !meeting.getEndDate().isEmpty() && !meeting.getEndTime().isEmpty()
                                        && !meeting.getPriority().isEmpty()) {
                                    if (updateFlag){
                                        meeting.setUid(meetingUid);
                                        mMeetingRef.setValue(meeting);
                                        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_meeting_updated, Snackbar.LENGTH_LONG)
                                                .show();
                                    }
                                    else {
                                        //create new meeting
                                        DatabaseReference df = mDatabase.child("meetings").push();
                                        meeting.setUid(df.getKey());
                                        df.setValue(meeting);
                                    }

                                    Intent intent = new Intent(CreateUpdateMeetingActivity.this, MainActivity.class);
                                    //newContactName=(EditText)findViewById(R.id.newContactEditText);
                                    //intent.putExtra("newContactName",newContactName.getText().toString());
                                    startActivity(intent);
                                }
                                else{
                                    Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_fill_all_fields, Snackbar.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
                ad.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                ad.show();

            }
        });

    }

    // Заглушка, работа с меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        if ((name!=null && name.isActivated()) || (description!=null && description.isActivated())) {
            //inputMessage.clearFocus();
            name.requestFocus(EditText.FOCUS_DOWN);
            description.requestFocus(EditText.FOCUS_DOWN);
        }
        else{
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }


    }
    private void loadLogInView(){
        Intent intent = new Intent(this,LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //This navigates to the Login view and clears the activity stack
    }
}
