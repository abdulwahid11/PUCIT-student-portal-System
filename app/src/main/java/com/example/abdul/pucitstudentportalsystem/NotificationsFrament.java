package com.example.abdul.pucitstudentportalsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapters.NotificationAdapter;
import DTO.Notification;
import DTO.Post;
import Utils.RecyclerViewEmptySupport;

import static android.content.Context.MODE_PRIVATE;

public class NotificationsFrament extends Fragment implements NotificationAdapter.OnItemClickListener{
    private RecyclerViewEmptySupport recyclerView;
    private NotificationAdapter mAdapter;
    private List<Notification> notificationList;
    private ValueEventListener mDbListener;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    private SharedPreferences prefs;
    private static  final  String MY_PREF="prefs";
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications,null);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
   //     Toast.makeText(getActivity(), "NotificationFragment", Toast.LENGTH_SHORT).show();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference();
        mAuth=FirebaseAuth.getInstance();
        recyclerView=(RecyclerViewEmptySupport) view.findViewById(R.id.notification_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(view.findViewById(R.id.list_empty));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationList=new ArrayList<>();
        prefs=getActivity().getSharedPreferences(MY_PREF,MODE_PRIVATE);
        String isCr=prefs.getString("CR","null");
        if(isCr.equals("true"))
        {
            mAdapter=new NotificationAdapter(getActivity(),notificationList,true);
        }
        else if(isCr.equals("false")){

            mAdapter=new NotificationAdapter(getActivity(),notificationList,false);

        }
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);




        final String batch=getBatch(mAuth.getCurrentUser().getEmail());

        mDbListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                notificationList.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("notifications");
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    Notification mNotification=data.getValue(Notification.class);
                    mNotification.setId(data.getKey());
                    notificationList.add(mNotification);

                }

                Collections.reverse(notificationList);

                mAdapter.notifyDataSetChanged();

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
    public void onDestroy() {
        super.onDestroy();

        reference.removeEventListener(mDbListener);
    }


    @Override
    public void onDeleteClick(final int position) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this notification?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Notification notification=notificationList.get(position);
                        final String selectedKey=notification.getId();
                        reference.child(getBatch(mAuth.getCurrentUser().getEmail())).child("notifications").child(selectedKey).removeValue();
                        Toast.makeText(getActivity(), "item Deleted", Toast.LENGTH_SHORT).show();

                    }
                }).create().show();
    }

    @Override
    public void onWhatEverClick(int positon) {
     //   Toast.makeText(getActivity(), "whatever", Toast.LENGTH_SHORT).show();

    }

}
