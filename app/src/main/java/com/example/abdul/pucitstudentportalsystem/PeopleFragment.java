package com.example.abdul.pucitstudentportalsystem;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapters.FriendsAdapter;
import DTO.Friend;
import Utils.RecyclerViewEmptySupport;


/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleFragment extends Fragment implements FriendsAdapter.OnItemClickListener {

    private Toolbar mToolbar;
    private RecyclerViewEmptySupport recyclerView;
    private FriendsAdapter mAdapter;
    private List<Friend> usersList;
    private ValueEventListener mDbListener;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private String batch;
    private String currentUserId;

    DatabaseReference mRef;


    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database=FirebaseDatabase.getInstance();
        reference=database.getReference();
        mAuth=FirebaseAuth.getInstance();
        recyclerView=(RecyclerViewEmptySupport) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(view.findViewById(R.id.list_empty));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        usersList=new ArrayList<>();
        mAdapter=new FriendsAdapter(getActivity(),usersList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        currentUserId=mAuth.getCurrentUser().getUid();
        batch=getBatch(mAuth.getCurrentUser().getEmail());




        mDbListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                usersList.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("Friends").child(currentUserId);
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    Friend user=data.getValue(Friend.class);
                    user.setId(data.getKey());
                    String isOnline=dataSnapshot.child(batch).child("users").child(data.getKey()).child("online").getValue().toString();
                    user.setIsOnline(isOnline);
                    usersList.add(user);
                }

                Collections.reverse(usersList);

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
    public void onProfileContextMenu(int position) {
        Intent intent= new Intent(getActivity(),ProfileActivity.class);
        intent.putExtra("user_id",usersList.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {

    }

    @Override
    public void onProfileClick(final int position) {
        CharSequence options[]=new CharSequence[]{"Open Profile","Send Mesaage"};
        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //reference.removeEventListener(mDbListener);
                    Intent intent= new Intent(getActivity(),ProfileActivity.class);
                    intent.putExtra("user_id",usersList.get(position).getId());
                    startActivity(intent);
                }
                else if (which==1){
                    //reference.removeEventListener(mDbListener);
                    startActivity(new Intent(getActivity(),ChatActivity.class).putExtra("user_id",usersList.get(position).getId()).putExtra("user_name",usersList.get(position).getName()));

                }
            }
        });
        builder.show();

    }
}
