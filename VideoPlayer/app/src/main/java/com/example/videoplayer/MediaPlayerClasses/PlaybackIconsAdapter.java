package com.example.videoplayer.MediaPlayerClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplayer.R;

import java.util.ArrayList;

public class PlaybackIconsAdapter extends RecyclerView.Adapter<PlaybackIconsAdapter.ViewHolder> {
    private ArrayList<IconModel> iconModelArrayList;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener
    {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mOnItemClickListener=listener;
    }
    public PlaybackIconsAdapter(ArrayList<IconModel> iconModelArrayList, Context context) {
        this.iconModelArrayList = iconModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.icons_layout,parent,false);

        return new ViewHolder(view,mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.iconImage.setImageResource(iconModelArrayList.get(position).getImageView());
        holder.iconName.setText(iconModelArrayList.get(position).getIconTitle());
    }

    @Override
    public int getItemCount() {
        return iconModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconName;
        ImageView iconImage;
        public ViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View view) {
                    if(listener!=null)
                    {
                        int position=getAbsoluteAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION)
                            listener.onItemClick(position);
                    }
                }
            });
            iconName=itemView.findViewById(R.id.icon_Title);
            iconImage=itemView.findViewById(R.id.playback_icon);


        }
    }
}
