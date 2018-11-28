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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abdul.pucitstudentportalsystem.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import DTO.Friend;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>{
    private Context mContext;
    private List<Friend> friends;

    private FriendsAdapter.OnItemClickListener mListener;

    public FriendsAdapter(Context mContext, List<Friend> allUsers) {
        this.mContext = mContext;
        this.friends =allUsers ;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.single_user_layout,parent,false);
        return new FriendsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        Friend user=friends.get(position);
        holder.name.setText(user.getName());
        if(user.getIsOnline().equals("1")){
            holder.onlineImg.setVisibility(View.VISIBLE);
        }else{
            holder.onlineImg.setVisibility(View.GONE);
        }
        if(!user.getImageUrl().equals(""))
        {
            Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.profileImage);
        }
        holder.rollNo.setText(user.getDate());

    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener{

        TextView name,rollNo;
        ImageView profileImage,onlineImg;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            name=(TextView)itemView.findViewById(R.id.u_name);
            profileImage=(ImageView)itemView.findViewById(R.id.profile_image);
            rollNo=(TextView)itemView.findViewById(R.id.roll);
            onlineImg=(ImageView)itemView.findViewById(R.id.online);

            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener!=null){
                int position =getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.onProfileClick(position);
                            return true;


                    }
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            mListener.onProfileClick(getAdapterPosition());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("select action");
            MenuItem myMenu=menu.add(Menu.NONE,1,1,"Go to profile");
            // MenuItem item=menu.getItem(0);
            myMenu.setOnMenuItemClickListener(this);
        }
    }
    public interface OnItemClickListener{
        void onProfileContextMenu(int position);
        void onDeleteClick(final int position);
        void onProfileClick(final int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener=listener;

    }
}
