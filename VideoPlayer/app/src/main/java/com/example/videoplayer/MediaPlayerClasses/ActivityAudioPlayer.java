package com.example.videoplayer.MediaPlayerClasses;

import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.FOLDER_NAME_KEY;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MY_PREF;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.R;
import com.example.videoplayer.RecyclerViewClasses.MediaFilesAdapter;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;

public class ActivityAudioPlayer extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ActivityAudioPlayer.class.getSimpleName();
    private static final  String CHANNEL_ID = "Notification-channel-id";
    private static final int NOTIFICATION_ID=3004;
    PlayerView playerView;
    public static SimpleExoPlayer player;
    RelativeLayout root;
    ImageView audioBack, audioList, menu_more;
    ImageView nextButton, previousButton;

    TextView playlistTitle, title;
    ConcatenatingMediaSource concatenatingMediaSource;
    static ArrayList<MediaFiles> mAudioFilesArrayList;
    static int position;
    MediaFilesAdapter mediaFilesAdapter;
    PlaybackParameters parameters;
    static String audioTitle;
    String listTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exo_simple_player_view);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e(TAG + " ###", "in VideoPlayerActivity: " + e);
        }
        playerView = findViewById(R.id.exoplayer_view_audio);
        playerView.setControllerShowTimeoutMs(0);//non positive value will keep it visible
        playerView.setControllerHideOnTouch(false);

        root = findViewById(R.id.root_layout_audio);
        audioBack = findViewById(R.id.audio_back_audio);
        audioList = findViewById(R.id.audio_list);
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        playlistTitle = findViewById(R.id.playlistTitle);
        title=findViewById(R.id.audiofile_title);
        menu_more=findViewById(R.id.audio_more);

        position=getIntent().getIntExtra("position",1);
        audioTitle=getIntent().getStringExtra("media_title");
        mAudioFilesArrayList=getIntent().getExtras().getParcelableArrayList("mediaArrayList");
        if(mAudioFilesArrayList==null)
            throw new NullPointerException();
        title.setText(audioTitle);
        SharedPreferences preferences=this.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        listTitle= preferences.getString(FOLDER_NAME_KEY,"DEFAULT_FOLDER_NAME");
        playlistTitle.setText(listTitle);

        audioList.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        audioBack.setOnClickListener(this);
        menu_more.setOnClickListener(this);
        playAudio();

    }
    void playAudio()
    {
        Intent intent= new Intent(this,PlayerNotificationService.class);
        intent.putExtra("Position",position);
        Bundle bundle=new Bundle();
        bundle.putSerializable("list",mAudioFilesArrayList);
        intent.putExtras(bundle);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.exo_play:
                player.play();
                break;
            case R.id.exo_pause:
                player.pause();
                break;
            case R.id.exo_next:
                try {
                    player.stop();
                    position++;
                    audioTitle = mAudioFilesArrayList.get(position).getTitle();
                    title.setText(audioTitle);
                    playAudio();
                } catch (Exception e) {
                    Toast.makeText(this, "No next audio file", Toast.LENGTH_SHORT).show();
                    position--;
                }
                break;
            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    audioTitle = mAudioFilesArrayList.get(position).getTitle();
                    title.setText(audioTitle);
                    playAudio();
                } catch (Exception e) {
                    Toast.makeText(this, "No previous Video", Toast.LENGTH_SHORT).show();
                    position++;
                }
                break;
            case R.id.audio_back_audio:
                if (player != null)
                    player.release();
                finish();
                break;
            case R.id.audio_list:
                PlaylistDialog playlistDialog = new PlaylistDialog(mAudioFilesArrayList, mediaFilesAdapter);
                playlistDialog.show(getSupportFragmentManager(), playlistDialog.getTag());
                break;
            case R.id.audio_more:
                PopupMenu popupMenu=new PopupMenu(this,menu_more);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.actions_video, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id= menuItem.getItemId();
                        switch(id)
                        {
                            case R.id.share_file:
                                Intent shareIntent=new Intent(Intent.ACTION_SEND);
                                String filepath=mAudioFilesArrayList.get(position).getPath();
                                Uri uri=Uri.parse(filepath);
                                shareIntent.setType("*/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                                startActivity(Intent.createChooser(shareIntent,"Share File using"));
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

                break;
        }
    }
}
