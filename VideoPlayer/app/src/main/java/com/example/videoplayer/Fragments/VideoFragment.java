package com.example.videoplayer.Fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.R;
import com.example.videoplayer.RecyclerViewClasses.MediaFoldersAdapter;

import java.util.ArrayList;

public class VideoFragment extends Fragment {
    private static final String TAG = VideoFragment.class.getSimpleName();
    private ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
    private ArrayList<String> allFolderList = new ArrayList<>();
    RecyclerView mRecyclerView;
    MediaFoldersAdapter mediaFoldersAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between
     * {@link #onCreate(Bundle)} and {@link #onViewCreated(View, Bundle)}.
     * <p>A default View can be returned by calling {@link #//Fragment(int)} in your
     * constructor. Otherwise, this method returns null.
     *
     * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
     * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videos, container, false);

    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.folders_recycler_view_Video);
        swipeRefreshLayout=view.findViewById(R.id.swipe_refresh_folder_Video);
        showFolders();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showFolders() {
        mediaFiles = fetchMedia();
        //Initialize the adapter
        mediaFoldersAdapter = new MediaFoldersAdapter(mediaFiles, allFolderList, getContext(),"video");
        mRecyclerView.setAdapter(mediaFoldersAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),DividerItemDecoration.VERTICAL));
        mediaFoldersAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        //Make the cursor
        try {


            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                do {
                    /*
                    Why getColumnIndex() gives error
                    https://stackoverflow.com/questions/69053061/android-studio-value-must-be-%E2%89%A5-0
                     */
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    //The path contains the complete path of video file with extension
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                    int index = path.lastIndexOf("/");
                    //This substring will be the path of folder without file name
                    String subString = path.substring(0, index);
                    if (!allFolderList.contains(subString)) {
                        allFolderList.add(subString);
                    }
                    mediaFilesArrayList.add(mediaFiles);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG + " ###", "Error retrieving the video files " + e);
        }
        return mediaFilesArrayList;
    }
}
