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
import android.widget.TextView;

import com.example.abdul.pucitstudentportalsystem.R;

import java.util.List;

import DTO.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context mContext;
    private List<Notification> notifications;
    private boolean isCR;

    private OnItemClickListener mListener;

    public NotificationAdapter(Context mContext, List<Notification> notifications,boolean cr) {
        this.mContext = mContext;
        this.notifications = notifications;
        isCR=cr;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false);
        return new NotificationViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification=notifications.get(position);
        holder.title.setText(notification.getTitle().toUpperCase());
        holder.description.setText(notification.getDescription());
        holder.time.setText(notification.getDate());

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener{
        private TextView title,description,time;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            title=(TextView)itemView.findViewById(R.id.notification_title_tv);
            description=(TextView)itemView.findViewById(R.id.notification_description_tv);
            time=(TextView)itemView.findViewById(R.id.notification_time_tv);

            itemView.setOnCreateContextMenuListener(this);

        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener!=null){
                int position =getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.onDeleteClick(position);
                            return true;


                    }
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("select action");
            MenuItem delete=menu.add(Menu.NONE,1,1,"Delete");
            if(isCR==false)
            {
                MenuItem item=menu.getItem(0);
                item.setVisible(false);
            }
            delete.setOnMenuItemClickListener(this);
        }
    }
    public interface OnItemClickListener{
        void onWhatEverClick(int position);
        void onDeleteClick(final int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener=listener;

    }
}
