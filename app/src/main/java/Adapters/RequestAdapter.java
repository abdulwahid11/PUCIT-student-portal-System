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

import DTO.Request;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{
    private Context mContext;
    private List<Request> requests;

    private RequestAdapter.OnItemClickListener mListener;

    public RequestAdapter(Context mContext, List<Request> allUsers) {
        this.mContext = mContext;
        this.requests =allUsers ;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.single_user_layout,parent,false);
        return new RequestViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request=requests.get(position);
        holder.name.setText(request.getName());
        if(!request.getImageUrl().equals(""))
        {
            Picasso.get().load(request.getImageUrl()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.profileImage);
        }

    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener{

        TextView name,rollNo;
        ImageView profileImage,onlineImg;
        public RequestViewHolder(View itemView) {
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
        void onWhatEverClick(int position);
        void onDeleteClick(final int position);
        void onProfileClick(final int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener=listener;

    }
}
