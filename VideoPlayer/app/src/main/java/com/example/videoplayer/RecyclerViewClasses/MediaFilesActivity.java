package com.example.videoplayer.RecyclerViewClasses;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.R;

import java.util.ArrayList;
import java.util.Locale;

public class MediaFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String MY_PREF = "My Pref";
    RecyclerView recyclerView;
    String folder_name;
    String mediaType;
    private ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
    static MediaFilesAdapter mediaFilesAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String sortOrder;
    public static final String FOLDER_NAME_KEY="playlistFolderName";
    public static final String MEDIA_TYPE_KEY="mediaType";
    public static final String MEDIA_TYPE_VIDEO="video";
    public static final String MEDIA_TYPE_AUDIO="audio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_files);
        folder_name = getIntent().getStringExtra("folderName");
        mediaType = getIntent().getStringExtra("mediaType");

        getSupportActionBar().setTitle(folder_name);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.media_rv);

        //Swipe refresh action
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_media);
        showMediaFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showMediaFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        SharedPreferences.Editor editor=getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.putString(FOLDER_NAME_KEY,folder_name);
        editor.putString(MEDIA_TYPE_KEY,mediaType);
        editor.apply();
    }

    private void showMediaFiles() {
        if (mediaType.equals(MEDIA_TYPE_VIDEO))
            mediaFilesArrayList = fetchVideo(folder_name);
        else if (mediaType.equals(MEDIA_TYPE_AUDIO))
            mediaFilesArrayList = fetchAudio(folder_name);
        mediaFilesAdapter = new MediaFilesAdapter(mediaFilesArrayList, this, mediaType,0);
        recyclerView.setAdapter(mediaFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mediaFilesAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchVideo(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        String sort_value = preferences.getString("sort", "abcd");

        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        if (sort_value.equals("sortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
        } else if (sort_value.equals("sortSize")) {
            sortOrder = MediaStore.MediaColumns.SIZE + " DESC";
        } else if (sort_value.equals("sortDate")) {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        } else {
            sortOrder = MediaStore.Video.Media.DURATION + " DESC";
        }


        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArg, sortOrder);
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
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        String sort_value = preferences.getString("sort", "abcd");

        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        if (sort_value.equals("sortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
        } else if (sort_value.equals("sortSize")) {
            sortOrder = MediaStore.MediaColumns.SIZE + " DESC";
        } else if (sort_value.equals("sortDate")) {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        } else {
            sortOrder = MediaStore.Video.Media.DURATION + " DESC";
        }

        String selection = MediaStore.Audio.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArg, sortOrder);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_media);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Sort By");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                        dialogInterface.dismiss();
                    }
                });
                String[] items = {"Name (A to Z)", "Size (Big to Small)", "Date (New to Old)", "Length (Long to Short)"};
                //checkedItem: default selected item, if 0 then sort by Name, select -1
                alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                //Sort by Name (A to Z)
                                editor.putString("sort", "sortName");

                                break;
                            case 1:
                                editor.putString("sort", "sortSize");
                                break;
                            case 2:
                                editor.putString("sort", "sortDate");
                                break;
                            case 3:
                                editor.putString("sort", "sortLength");
                                break;

                        }
                    }
                });
                alertDialog.create().show();
                break;
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String inputs = newText.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media : mediaFilesArrayList) {
            if (media.getTitle().toLowerCase(Locale.ROOT).contains(inputs)) {
                //If the search item contains the name of some media file, show the result
                mediaFiles.add(media);
            }
        }
        MediaFilesActivity.mediaFilesAdapter.updateMediaFiles((mediaFiles));
        return true;
    }
}