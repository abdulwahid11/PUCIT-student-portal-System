package com.example.abdul.pucitstudentportalsystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import Adapters.RequestAdapter;
import DTO.Conversation;
import DTO.Request;
import Utils.RecyclerViewEmptySupport;

public class RequestFragment extends Fragment implements RequestAdapter.OnItemClickListener {

    private Toolbar mToolbar;
    private RecyclerViewEmptySupport recyclerView;
    private RequestAdapter mAdapter;
    private List<Request> usersList;
    private ValueEventListener mDbListener;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String batch;
    private String currentUserId;
    private FirebaseAuth mAuth;

    DatabaseReference mRef;


    public RequestFragment() {
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
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = (RecyclerViewEmptySupport) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(view.findViewById(R.id.list_empty));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        usersList = new ArrayList<>();
        mAdapter = new RequestAdapter(getActivity(), usersList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        currentUserId=mAuth.getCurrentUser().getUid();
        batch=getBatch(mAuth.getCurrentUser().getEmail());



        mDbListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                usersList.clear();
                DataSnapshot postDataSnap = dataSnapshot.child(batch).child("Friend_request").child(currentUserId);
                Iterable<DataSnapshot> itr1 = postDataSnap.getChildren();
                for (DataSnapshot data : itr1) {
                    Request user = data.getValue(Request.class);
                    user.setfrom(data.getKey());
                    if(user.getRequest_type().equals("recieved")){
                        usersList.add(user);
                    }
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
    public void onWhatEverClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {

    }

    @Override
    public void onProfileClick(final int position) {

       // reference.removeEventListener(mDbListener);
        startActivity(new Intent(getActivity(), ProfileActivity.class).putExtra("user_id", usersList.get(position).getfrom()));
    }
}