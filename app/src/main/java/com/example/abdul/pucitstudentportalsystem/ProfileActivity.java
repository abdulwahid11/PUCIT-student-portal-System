package com.example.abdul.pucitstudentportalsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import DTO.Friend;
import DTO.User;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profile;
    private TextView name,email;
    private Button sendReq,decReq;
    private DatabaseReference mReference;
    private ValueEventListener mDbListener;

    private ValueEventListener mDbListener1;

    private ValueEventListener mDbListener2;
    private ProgressDialog mProgressDialogue;
    private int current_state;
    String user_id;
    private DatabaseReference requestDatabase;
    private DatabaseReference frienDataBase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference ref;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    User otherUser;
    User current;
    DatabaseReference mRef;
    SharedPreferences prefs;
    private static  String PREF_NAME="notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sendReq=(Button)findViewById(R.id.profile_send_req_btn);
        decReq=(Button)findViewById(R.id.profile_decline_btn);
        name=(TextView)findViewById(R.id.profile_displayName);
        profile=(ImageView)findViewById(R.id.profile_image);
        email=(TextView)findViewById(R.id.email);
        current_state=0;
        mAuth=FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        mReference= FirebaseDatabase.getInstance().getReference();
        requestDatabase=FirebaseDatabase.getInstance().getReference().child(getBatch(currentUser.getEmail())).child("Friend_request");
        if(mAuth.getCurrentUser()!=null)
        {
            mRef=FirebaseDatabase.getInstance().getReference().child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid());

        }

        ref=FirebaseDatabase.getInstance().getReference().child(getBatch(currentUser.getEmail()));

        notificationDatabase=FirebaseDatabase.getInstance().getReference().child(getBatch(currentUser.getEmail())).child("RequestNotifications");
        frienDataBase=FirebaseDatabase.getInstance().getReference().child(getBatch(currentUser.getEmail())).child("Friends");
        mProgressDialogue=new ProgressDialog(this);
        mProgressDialogue.setTitle("Loading");
        mProgressDialogue.setMessage("Please Wait");
        mProgressDialogue.setCanceledOnTouchOutside(false);
        mProgressDialogue.show();
        decReq.setVisibility(View.INVISIBLE);
        decReq.setEnabled(false);
        user_id=getIntent().getStringExtra("user_id");

       // Toast.makeText(this, user_id, Toast.LENGTH_LONG).show();

        mDbListener=mReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {




                final User user=dataSnapshot.child(getBatch(currentUser.getEmail())).child("users").child(user_id).getValue(User.class);

                final User user2=dataSnapshot.child(getBatch(currentUser.getEmail())).child("users").child(currentUser.getUid()).getValue(User.class);
                otherUser=user;
                current=user2;


                name.setText(user.getUserName());
                email.setText(user.getEmail());

                if(!user.getThumbPath().equals(""))
                {
                    Picasso.get().load(user.getThumbPath()).placeholder(R.drawable.default_avatar).into(profile);
                }

               mDbListener1= requestDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("recieved"))
                            {
                                current_state=2;
                                sendReq.setText("Accept Request");

                                decReq.setVisibility(View.VISIBLE);
                                decReq.setEnabled(true);

                            }
                            else if(req_type.equals("sent")){
                                current_state=1;
                                sendReq.setText("cancel request");
                            }

                            mProgressDialogue.dismiss();
                        }
                        else{
                            mDbListener2=frienDataBase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        current_state=3;
                                        sendReq.setText("remove this person");
                                        decReq.setVisibility(View.VISIBLE);
                                        decReq.setEnabled(true);
                                        decReq.setText("send message");



                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialogue.dismiss();

                                }
                            });

                            mProgressDialogue.dismiss();
                        }

                        mProgressDialogue.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        mProgressDialogue.dismiss();

                    }
                });

            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();


                mProgressDialogue.dismiss();
            }
        });
        sendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReq.setEnabled(false);
             /*   if(mDbListener!=null){

                    mReference.removeEventListener(mDbListener);
                }
                if(mDbListener1!=null){

                    requestDatabase.removeEventListener(mDbListener1);
                }
                if(mDbListener2!=null){

                    frienDataBase.removeEventListener(mDbListener2);
                }*/
                switch (current_state)
                {
                    case 0:
                        DatabaseReference notificationRef=ref.child("RequestNotifications").push();
                        String notificationID=notificationRef.getKey();

                        Map notificationData=new HashMap<>();
                        notificationData.put("timestamp", ServerValue.TIMESTAMP);

                        notificationData.put("type","request");


                        Map requestMap1=new HashMap();
                        requestMap1.put("request_type","sent");

                        requestMap1.put("name",otherUser.getUserName());
                        requestMap1.put("imageUrl",otherUser.getThumbPath());


                        Map requestMap=new HashMap();
                        requestMap.put("request_type","recieved");

                        requestMap.put("name",current.getUserName());
                        requestMap.put("imageUrl",current.getThumbPath());


                        Map requestMajorMap =new HashMap();
                        requestMajorMap.put("Friend_request/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+user_id,requestMap1);

                        requestMajorMap.put("Friend_request/"+user_id+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid(),requestMap);

                        requestMajorMap.put("RequestNotifications/"+user_id+"/"+currentUser.getUid(),notificationData);

                        ref.updateChildren(requestMajorMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    Toast.makeText(ProfileActivity.this, "not inserted", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    current_state=1;
                                    sendReq.setText("Cancel Request");
                                    sendReq.setEnabled(true);
                                }



                            }
                        });
                        return;
                    case 1:
                        Map RemoverequestMap=new HashMap();
                        RemoverequestMap.put("Friend_request/"+currentUser.getUid()+"/"+user_id,null);
                        RemoverequestMap.put("Friend_request/"+user_id+"/"+currentUser.getUid(),null);
                        ref.updateChildren(RemoverequestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError==null)
                                {

                                    sendReq.setEnabled(true);
                                    current_state=0;
                                    sendReq.setText("Send Request");

                                }
                                else{
                                    Toast.makeText(ProfileActivity.this, "error occured", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        return;
                    case 2:
                        sendReq.setEnabled(false);
                        final String currentDate=DateFormat.getDateTimeInstance().format(new Date());


                        Map current_friend=new HashMap();
                        current_friend.put("name",current.getUserName());
                        current_friend.put("date",currentDate);
                        current_friend.put("imageUrl",current.getThumbPath());






                        Map other=new HashMap();
                        other.put("name",otherUser.getUserName());
                        other.put("date",currentDate);
                        other.put("imageUrl",otherUser.getThumbPath());




                        Map notificationData1=new HashMap<>();
                        notificationData1.put("timestamp", ServerValue.TIMESTAMP);

                        notificationData1.put("type","accepted");


                        Map friend =new HashMap();
                        friend.put("Friends/"+currentUser.getUid()+"/"+user_id,other);
                        friend.put("Friends/"+user_id+"/"+currentUser.getUid(),current_friend);
                        friend.put("RequestNotifications/"+user_id+"/"+currentUser.getUid(),notificationData1);
                        friend.put("Friend_request/"+currentUser.getUid()+"/"+user_id,null);
                        friend.put("Friend_request/"+user_id+"/"+currentUser.getUid(),null);

                        ref.updateChildren(friend, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError==null)
                                {
                                    sendReq.setEnabled(true);
                                    current_state=3;
                                    sendReq.setText("remove this person");
                                    decReq.setVisibility(View.VISIBLE);
                                    decReq.setEnabled(true);
                                    decReq.setText("send message");

                                }
                                else{
                                    Log.d("ERROR", "onComplete: "+databaseError.getMessage());
                                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }


                            }
                        });

                        return;
                    case 3:
                        Map unFriendsMap=new HashMap();
                        unFriendsMap.put("Friends/"+currentUser.getUid()+"/"+user_id,null);
                        unFriendsMap.put("Friends/"+user_id+"/"+currentUser.getUid(),null);

                        unFriendsMap.put("messages/"+currentUser.getUid()+"/"+user_id,null);
                       // unFriendsMap.put("messages/"+user_id+"/"+currentUser.getUid(),null);

                        unFriendsMap.put("Chat/"+currentUser.getUid()+"/"+user_id,null);
                        //unFriendsMap.put("Chat/"+user_id+"/"+currentUser.getUid(),null);


                        ref.updateChildren(unFriendsMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError==null)
                                {
                                    sendReq.setEnabled(true);
                                    current_state=0;
                                    sendReq.setText("Send Request");
                                    decReq.setVisibility(View.INVISIBLE);
                                    decReq.setEnabled(false);

                                }
                                else{
                                    Toast.makeText(ProfileActivity.this, "error occured", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        return;

                        default:
                            return;
                }

            }
        });
        decReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_state==3){


                    if(mDbListener!=null){

                        mReference.removeEventListener(mDbListener);
                    }
                    if(mDbListener1!=null){

                        requestDatabase.removeEventListener(mDbListener1);
                    }
                    if(mDbListener2!=null){

                        frienDataBase.removeEventListener(mDbListener2);
                    }


                    Intent intent= new Intent(ProfileActivity.this,ChatActivity.class);
                    intent.putExtra("user_id",user_id);
                    intent.putExtra("user_name",otherUser.getUserName());
                    startActivity(intent);


                }
                else{
                    Map RemoverequestMap=new HashMap();
                    RemoverequestMap.put("Friend_request/"+currentUser.getUid()+"/"+user_id,null);
                    RemoverequestMap.put("Friend_request/"+user_id+"/"+currentUser.getUid(),null);
                    ref.updateChildren(RemoverequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null)
                            {

                                sendReq.setEnabled(true);
                                current_state=0;
                                sendReq.setText("Send Request");
                                decReq.setVisibility(View.INVISIBLE);
                                decReq.setEnabled(false);

                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "error occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }

            }
        });

    }

    public String getBatch(String email){
        return email.substring(0,7);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mDbListener!=null){

            mReference.removeEventListener(mDbListener);
        }
        if(mDbListener1!=null){

            requestDatabase.removeEventListener(mDbListener1);
        }
        if(mDbListener2!=null){

            frienDataBase.removeEventListener(mDbListener2);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null)
        {
            mRef.child("online").setValue(1);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        user_id=intent.getStringExtra("user_id");
    }
}
