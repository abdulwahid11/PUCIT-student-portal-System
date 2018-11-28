package com.example.abdul.pucitstudentportalsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapters.PostAdapter;
import DTO.Post;
import DTO.User;
import Utils.RecyclerViewEmptySupport;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PostAdapter.OnItemClickListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage mStorage;
    private DatabaseReference myRef;
    private ArrayList<User> allUser=new ArrayList<User>();
    private User currentLoggedInUser;
    private ImageView profieImage;
    private TextView name,place;
    private RecyclerViewEmptySupport recyclerView;
    private PostAdapter mAdapter;
    private List<Post> postList;
    private ValueEventListener mDbListener;
    private NavigationView navigationView;
    private SharedPreferences prefs;
    DatabaseReference mRef;
    private static  final  String MY_PREF="prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();

        mRef=FirebaseDatabase.getInstance().getReference().child(getBatch(mAuth.getCurrentUser().getEmail())).child("users").child(mAuth.getCurrentUser().getUid());
        database=FirebaseDatabase.getInstance();
        mStorage=FirebaseStorage.getInstance();
        myRef=database.getReference();
        myRef.keepSynced(true);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        recyclerView=(RecyclerViewEmptySupport) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(findViewById(R.id.list_empty));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList=new ArrayList<>();
        prefs=getSharedPreferences(MY_PREF,MODE_PRIVATE);
        String isCr=prefs.getString("CR","null");
        if(isCr.equals("true"))
        {
            mAdapter=new PostAdapter(Home.this,postList,true);
        }
        else{

            mAdapter=new PostAdapter(Home.this,postList,false);
            hide();

        }
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(Home.this);



        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View view=navigationView.getHeaderView(0);


        profieImage=(ImageView)view.findViewById(R.id.myprofileImageView);
        profieImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager=getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.myFrameLayout,new ProfileFragment()).addToBackStack(null).commit();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        name=(TextView)view.findViewById(R.id.textViewName);
        place=(TextView)view.findViewById(R.id.textViewPlace);
        navigationView.setNavigationItemSelectedListener(this);


        final FirebaseUser currentUser = mAuth.getCurrentUser();

        mDbListener=myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String batch=getBatch(mAuth.getCurrentUser().getEmail());
                place.setText(getRollNo(mAuth.getCurrentUser().getEmail()).toUpperCase());

                DataSnapshot snap= dataSnapshot.child(batch).child("users").child(mAuth.getCurrentUser().getUid());
                Iterable<DataSnapshot> itr=snap.getChildren();


                User user=snap.getValue(User.class);
                currentLoggedInUser=user;
                if(!user.getThumbPath().equals(""))
                {
                    Picasso.get().load(currentLoggedInUser.getThumbPath().toString()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).fit().centerCrop().into(profieImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get().load(currentLoggedInUser.getThumbPath().toString()).placeholder(R.drawable.profile).fit().centerCrop().into(profieImage);
                        }
                    });
                }
                name.setText(currentLoggedInUser.getUserName().toString());

                postList.clear();
                DataSnapshot postDataSnap=dataSnapshot.child(batch).child("posts");
                Iterable<DataSnapshot> itr1=postDataSnap.getChildren();
                for(DataSnapshot data:itr1){
                    Post mPost=data.getValue(Post.class);
                    mPost.setPostId(data.getKey());
                    postList.add(mPost);

                }

                Collections.reverse(postList);
                mAdapter.notifyDataSetChanged();



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Home.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null)
        {
            mRef.child("online").setValue(1);
        }

    }

    public void hide(){
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_post).setVisible(false);
        nav_Menu.findItem(R.id.addNotification).setVisible(false);
    }




    public String getBatch(String email){

        return email.substring(0,7);
    }
    public String getRollNo(String email)
    {
        return email.substring(0,10);

    }


    private void  getAllUsers(){


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                DataSnapshot snap= dataSnapshot.child("users");
                Iterable<DataSnapshot> itr=snap.getChildren();
                User curr=new User();

                for(DataSnapshot data:itr){
                    User user=data.getValue(User.class);
                    allUser.add(user);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onBackPressed() {


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Really Exit?")
                        .setMessage("Are you sure you want to exit?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {


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

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment=null;
        int id = item.getItemId();
        if(id==R.id.nav_home){
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if (id == R.id.nav_post) {
          //  Intent intent = new Intent(Home.this,NewPost.class);
           // startActivity(intent);
            fragment=new PostFragment();
            // Handle the camera action
        } else if (id == R.id.nav_edit_profile) {
           // startActivity(new Intent(Home.this,EditProfile.class));
            fragment=new ProfileFragment();

        } else if (id == R.id.notification) {
           // Intent intent = new Intent(Home.this,Notifications.class);
            //startActivity(intent);
            fragment=new NotificationsFrament();


        }
        else if (id==R.id.addNotification)
        {
            //Intent intent = new Intent(Home.this,NewNotification.class);
            //startActivity(intent);
            fragment=new NewNotificationFragment();
        }
        else if (id == R.id.nav_logOut) {
            mRef.child("deviceToken").setValue("");
            myRef.removeEventListener(mDbListener);
            mRef.child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            Intent intent=new Intent(Home.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }
        else if(id==R.id.nav_chat)
        {

            myRef.removeEventListener(mDbListener);
            startActivity(new Intent(Home.this,ConversationsActivity.class));
            //finish();
        }
        else if(id==R.id.nav_allusers)
        {
            myRef.removeEventListener(mDbListener);
            startActivity(new Intent(Home.this,AllUsers.class));
        }
        if(fragment!=null){
            FragmentManager fragmentManager=getSupportFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.myFrameLayout,fragment);
            fragmentTransaction.addToBackStack(null).commit();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onProfileImageClick(View v, int position) {
      //  Toast.makeText(this, "profile "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttachmentClick(View v, int position) {

        //Toast.makeText(this, "attachment "+position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWhatEverClick(int position) {
        //Toast.makeText(this, "WhatEver click "+position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeleteClick(final int position) {


        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Post post =postList.get(position);
                        final String selectedKey=post.getPostId();
                        StorageReference imageRef=mStorage.getReferenceFromUrl(post.getFileUrl());

                        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                myRef.child(getBatch(mAuth.getCurrentUser().getEmail())).child("posts").child(selectedKey).removeValue();
                                Toast.makeText(Home.this, "item Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this, "failed", Toast.LENGTH_SHORT).show();

                            }
                        });




                    }
                }).create().show();




    }

    @Override
    public void onDownlaodClick(int position) {
        Toast.makeText(this, "im clicked", Toast.LENGTH_SHORT).show();
        Intent intent =new Intent();
        intent.setType(Intent.ACTION_VIEW);
        Post post =postList.get(position);
        intent.setData(Uri.parse(post.getFileUrl()));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDbListener!=null){
            myRef.removeEventListener(mDbListener);
        }
    }
    /*
    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null){
            mRef.child("online").setValue("false");
        }
    }
    */
}
