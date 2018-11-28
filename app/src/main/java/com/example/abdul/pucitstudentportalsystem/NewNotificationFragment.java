package com.example.abdul.pucitstudentportalsystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DTO.Notification;
import DTO.User;

public class NewNotificationFragment extends Fragment {
    private EditText title,desc;
    private Button notify;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private ProgressBar progressBar;
    private List deviceTokens=new ArrayList();
    private DatabaseReference reference;
    private ValueEventListener mDbListener;
    private String batch;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_notification_fragment,null);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getActivity(), "new Notification fragment", Toast.LENGTH_SHORT).show();
        title=(EditText) view.findViewById(R.id.notification_title);
        desc=(EditText)view.findViewById(R.id.notification_description);
        notify=(Button)view.findViewById(R.id.add_notification);
        progressBar=(ProgressBar)view.findViewById(R.id.notification_progress);
        reference=FirebaseDatabase.getInstance().getReference();


        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        ref=database.getReference();
        batch=getBatch(mAuth.getCurrentUser().getEmail());
        currentUserId=mAuth.getCurrentUser().getUid();

        mDbListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                deviceTokens.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("users");
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    User user1=data.getValue(User.class);
                    user1.setId(data.getKey());
                    if(!currentUserId.equals(user1.getId())&&!user1.getDeviceToken().equals("")){

                        deviceTokens.add(user1.getDeviceToken());
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                progressBar.setVisibility(View.VISIBLE);
                publishNotification();


            }
        });
    }

    private void publishNotification() {
        if(!validateNotification()) {
            Toast.makeText(getActivity(), "please fill out all fields", Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.GONE);

            return;
        }
        writeNotification();
    }

    private void writeNotification() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        String formattedDate = df.format(c)+" "+c.getHours()+":"+c.getMinutes()+":"+c.getSeconds();


        final String fileId=System.currentTimeMillis()+"";
        String batch=getBatch(mAuth.getCurrentUser().getEmail());
        final Notification notification=new Notification();
        notification.setTitle(title.getText().toString());
        notification.setDescription(desc.getText().toString());
        notification.setDate(formattedDate);



        Map myMap=new HashMap();
        myMap.put("deviceTokens",deviceTokens);
        myMap.put("timestamp", ServerValue.TIMESTAMP);
        myMap.put("type","notification");

        Map postMap=new HashMap();
        postMap.put(getBatch(getBatch(mAuth.getCurrentUser().getEmail()))+"/notifications/"+fileId+"/",notification);
        postMap.put(getBatch(mAuth.getCurrentUser().getEmail())+"/postNotificatoins/",myMap);

        reference.updateChildren(postMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError==null){

                    Toast.makeText(getActivity(), "uploaded successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    reference.removeEventListener(mDbListener);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.myFrameLayout,new NotificationsFrament()).addToBackStack(null).commit();

                }
            }

        });

    }

    private boolean validateNotification(){
        if(TextUtils.isEmpty(title.getText().toString()))
            return false;
        else if (TextUtils.isEmpty(desc.getText().toString()))
            return false;
        return true;
    }

    public String getBatch(String email){
        return email.substring(0,7);
    }



}
