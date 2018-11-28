package com.example.abdul.pucitstudentportalsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import DTO.User;

public class AdminActivity extends AppCompatActivity {

    private Button add;
    private  Button remove;
    private  Button logOut;


    private ValueEventListener mDbListener;

    DatabaseReference databaseReference;

    private SharedPreferences prefs;
    private static  final  String PREF_NAME="myprefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        add=(Button)findViewById(R.id.addCR);
        remove=(Button)findViewById(R.id.removeCR);
        logOut=(Button)findViewById(R.id.signOut);

        databaseReference= FirebaseDatabase.getInstance().getReference();
        prefs=getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent=new Intent(AdminActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogue();
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogue1();

            }
        });
    }
    private void showDialogue(){
        AlertDialog.Builder dialogueBuider=new AlertDialog.Builder(this);
        LayoutInflater inflater=getLayoutInflater();
        final View dialogueView=inflater.inflate(R.layout.alert_dialogue,null);
        dialogueBuider.setView(dialogueView);

        dialogueBuider.setTitle("Add a CR");
        final AlertDialog alertDialog=dialogueBuider.create();
        alertDialog.show();


        final EditText email=(EditText)dialogueView.findViewById(R.id.updatedEditText);
        final Button updateBtn=(Button)dialogueView.findViewById(R.id.updateUser);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(email.getText().toString()))
                {
                    Toast.makeText(AdminActivity.this, "please enter email", Toast.LENGTH_SHORT).show();
                }
                else{
                    updateUser(email.getText().toString());

                    alertDialog.dismiss();

                }

            }
        });



    }
    private void showDialogue1(){
        AlertDialog.Builder dialogueBuider=new AlertDialog.Builder(this);
        LayoutInflater inflater=getLayoutInflater();
        final View dialogueView=inflater.inflate(R.layout.alert_dialogue,null);
        dialogueBuider.setView(dialogueView);

        dialogueBuider.setTitle("remove a CR");
        final AlertDialog alertDialog=dialogueBuider.create();
        alertDialog.show();


        final EditText email=(EditText)dialogueView.findViewById(R.id.updatedEditText);
        final Button updateBtn=(Button)dialogueView.findViewById(R.id.updateUser);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(email.getText().toString()))
                {
                    Toast.makeText(AdminActivity.this, "please enter email", Toast.LENGTH_SHORT).show();
                }
                else{
                    updateUser1(email.getText().toString());

                    alertDialog.dismiss();

                }

            }
        });



    }
    private void updateUser(final String email) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                String batch=getBatch(email);
                DataSnapshot snap= dataSnapshot.child(batch).child("users");
                Iterable<DataSnapshot> itr=snap.getChildren();
                User curr=new User();

                for(DataSnapshot data:itr){


                    curr=data.getValue(User.class);
                    if(curr.getEmail().equals(email)) {
                        curr.setId(data.getKey());
                        break;
                    }

                }
                if(TextUtils.isEmpty(curr.getId()))
                {
                    Toast.makeText(AdminActivity.this, "email not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(curr.getCR()==true) {
                    Toast.makeText(AdminActivity.this, "this user is already a CR", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {

                    DatabaseReference databaseReference1 = dataSnapshot.getRef().child(batch).child("users").child(curr.getId());
                    curr.setCR(true);
                    databaseReference1.setValue(curr);
                    Toast.makeText(AdminActivity.this, "updated successfully", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void updateUser1(final String email) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String batch = getBatch(email);
                DataSnapshot snap = dataSnapshot.child(batch).child("users");
                Iterable<DataSnapshot> itr = snap.getChildren();
                User curr = new User();

                for (DataSnapshot data : itr) {


                    curr = data.getValue(User.class);
                    if (curr.getEmail().equals(email)) {
                        curr.setId(data.getKey());
                        break;
                    }

                }
                if (TextUtils.isEmpty(curr.getId())) {
                    Toast.makeText(AdminActivity.this, "email not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (curr.getCR() == false) {
                    Toast.makeText(AdminActivity.this, "this user is already a student/not a CR", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    DatabaseReference databaseReference1 = dataSnapshot.getRef().child(batch).child("users").child(curr.getId());
                    curr.setCR(false);
                    databaseReference1.setValue(curr);
                    Toast.makeText(AdminActivity.this, "updated successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public String getBatch(String email){
        return email.substring(0,7);
    }

    @Override
    public void onBackPressed() {


            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){


                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }
                    }).create().show();
    }
}
