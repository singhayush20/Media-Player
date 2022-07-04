package com.example.videoplayer.RecyclerViewClasses;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity;
import com.example.videoplayer.R;

import java.util.ArrayList;

public class MediaFoldersAdapter extends RecyclerView.Adapter<MediaFoldersAdapter.ViewHolder> {
    private static final String TAG = MediaFoldersAdapter.class.getSimpleName();
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;
    private Context mContext;
    private String mediaType;

    public MediaFoldersAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context mContext,String mediaType) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.mContext = mContext;
        this.mediaType=mediaType;
    }

    /**
     * Creates a new ViewHolder whenever the RecyclerView needs a new one
     * Whenever we download a video from a source, a new ViewHolder will be created
     * @param parent:
     * @param viewType:
     * @return a ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(mContext).inflate(R.layout.folder_item,parent,false);
            return new ViewHolder(view);
    }

    /**
     * Update the recycler view and show the actual data
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //lastIndexOf returns the position of the last / from
        //a path like /storage/Media/Videos
        int indexPath=folderPath.get(position).lastIndexOf("/");
        String nameOfFolder= folderPath.get(position).substring(indexPath+1);
        holder.folderName.setText(nameOfFolder);
        holder.folderPath.setText(folderPath.get(position));
        int number_of_Files=noOfFiles(folderPath.get(position));
        holder.noOfFiles.setText(number_of_Files+" Files");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(AudioPlayerActivity.playerNotificationManager!=null)
//                    AudioPlayerActivity.playerNotificationManager.setPlayer(null);
                //Navigate to the next activity when clicked on the folder item
                Intent intent=new Intent(mContext, MediaFilesActivity.class);
                intent.putExtra("folderName",nameOfFolder);
                intent.putExtra("mediaType",mediaType);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * Return the total number of items in the adapter
     * @return
     */
    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public  static class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName, folderPath, noOfFiles;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName=itemView.findViewById(R.id.folderName);
            folderPath=itemView.findViewById(R.id.folderPath);
            noOfFiles=itemView.findViewById(R.id.noOfFiles);


        }
    }
    int noOfFiles(String folder_name)
    {
        //Log.i(TAG+"###","Folder name in noOfFiles: "+folder_name);
        int files_number=0;
        for(MediaFiles mediaFiles:mediaFiles)
        {

            //Log.i(TAG+" ###","mediaFiles.getPath(): "+mediaFiles.getPath());
            if(mediaFiles.getPath().substring(0,mediaFiles.getPath().lastIndexOf("/")).trim()
                    .equals(folder_name))
            {
                //Log.i(TAG+" ###","mediaFiles.getPath(): "+mediaFiles.getPath());
                files_number++;
            }
        }
        return files_number;
    }
}
