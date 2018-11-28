package Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abdul.pucitstudentportalsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import DTO.Post;

public class PostAdapter extends RecyclerView.Adapter <PostAdapter.PostViewHolder>{

    private Context mContext;
    private List<Post> posts;
    private OnItemClickListener mListener;
    private boolean isCr;
    public PostAdapter(Context context,List<Post> mPosts,boolean Cr){
        this.mContext=context;
        this.posts=mPosts;
        this.isCr=Cr;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item,parent,false);
        return new PostViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {

        final Post post =posts.get(position);
        holder.postedBy.setText(post.getPostedByName());
        holder.desc.setText(post.getText());
        holder.timeOfPost.setText(post.getDateString());
        if(!post.getPostedByImageUrl().equals("")) {

            Picasso.get().load(post.getPostedByImageUrl().toString()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).fit().centerCrop().into(holder.profileImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {

                    Picasso.get().load(post.getPostedByImageUrl().toString()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.profileImageView);
                }
            });
        }
        switch (post.getAttachmentType())
        {
            case "pdf":
                Picasso.get().load(R.drawable.pdf).fit().centerCrop().into(holder.postImage);
                break;
            case "png":
                Picasso.get().load(post.getFileUrl().toString()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).fit().centerCrop().into(holder.postImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        Picasso.get().load(post.getFileUrl().toString()).fit().placeholder(R.drawable.file).centerCrop().into(holder.postImage);

                    }
                });
                break;
            case "jpg":
                Picasso.get().load(post.getFileUrl().toString()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).fit().centerCrop().into(holder.postImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        Picasso.get().load(post.getFileUrl().toString()).fit().placeholder(R.drawable.file).centerCrop().into(holder.postImage);
                    }
                });
                break;
            case "zip":
                Picasso.get().load(R.drawable.zip).placeholder(R.drawable.file).fit().centerCrop().into(holder.postImage);
                break;
            case "rar":
                Picasso.get().load(R.drawable.zip).fit().centerCrop().placeholder(R.drawable.file).into(holder.postImage);
                break;

            case "docx":
                Picasso.get().load(R.drawable.doc).fit().centerCrop().placeholder(R.drawable.file).into(holder.postImage);
                break;
            case "pptx":
                Picasso.get().load(R.drawable.ppt).fit().placeholder(R.drawable.file).centerCrop().into(holder.postImage);
                break;
            case "xls":
                Picasso.get().load(R.drawable.xls).fit().placeholder(R.drawable.file).centerCrop().into(holder.postImage);
                break;
            case "txt":
                Picasso.get().load(R.drawable.txt).fit().placeholder(R.drawable.file).centerCrop().into(holder.postImage);
                break;
            default:
                Picasso.get().load(R.drawable.file).fit().placeholder(R.drawable.file).centerCrop().into(holder.postImage);
                break;

        }

    }
    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener {
        public TextView postedBy,timeOfPost,desc;
        public ImageView postImage,profileImageView;
        private Button download,delete;
        FirebaseDatabase database;
        FirebaseAuth mAuth;
        DatabaseReference ref;

        public PostViewHolder(View itemView) {
            super(itemView);

            postedBy=(TextView)itemView.findViewById(R.id.postedBy);
            database=FirebaseDatabase.getInstance();
            ref=database.getReference();
            mAuth=FirebaseAuth.getInstance();
            timeOfPost=(TextView)itemView.findViewById(R.id.timestamp);
            desc=(TextView)itemView.findViewById(R.id.txtStatusMsg);
            postImage=(ImageView)itemView.findViewById(R.id.feedImage1);
            profileImageView=(ImageView)itemView.findViewById(R.id.postProfilePic);
            download=(Button)itemView.findViewById(R.id.downloadBtn);
            delete=(Button)itemView.findViewById(R.id.deleteBtn);
            profileImageView.setOnClickListener(this);
            download.setOnClickListener(this);
            delete.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            if(isCr==false)
            {
                delete.setVisibility(View.GONE);
            }

        }
        @Override
        public void onClick(View v) {

            if(mListener!=null){
                int position =getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    if(v.getId()==profileImageView.getId())
                    {
                        mListener.onProfileImageClick(v,position);
                    }
                    else if (v.getId()==postImage.getId())
                    {
                        mListener.onAttachmentClick(v,position);
                    }
                    else if (v.getId()==download.getId())
                    {
                        mListener.onDownlaodClick(position);
                    }
                    else if(v.getId()==delete.getId())
                    {
                        mListener.onDeleteClick(position);
                    }
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("select action");


            MenuItem doWhatever=menu.add(Menu.NONE,1,1,"Download attachment");
            MenuItem delete=menu.add(Menu.NONE,2,2,"Delete");
            if(isCr==false)
            {
                MenuItem item=menu.getItem(1);
                item.setVisible(false);
            }
            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }


        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener!=null){
                int position =getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                   switch (item.getItemId()){
                       case 1:

                           mListener.onDownlaodClick(position);
                           return true;
                       case 2:
                           mListener.onDeleteClick(position);
                           return true;

                   }
                }
            }
            return false;
        }
    }
    public interface OnItemClickListener{
        void onProfileImageClick(View v, int position);
        void onAttachmentClick(View v, int position);
        void onWhatEverClick(int position);
        void onDeleteClick(final int position);
        void onDownlaodClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener=listener;

    }
}