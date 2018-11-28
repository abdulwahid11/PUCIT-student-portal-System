package com.example.abdul.pucitstudentportalsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DTO.User;
import de.hdodenhof.circleimageview.CircleImageView;
import Adapters.MessageListAdapter;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private android.support.v7.widget.Toolbar mToolBar;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private String userName;
    private TextView name,lastSeen;
    private CircleImageView pic;
    private ValueEventListener mDbListener;

    private ValueEventListener mDbListener1;
    private DatabaseReference reference;
    private ImageButton sendBtn;
    private EditText editTextmessage;
    private RecyclerView mMessgaesList;
    private SwipeRefreshLayout mSwipeLayout;
    private final List<Messages> messageList=new ArrayList<>();
    private LinearLayoutManager mLinearlayout;
    private MessageListAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD=10;
    private int mCurrentPage=1;
    private LinearLayout messageLayout,disableLayout;
    String batch;
    String currentUserId;



    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";
    private String userImage;
    private SharedPreferences prefs;
    private static  String PREF_NAME="notifications";

    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        mToolBar=(android.support.v7.widget.Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolBar);
        ActionBar actionBar=getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        sendBtn=(ImageButton)findViewById(R.id.chat_send_btn);
        editTextmessage=(EditText)findViewById(R.id.chat_message_view);
        mAdapter=new MessageListAdapter(this,messageList);
        mMessgaesList=(RecyclerView)findViewById(R.id.messagesList);
        mSwipeLayout=(SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);


        mLinearlayout= new LinearLayoutManager(this);
        mMessgaesList.setHasFixedSize(true);
        mMessgaesList.setLayoutManager(mLinearlayout);
        mMessgaesList.setAdapter(mAdapter);
        messageLayout=(LinearLayout)findViewById(R.id.linearLayout);
        disableLayout=(LinearLayout)findViewById(R.id.disableLayout);
        messageLayout.setVisibility(View.INVISIBLE);
        messageLayout.setEnabled(false);
        disableLayout.setVisibility(View.INVISIBLE);
        disableLayout.setEnabled(false);




        mChatUser=getIntent().getStringExtra("user_id");

       // Toast.makeText(this, mChatUser, Toast.LENGTH_LONG).show();

      //  mChatUser=getIntent().getStringExtra("user_id");
        userName=getIntent().getStringExtra("user_name");

        //Toast.makeText(this, userName, Toast.LENGTH_LONG).show();

        //getSupportActionBar().setTitle(userName);
        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);
        name=(TextView) action_bar_view.findViewById(R.id.name);

        lastSeen=(TextView)action_bar_view.findViewById(R.id.last_seen);
        pic=(CircleImageView)action_bar_view.findViewById(R.id.profilePic);
        pic.setVisibility(View.INVISIBLE);
        name.setText(userName);
        currentUserId=mAuth.getCurrentUser().getUid();
        batch=getBatch(mAuth.getCurrentUser().getEmail());
        reference=FirebaseDatabase.getInstance().getReference();
        loadMessages();


        myRef=FirebaseDatabase.getInstance().getReference().child(batch).child("users");
        reference.child(batch).child("Friends").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    disableLayout.setEnabled(true);
                    disableLayout.setVisibility(View.VISIBLE);
                }
                else{
                    messageLayout.setEnabled(true);
                    messageLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       mDbListener1= myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child(mChatUser).child("online").getValue().toString();
                final String img=dataSnapshot.child(mChatUser).child("thumbPath").getValue().toString();
                user=dataSnapshot.child(currentUserId).getValue(User.class);

                userImage=img;
                if(online.equals("1")){
                    lastSeen.setText("Online");
                }else{
                    GetTimeAgo getTime=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String lastSeenTime=getTime.getTimeAgo(lastTime,ChatActivity.this);
                    if(lastSeenTime==null){
                        lastSeenTime="just now";
                    }

                    lastSeen.setText(lastSeenTime);
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {



                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();

            }
        });
    }



    private void loadMoreMessages() {
        DatabaseReference messageRef = reference.child(batch).child("messages").child(currentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messageList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }
                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mSwipeLayout.setRefreshing(false);

                mLinearlayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = reference.child(batch).child("messages").child(currentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                messageList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessgaesList.scrollToPosition(messageList.size() - 1);
                mSwipeLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void sendMessage() {
        final String message=editTextmessage.getText().toString();
        editTextmessage.setText("");
        if(!TextUtils.isEmpty(message)){
            final String lastMessage=message;
            if(lastMessage.length()>15){
                lastMessage.substring(0,14);

            }
            reference.child(batch).child("Chat").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(mChatUser)){
                        Map chatAddMap=new HashMap();
                        chatAddMap.put("seen",false);
                        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                        chatAddMap.put("name",userName);
                        chatAddMap.put("imageUrl",userImage);
                        chatAddMap.put("lastMessage",lastMessage);

                        Map chatAddMap1=new HashMap();
                        chatAddMap1.put("seen",false);
                        chatAddMap1.put("timestamp", ServerValue.TIMESTAMP);
                        chatAddMap1.put("name",user.getUserName());
                        chatAddMap1.put("imageUrl",user.getThumbPath());
                        chatAddMap1.put("lastMessage",lastMessage);


                        Map chatUserMap= new HashMap();
                        chatUserMap.put(batch+"/Chat/"+currentUserId+"/"+mChatUser,chatAddMap);
                        chatUserMap.put(batch+"/Chat/"+mChatUser+"/"+currentUserId,chatAddMap1);

                        reference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    Log.d("TAG", "onComplete: "+databaseError.getMessage());
                                }

                            }
                        });


                    }
                    DatabaseReference newRef=reference.child(batch).child("Chat").child(mChatUser);
                    newRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.hasChild(currentUserId)){
                                Map chatAddMap=new HashMap();
                                chatAddMap.put("seen",false);
                                chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                                chatAddMap.put("name",userName);
                                chatAddMap.put("imageUrl",userImage);
                                chatAddMap.put("lastMessage",lastMessage);

                                Map chatAddMap1=new HashMap();
                                chatAddMap1.put("seen",false);
                                chatAddMap1.put("timestamp", ServerValue.TIMESTAMP);
                                chatAddMap1.put("name",user.getUserName());
                                chatAddMap1.put("imageUrl",user.getThumbPath());
                                chatAddMap1.put("lastMessage",lastMessage);


                                Map chatUserMap= new HashMap();
                                chatUserMap.put(batch+"/Chat/"+currentUserId+"/"+mChatUser,chatAddMap);
                                chatUserMap.put(batch+"/Chat/"+mChatUser+"/"+currentUserId,chatAddMap1);

                                reference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError!=null){
                                            Log.d("TAG", "onComplete: "+databaseError.getMessage());
                                        }

                                    }
                                });

                            }
                            else {
                                Map chatUserMap= new HashMap();
                                chatUserMap.put(batch+"/Chat/"+currentUserId+"/"+mChatUser+"/lastMessage",lastMessage);
                                chatUserMap.put(batch+"/Chat/"+mChatUser+"/"+currentUserId+"/lastMessage",lastMessage);

                                reference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if(databaseError!=null){
                                            Log.d("TAG", "onComplete: "+databaseError.getMessage());
                                        }

                                    }
                                });


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });




                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
         //   reference.removeEventListener(mDbListener);


            editTextmessage.setText("");
            String crrentUserRef=batch+"/messages/"+currentUserId+"/"+mChatUser;
            String chatUserRef=batch+"/messages/"+mChatUser+"/"+currentUserId;
            DatabaseReference pushRef=reference.child(batch).child("messages").child(currentUserId).child(mChatUser).push();

            String pushId=pushRef.getKey();


            Map messageMap=new HashMap();

            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);
            messageMap.put("otherUserImage",user.getThumbPath());
            messageMap.put("otherUserName",user.getUserName());


            Map messageMap1=new HashMap();

            messageMap1.put("message",message);
            messageMap1.put("seen",false);
            messageMap1.put("type","text");
            messageMap1.put("time",ServerValue.TIMESTAMP);
            messageMap1.put("from",currentUserId);
            messageMap1.put("otherUserImage",userImage);
            messageMap1.put("otherUserName",userName);




            Map messageUserMap= new HashMap();
            messageUserMap.put(crrentUserRef+"/"+pushId,messageMap1);
            messageUserMap.put(chatUserRef+"/"+pushId,messageMap);
            reference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError!=null){
                        Log.d("TAG", "onComplete: "+databaseError.getMessage());
                    }


                    Map notificationData=new HashMap<>();
                    notificationData.put(batch+"/RequestNotifications/"+mChatUser+"/"+currentUserId+"/timestamp",ServerValue.TIMESTAMP);

                    notificationData.put(batch+"/RequestNotifications/"+mChatUser+"/"+currentUserId+"/type","message");


                    reference.updateChildren(notificationData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("TAG", "onComplete: error in notification adding");
                            }
                        }
                    });


                }
            });

        }
    }

    public String getBatch(String email){
        return email.substring(0,7);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mChatUser=intent.getStringExtra("user_id");
        userName=intent.getStringExtra("user_name");
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null)
        {
            reference.child(batch).child("users").child(currentUserId).child("online").setValue(1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbListener!=null)
        {

            reference.removeEventListener(mDbListener);
        }
        if(mDbListener1!=null){

            myRef.removeEventListener(mDbListener1);
        }
    }
}
