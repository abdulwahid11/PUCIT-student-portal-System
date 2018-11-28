package com.example.abdul.pucitstudentportalsystem;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DTO.Notification;
import DTO.User;
import Utils.RecyclerViewEmptySupport;
import Adapters.AllUsersAdapter;

public class AllUsers extends AppCompatActivity implements AllUsersAdapter.OnItemClickListener {

    private Toolbar mToolbar;
    private RecyclerViewEmptySupport recyclerView;
    private AllUsersAdapter mAdapter;
    private List<User> usersList;
    private ValueEventListener mDbListener;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String isOnline;
    private String currentUserId;
    private String batch;

    DatabaseReference mRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);




        mToolbar=(Toolbar)findViewById(R.id.myToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database=FirebaseDatabase.getInstance();
        reference=database.getReference();
        mAuth=FirebaseAuth.getInstance();

        currentUserId=mAuth.getCurrentUser().getUid();
        batch=getBatch(mAuth.getCurrentUser().getEmail());

        mRef=FirebaseDatabase.getInstance().getReference().child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid());
        recyclerView=(RecyclerViewEmptySupport) findViewById(R.id.recycler_view);
        recyclerView.setEmptyView(findViewById(R.id.list_empty));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersList=new ArrayList<>();
        mAdapter= new AllUsersAdapter(this,usersList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);





        mDbListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                usersList.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("users");
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    User user=data.getValue(User.class);
                    user.setId(data.getKey());
                    if(!currentUserId.equals(user.getId())){

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
    public void onProfileClick(int position) {
        reference.removeEventListener(mDbListener);
        Intent intent= new Intent(AllUsers.this,ProfileActivity.class);
        intent.putExtra("user_id",usersList.get(position).getId());
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        reference.removeEventListener(mDbListener);

    }
}
