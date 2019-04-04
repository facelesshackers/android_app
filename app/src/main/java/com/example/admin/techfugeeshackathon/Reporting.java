package com.example.admin.techfugeeshackathon;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.content.ActivityNotFoundException;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.net.*;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.*;


public class Reporting extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private final int PICK_IMAGE_REQUEST=71;

    private FusedLocationProviderClient fusedLocationClient;

    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText question;
    Button ask,speak;
    Location loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporting);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        question = (EditText)findViewById(R.id.questionText);
        ask = (Button)findViewById(R.id.askquestionbutton);
        speak = (Button)findViewById(R.id.speakbutton);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(Reporting.this,login.class));

                }
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    public void saveQuestion(View view){
        String questiontext = question.getText().toString();
        updateQuestion(questiontext);
        Toast.makeText(this,"Your Question has been added Successfully",Toast.LENGTH_LONG).show();

    }
    //Method to add question data to database
    public void updateQuestion(String content){

        //getting location


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            loc = location;
                        } else {
                            Toast.makeText(Reporting.this, "Can not get your location", Toast.LENGTH_LONG).show();
                        }

                    }
                });

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();
        FirebaseDatabase mydatabase = FirebaseDatabase.getInstance();
        DatabaseReference myref = mydatabase.getReference();
        String key = myref.push().getKey();
        Map<String,Object> userdata = new HashMap<String,Object>();
        userdata.put("Content",content);
        userdata.put("Creatorid",uid);
        userdata.put("Timestamp", ServerValue.TIMESTAMP);
        userdata.put("Location",loc);
        myref.child("Questions").child(key).setValue(userdata);

        if(filePath!=null){
            StorageReference riversRef = storageReference.child("images/");

            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                          //  Uri downloadUrl = taskSnapshot.get
                            Toast.makeText(Reporting.this,"File Uploaded",Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Toast.makeText(Reporting.this,"File not Uploaded",Toast.LENGTH_LONG).show();

                        }
                    });
        }


    }
    public void Speak(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"say something");
        try{
            startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException a){
            Toast.makeText(Reporting.this,"Speech not supported. Please install Google Speech to Text",Toast.LENGTH_LONG).show();
        }
    }
    //Receiving speech input and actions to perform based on the speech
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:{
                if(resultCode==RESULT_OK && null!=data) {
                    if(data!=null){
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        question.setText(result.get(0));
                    }
                    else{
                        Toast.makeText(Reporting.this,"No data found",Toast.LENGTH_LONG).show();
                    }

                }
                break;
            }
            case PICK_IMAGE_REQUEST:{
                if (data.getData() != null) {
                    filePath = data.getData();
                }else{
                    Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    //method to select image
    public void chooseImage(View view){
        //for greater than lolipop versions we need the permissions asked on runtime
        //so if the permission is not available user will go to the screen to allow storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }
        //creating an intent for image chooser
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


}
