package com.example.admin.techfugeeshackathon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    private Button login;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG=login.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        userName = (EditText) findViewById(R.id.userName);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.login);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    startActivity(new Intent(login.this,Reporting.class));

                }
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    public void Login(View view){
        String email = userName.getText().toString();
        String pass = password.getText().toString();
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)){
            Toast.makeText(login.this,"Empty Field(s)",Toast.LENGTH_LONG).show();

        }
        else{
            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Log.e(TAG ,"on complete: Failed="+task.getException().getMessage());
                        Toast.makeText(login.this,"Problem with login",Toast.LENGTH_LONG).show();
                    }



                }
            });


        }

    }
    public void SignUp(View view){
        startActivity(new Intent(this,signup.class));
    }
    public void forgotPassword(View view){
      //  startActivity(new Intent(login.this,MyProfile.class));
    }
}
