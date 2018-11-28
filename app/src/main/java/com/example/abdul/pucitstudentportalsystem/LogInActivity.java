package com.example.abdul.pucitstudentportalsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import DTO.Admin;
import DTO.User;

public class LogInActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private TextView forgetPass;
    private Button loginBtn;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private ProgressBar prog;
    private DatabaseReference mUserDataBase;

    private ValueEventListener mDbListener;
    private TextView admin;
    private static final String TAG = "MainActivity";
    private SharedPreferences prefs,myPrefs;
    private static  final  String PREF_NAME="myprefs";

    private static  final  String MY_PREF="prefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        loginBtn=(Button)findViewById(R.id.logInBt);
        email=(EditText)findViewById(R.id.editEmail);
        password=(EditText)findViewById(R.id.editUserPass);
        mAuth = FirebaseAuth.getInstance();
        prog= (ProgressBar)findViewById(R.id.progressbars);
        prefs=getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        myPrefs=getSharedPreferences(MY_PREF,Context.MODE_PRIVATE);
        forgetPass=(TextView)findViewById(R.id.forgetPasswordTxt);
        mUserDataBase=FirebaseDatabase.getInstance().getReference();

        database=FirebaseDatabase.getInstance();
        ref=database.getReference();
        admin=(TextView)findViewById(R.id.adminTxt);
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this,ForgetPassword.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prog.setVisibility(View.VISIBLE);
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

           signIn();

            }
        });
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                prog.setVisibility(View.VISIBLE);
                    final String em=email.getText().toString();
                    final String pass=password.getText().toString();
                    if(em.equals("")||pass.equals(""))
                    {
                        Toast.makeText(LogInActivity.this, "please enter details first", Toast.LENGTH_SHORT).show();

                        prog.setVisibility(View.GONE);
                        return;
                    }
                    else{
                        DatabaseReference myRef= FirebaseDatabase.getInstance().getReference();
                        mDbListener=ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                DataSnapshot snap= dataSnapshot.child("admin");
                                Admin admin=snap.getValue(Admin.class);
                                if(em.equals(admin.getUsername())&&pass.equals(admin.getPassword()))
                                {
                                    SharedPreferences.Editor editor=prefs.edit();
                                    editor.putString("isAdminLoggedIn","true");
                                    editor.apply();
                                    prog.setVisibility(View.GONE);
                                    Intent intent=new Intent(LogInActivity.this,AdminActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();

                                }
                                else
                                {

                                    prog.setVisibility(View.GONE);
                                    Toast.makeText(LogInActivity.this, "wrong username or password", Toast.LENGTH_SHORT).show();
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(LogInActivity.this, "error occured "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                                prog.setVisibility(View.GONE);

                            }
                        });


                    }








            }
        });
    }
    public void faceLogIn(View v) {


        String em=email.getText().toString();
        String pass=password.getText().toString();
        Intent intent = new Intent(LogInActivity.this,FaceRecognitionActivity.class);
        intent.putExtra("email",em);
        intent.putExtra("password",pass);
        startActivity(new Intent(LogInActivity.this,FaceRecognitionActivity.class));
        //Toast.makeText(this, "oh helo !!", Toast.LENGTH_SHORT).show();
    }

    private  void signIn(){
        String em=email.getText().toString();
        String pass=password.getText().toString();
        if(em.equals("")||pass.equals(""))
        {
            Toast.makeText(this, "please fill out all fields", Toast.LENGTH_SHORT).show();

            prog.setVisibility(View.GONE);
            return;
        }
        else{

            mAuth.signInWithEmailAndPassword(em, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                final String batch = getBatch(mAuth.getCurrentUser().getEmail());
                                if (mAuth.getCurrentUser().isEmailVerified()) {
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                                mUserDataBase.child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid()).child("deviceToken").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mDbListener = ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                DataSnapshot snap = dataSnapshot.child(batch).child("users").child(mAuth.getCurrentUser().getUid());

                                                DatabaseReference mRef=FirebaseDatabase.getInstance().getReference().child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid());

                                                mRef.child("online").setValue(1);
                                                User user = snap.getValue(User.class);
                                                if (user.getCR() == false) {

                                                    prog.setVisibility(View.GONE);
                                                    SharedPreferences.Editor editor = myPrefs.edit();
                                                    editor.clear();
                                                    editor.putString("CR", "false");
                                                    editor.apply();
                                                    Log.d(TAG, "signInWithEmail:success");
                                                    Intent intent = new Intent(LogInActivity.this, Home.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {

                                                    prog.setVisibility(View.GONE);
                                                    SharedPreferences.Editor editor = myPrefs.edit();
                                                    editor.clear();
                                                    editor.putString("CR", "true");
                                                    editor.apply();
                                                    Log.d(TAG, "signInWithEmail:success");
                                                    Intent intent = new Intent(LogInActivity.this, Home.class);
                                                    startActivity(intent);
                                                    finish();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {


                                            }
                                        });

                                    }
                                });

                                    // Sign in success, update UI with the signed-in user's information


                               }
                                else {
                                    Toast.makeText(LogInActivity.this, "please verify your email", Toast.LENGTH_SHORT).show();
                                    prog.setVisibility(View.GONE);
                                }

                            } else {
                                // If sign in fails, display a message to the user.

                                prog.setVisibility(View.GONE);
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LogInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });

        }
    }



    public String getBatch(String email){
        return email.substring(0,7);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbListener!=null)
        {

            ref.removeEventListener(mDbListener);

        }

    }


}
