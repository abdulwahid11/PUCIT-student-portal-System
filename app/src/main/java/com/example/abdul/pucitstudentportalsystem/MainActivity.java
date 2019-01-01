package com.example.abdul.pucitstudentportalsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.opencv.android.OpenCVLoader;
import org.opencv.osgi.OpenCVInterface;
import org.w3c.dom.Text;

import DTO.User;

public class MainActivity extends AppCompatActivity {

    Button signUpBtn,logInBtn;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private static  final  String PREF_NAME="myprefs";
    private DatabaseReference mRef;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(OpenCVLoader.initDebug()){
            Toast.makeText(MainActivity.this, "OpenCV Loaded Successfully!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "OpenCV Not Loaded!", Toast.LENGTH_SHORT).show();
        }

        mAuth = FirebaseAuth.getInstance();
        prefs=getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null){
            mRef=FirebaseDatabase.getInstance().getReference().child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid());
        }
        String val=prefs.getString("isAdminLoggedIn","");
        if(val.equalsIgnoreCase("true"))
        {
            startActivity(new Intent(MainActivity.this,AdminActivity.class));
            finish();
        }



        signUpBtn =(Button) findViewById(R.id.signUpBtn);
        logInBtn=(Button)findViewById(R.id.signInBtn);
         logInBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this,LogInActivity.class);
                 startActivity(intent);
                 finish();
             }
         });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }@Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){//&&currentUser.isEmailVerified()){
            if(currentUser.isEmailVerified())
            {

            mRef.child("online").setValue(1);
                startActivity(new Intent(MainActivity.this,Home.class));
                finish();
            }
        }

    }

    public String getBatch(String email){
        return email.substring(0,7);
    }


}
