package ru.ssau.sanya.mettings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.ssau.sanya.mettings.Entity.Meeting;

public class SaveToCSVActivity extends AppCompatActivity {
    private static String TAG = SaveToCSVActivity.class.getName();
    private DatabaseReference mDatabase;
    EditText rootPath;
    EditText fileName;
    Button btnSave;

    String defaultPath = Environment.getExternalStorageDirectory() + "/csv/";
    String defaultFilename = "meetings.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_to_csv);
        rootPath = findViewById(R.id.root_path_csv_field);
        fileName = findViewById(R.id.csv_file_name_field);
        rootPath.setText(defaultPath);
        fileName.setText(defaultFilename);

        btnSave = findViewById(R.id.btnSaveToCSVFile);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path;
                String name;
                Intent intent = new Intent(SaveToCSVActivity.this,MainActivity.class);
                if (rootPath.getText().toString().length() > 0)
                    path = rootPath.getText().toString();
                else
                    path = defaultPath;
                if (fileName.getText().toString().length() > 0)
                    name = fileName.getText().toString();
                else
                    name = defaultFilename;

                if (ContextCompat.checkSelfPermission(SaveToCSVActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(SaveToCSVActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {


                    } else {


                        ActivityCompat.requestPermissions(SaveToCSVActivity.this,
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                        );
                    }
                }
                else {
                    intent.putExtra("path_csv", path);
                    intent.putExtra("name_csv", name);
                    startActivity(intent);
                }




            }
        });

    }



}



