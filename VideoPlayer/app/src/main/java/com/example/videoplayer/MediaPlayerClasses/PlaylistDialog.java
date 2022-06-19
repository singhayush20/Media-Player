package com.example.videoplayer.MediaPlayerClasses;

import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.FOLDER_NAME_KEY;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MEDIA_TYPE_AUDIO;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MEDIA_TYPE_KEY;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MEDIA_TYPE_VIDEO;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MY_PREF;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.R;
import com.example.videoplayer.RecyclerViewClasses.MediaFilesAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlaylistDialog extends BottomSheetDialogFragment {
    private static final String TAG = PlaylistDialog.class.getSimpleName();
    ArrayList<MediaFiles> arrayList=new ArrayList<>();
    MediaFilesAdapter mediaFilesAdapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    TextView folder;

    public PlaylistDialog(ArrayList<MediaFiles> arrayList, MediaFilesAdapter mediaFilesAdapter) {
        this.arrayList = arrayList;
        this.mediaFilesAdapter = mediaFilesAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog=(BottomSheetDialog) super.onCreateDialog(savedInstanceState);
View view= LayoutInflater.from(getContext()).inflate(R.layout.playlist_bottomsheet_layout,null);
    bottomSheetDialog.setContentView(view);
    recyclerView=view.findViewById(R.id.playlist_rv);
    folder =view.findViewById(R.id.playlistName);
    //Use fetch media method of media files activity to fetch the videos
        SharedPreferences preferences=this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String mediaType=preferences.getString(MEDIA_TYPE_KEY,"DEFAULT_MEDIA_TYPE");
        String folderName=preferences.getString(FOLDER_NAME_KEY,"DEFAULT_FOLDER_NAME");
        Log.i(TAG+" ###","folderName: "+folderName+" mediaType: "+mediaType);
        folder.setText(folderName);
        if(mediaType.equals(MEDIA_TYPE_AUDIO))
            arrayList=fetchAudio(folderName);
        else if(mediaType.equals(MEDIA_TYPE_VIDEO))
           arrayList= fetchVideo(folderName);
        else {
            Log.e(TAG + "###", "error fetching mediaType!");
        }
            mediaFilesAdapter=new MediaFilesAdapter(arrayList,getContext(),mediaType,1);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mediaFilesAdapter);
        mediaFilesAdapter.notifyDataSetChanged();
        return bottomSheetDialog;

    }
    private ArrayList<MediaFiles> fetchVideo(String folderName) {


        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;




        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, selectionArg, null);
        if (cursor != null && cursor.moveToNext()) {
            do {

                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                //The path contains the complete path of video file with extension
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                String dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mMediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                mediaFiles.add(mMediaFiles);
            } while (cursor.moveToNext());
        }
        return mediaFiles;
    }
    private ArrayList<MediaFiles> fetchAudio(String folderName) {

        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;



        String selection = MediaStore.Audio.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, selectionArg, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //The path contains the complete path of Audio file with extension
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));
                MediaFiles mMediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                mediaFiles.add(mMediaFiles);
            } while (cursor.moveToNext());
        }
        return mediaFiles;
    }
}
