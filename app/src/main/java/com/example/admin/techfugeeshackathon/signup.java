package com.example.admin.techfugeeshackathon;

import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import java.util.*;
import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {

    Spinner user;
    Button signup;
    EditText email,password,cpassword,username;
    String roleString;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG=signup.class.getName();
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth= FirebaseAuth.getInstance();

        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        cpassword=(EditText)findViewById(R.id.cPassword);
        username=(EditText)findViewById(R.id.username);
        signup=(Button)findViewById(R.id.signup);

        user=(Spinner)findViewById(R.id.user);
        final String usertypes[]={"HSV","Resident"};
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,usertypes);
        user.setAdapter(adapter);

        user.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                roleString=user.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                    startActivity(new Intent(signup.this,Reporting.class));
                   // Toast.makeText(this,"Succesful Signup",Toast.LENGTH_LONG).show();
                    //uid = firebaseAuth.getCurrentUser().getUid();

                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    public void SignUp(View view){
        String Password=password.getText().toString();
        String PasswordConfirmation = cpassword.getText().toString();

        if(TextUtils.isEmpty(email.getText().toString())||TextUtils.isEmpty(password.getText().toString())||TextUtils.isEmpty(username.getText().toString())||TextUtils.isEmpty(roleString)){
            Toast.makeText(this,"Empty Field(s). Please fill out all the fields",Toast.LENGTH_LONG).show();

        }
        else {
            if (!Password.equals(PasswordConfirmation)) {
                Toast.makeText(this, "Your Passwords Don't Match!!", Toast.LENGTH_LONG).show();
            } else {
                String Email = email.getText().toString();
                roleString = user.getSelectedItem().toString();

                mAuth.createUserWithEmailAndPassword(Email, Password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(signup.this,"SignUp Successful",Toast.LENGTH_LONG).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    uid = user.getUid();
                                    //databaseUpdate(DepartmentString,roleString,UsernameString);



                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.e(TAG, "createUserWithEmail:failure" + task.getException());
                                    Toast.makeText(signup.this, "Authentication failed.", Toast.LENGTH_SHORT).show();


                                }

                            }
                        });






            }
        }
    }
    public void databaseUpdate(String role, String username){
        FirebaseDatabase mydatabase = FirebaseDatabase.getInstance();
        DatabaseReference myref = mydatabase.getReference();
        Map<String,String> userdata = new HashMap<String,String>();
        userdata.put("Role",role);
        userdata.put("UserName",username);
        myref.child("Users").child(uid).setValue(userdata);
    }
}
