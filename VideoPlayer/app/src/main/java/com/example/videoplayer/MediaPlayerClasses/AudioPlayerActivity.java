package com.example.videoplayer.MediaPlayerClasses;

import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.FOLDER_NAME_KEY;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MY_PREF;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.R;
import com.example.videoplayer.RecyclerViewClasses.MediaFilesAdapter;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.io.File;
import java.util.ArrayList;

public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AudioPlayerActivity.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_id";

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
    PlayerNotificationManager.Builder playerNotificationManagerBuilder;
     public static PlayerNotificationManager playerNotificationManager;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.exo_simple_player_view);
        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e(TAG + " ###", "in VideoPlayerActivity: " + e);
        }
        playerView = findViewById(R.id.exoplayer_view_audio);
        playerView.setControllerShowTimeoutMs(0);//non positive value will keep it visible
        playerView.setControllerHideOnTouch(false);
        playerView.showController();

        root = findViewById(R.id.root_layout_audio);
        audioBack = findViewById(R.id.audio_back_audio);
        audioList = findViewById(R.id.audio_list);
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        playlistTitle = findViewById(R.id.playlistTitle);
        title = findViewById(R.id.audiofile_title);
        menu_more = findViewById(R.id.audio_more);

        position = getIntent().getIntExtra("position", 1);
        audioTitle = getIntent().getStringExtra("media_title");
        mAudioFilesArrayList = getIntent().getExtras().getParcelableArrayList("mediaArrayList");
        title.setText(audioTitle);
        SharedPreferences preferences = this.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        listTitle = preferences.getString(FOLDER_NAME_KEY, "DEFAULT_FOLDER_NAME");
        playlistTitle.setText(listTitle);

        audioList.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        audioBack.setOnClickListener(this);
        menu_more.setOnClickListener(this);

      createNotificationChannel();

        playerNotificationManagerBuilder = new PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID);
        playerNotificationManagerBuilder.setMediaDescriptionAdapter(new DescriptionAdapter());
        playerNotificationManagerBuilder.setNotificationListener(new NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
                if (dismissedByUser) {
                    player.stop();

                }
            }

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
            }
        });
        playerNotificationManager = playerNotificationManagerBuilder.build();


        playAudio();

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);


    }

    private void playAudio() {
        String path = mAudioFilesArrayList.get(position).getPath();
        Uri uri = Uri.parse(path);

        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(this);
        builder.setSeekForwardIncrementMs(10000);
        builder.setSeekBackIncrementMs(10000);
        player = builder.build();


        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);

        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mAudioFilesArrayList.size(); i++) {
            new File(String.valueOf(mAudioFilesArrayList.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
//        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        //Set audio attributes for audio focus
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        player.setAudioAttributes(audioAttributes, true);
        player.setPlaybackParameters(parameters);
        //player.prepare(concatenatingMediaSource);
        player.setMediaSource(concatenatingMediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        playerView.setPlayer(player);
        playerNotificationManager.setPlayer(player);
        player.setPlayWhenReady(true);//added in course
        player.play();
        player.seekTo(position, C.TIME_UNSET);
        //show error if media does not play
        playError();


    }


    private void playError() {
        //Player.EventListener is deprecated
        player.addListener(new Player.Listener() {


            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(AudioPlayerActivity.this, "Audio Playing Error", Toast.LENGTH_SHORT).show();
            }


//            @Override
//            public void onPlaybackStateChanged(int playbackState) {
//                Player.Listener.super.onPlaybackStateChanged(playbackState);
//                if(playbackState==ExoPlayer.STATE_READY&&player.getPlayWhenReady()) {
//                    Log.i(TAG + " ###", "onPlayerStateChanged: PLAYING");
//                    mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
//                            player.getCurrentPosition(), 1f);
//                }
//                else if(playbackState == ExoPlayer.STATE_READY&&!player.getPlayWhenReady()){
//                    Log.i(TAG, "onPlayerStateChanged: PAUSED");
//                    mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
//                            player.getCurrentPosition(), 1f);
//                }
//                    mMediaSession.setPlaybackState(mStateBuilder.build());
//                Log.i(TAG+"###","in onPlaybackStateChanged() calling showNotification");
//                showNotification(mStateBuilder.build());
//            }
          });
        player.setPlayWhenReady(true);
      }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG+" ###","onDestroy called");
        playerNotificationManager.setPlayer(null);



        if (player != null) {
            player.stop();
            player.release();
            player = null;

        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onResume() {
        super.onResume();

        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onStop() {
        super.onStop();



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.exo_play:
                //player.play();
                break;
            case R.id.exo_pause:
                //player.pause();
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
                //if (player != null)
                    //player.release();
                onBackPressed();
                break;
            case R.id.audio_list:
                PlaylistDialog playlistDialog = new PlaylistDialog(mAudioFilesArrayList, mediaFilesAdapter);
                playlistDialog.show(getSupportFragmentManager(), playlistDialog.getTag());
                break;
            case R.id.audio_more:
                PopupMenu popupMenu = new PopupMenu(this, menu_more);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.actions_video, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.share_file:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                String filepath = mAudioFilesArrayList.get(position).getPath();
                                Uri uri = Uri.parse(filepath);
                                shareIntent.setType("*/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                startActivity(Intent.createChooser(shareIntent, "Share File using"));
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

                break;
        }
    }


    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return audioTitle;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            Intent notificationIntent = new Intent(getApplicationContext(), AudioPlayerActivity.class);
            return PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return "Description";
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return null;
        }
    }



}