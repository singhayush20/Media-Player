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
//    private static MediaSessionCompat mMediaSession;
//    private PlaybackStateCompat.Builder mStateBuilder;
//    private NotificationManager mNotificationManager;


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
        // Initialize the Media Session.
        //initializeMediaSession();

        playAudio();

    }
    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
//    private void initializeMediaSession() {
//
//        // Create a MediaSessionCompat.
//        mMediaSession = new MediaSessionCompat(this, TAG);
//
//        // Enable callbacks from MediaButtons and TransportControls.
//        mMediaSession.setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//        // Do not let MediaButtons restart the player when the app is not visible.
//        mMediaSession.setMediaButtonReceiver(null);
//
//        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
//        mStateBuilder = new PlaybackStateCompat.Builder()
//                .setActions(
//                        PlaybackStateCompat.ACTION_PLAY |
//                                PlaybackStateCompat.ACTION_PAUSE |
//                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
//
//        mMediaSession.setPlaybackState(mStateBuilder.build());
//
//
//        // MySessionCallback has methods that handle callbacks from a media controller.
//        mMediaSession.setCallback(new MySessionCallback());
//
//        // Start the Media Session since the activity is active.
//        mMediaSession.setActive(true);
//
//    }

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
//    // TODO (1): Create a method that shows a MediaStyle notification with two actions (play/pause, skip to previous). Clicking on the notification should launch this activity. It should take one argument that defines the state of MediaSession.
//    /**
//     * Shows Media Style notification, with an action that depends on the current MediaSession
//     * PlaybackState.
//     * @param state The PlaybackState of the MediaSession.
//     */
//    private void showNotification(PlaybackStateCompat state) {
//        Log.i(TAG+"###","entered showNotification()");
//        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,CHANNEL_ID);
//
//        int icon;
//        String play_pause;
//        if(state.getState() == PlaybackStateCompat.STATE_PLAYING){
//            icon = R.drawable.ic_pause;
//            play_pause = getString(R.string.pause);
//        } else {
//            icon = R.drawable.ic_play;
//            play_pause = getString(R.string.play);
//        }
//
//
//        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
//                icon, play_pause,
//                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
//                        PlaybackStateCompat.ACTION_PLAY_PAUSE));
//
//        NotificationCompat.Action restartAction = new NotificationCompat
//                .Action(R.drawable.ic_previous, getString(R.string.restart),
//                MediaButtonReceiver.buildMediaButtonPendingIntent
//                        (this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
//
//        PendingIntent contentPendingIntent = PendingIntent.getActivity
//                (this, 0, new Intent(this, AudioPlayerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
//
//        builder.setContentTitle(audioTitle)
//                .setContentText("Description")
//                .setContentIntent(contentPendingIntent)
//                .setSmallIcon(R.drawable.ic_sharp_music_note_24)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .addAction(restartAction)
//                .addAction(playPauseAction)
//                .setAutoCancel(true)
//                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                        .setMediaSession(mMediaSession.getSessionToken())
//                        .setShowActionsInCompactView(0,1))
//                .setChannelId(CHANNEL_ID);
//
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//           // mNotificationManager=getSystemService(NotificationManager.class);
//            //mNotificationManager.createNotificationChannel(channel);
//
//
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mNotificationManager.createNotificationChannel(channel);
//
//        Log.i(TAG+" ###","calling notifiy: notificationId"+NOTIFICATION_ID+" CHANNEL ID: "+CHANNEL_ID);
//        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
//    }

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
//        if (player.isPlaying()) {
//            player.stop();//stop if back button is pressed
//
//        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG+" ###","onDestroy called");
        playerNotificationManager.setPlayer(null);
        //releasePlayer();
        //mMediaSession.setActive(false);

//        if (playerNotificationManager != null) {
//            playerNotificationManager.setPlayer(null);
//            Log.i(TAG+" ###","playerNotificationManager if");
//
//        }


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
//        if (player != null) {
//            player.setPlayWhenReady(false);
//            player.getPlaybackState();
//
//        }
    }
//    void releasePlayer()//added in course
//    {
//        mNotificationManager.cancelAll();
//
//        if (player != null) {
//            player.stop();
//            player.release();
//            player = null;
//
//        }
//    }

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
//    /**
//     * Media Session Callbacks, where all external clients control the player.
//     */
//    private class MySessionCallback extends MediaSessionCompat.Callback {
//        @Override
//        public void onPlay() {
//            player.setPlayWhenReady(true);
//        }
//
//        @Override
//        public void onPause() {
//            player.setPlayWhenReady(false);
//        }
//
//        @Override
//        public void onSkipToPrevious() {
//            player.seekTo(0);
//        }
//    }

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
//    /**
//     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
//     */
//    public static class MediaReceiver extends BroadcastReceiver {
//
//        public MediaReceiver() {
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            MediaButtonReceiver.handleIntent(mMediaSession, intent);
//        }
//    }


}