package com.example.videoplayer.MediaPlayerClasses;

import static com.example.videoplayer.MediaPlayerClasses.ActivityAudioPlayer.mAudioFilesArrayList;
import static com.example.videoplayer.MediaPlayerClasses.ActivityAudioPlayer.position;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.R;
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

public class PlayerNotificationService extends Service {
    private static final String TAG = AudioPlayerActivity.class.getSimpleName();
    private static final String CHANNEL_ID = "Notification-channel-id";
    private static final int NOTIFICATION_ID = 3004;
    PlayerView playerView;
    public static SimpleExoPlayer player;
//    RelativeLayout root;
//    ImageView audioBack, audioList, menu_more;
//    ImageView nextButton, previousButton;
//
//    TextView playlistTitle, title;
    ConcatenatingMediaSource concatenatingMediaSource;
//    static ArrayList<MediaFiles> mAudioFilesArrayList;
//    static int position;
//    MediaFilesAdapter mediaFilesAdapter;
    PlaybackParameters parameters;
//    static String audioTitle;
//    String listTitle;
    PlayerNotificationManager.Builder playerNotificationManager;
    PlayerNotificationManager notificationManager;
    private static final int CHANNEL_NAME=1;
    private static final int CHANNEL_DESC=2;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();



    }

    private void playError() {
        //Player.EventListener is deprecated
        player.addListener(new Player.Listener() {


            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(getApplicationContext(), "Audio Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String path="";
        Uri uri=null;
        mAudioFilesArrayList=(ArrayList<MediaFiles>) intent.getSerializableExtra("list");
        position=intent.getIntExtra("position",1);
        path = mAudioFilesArrayList.get(position).getPath();
        uri = Uri.parse(path);



//        PlayerView.Builder builder = new ExoPlayer.Builder(this);
//        builder.setSeekForwardIncrementMs(5000);
//        builder.setSeekBackIncrementMs(5000);
//        player = builder.build();
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
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        //Set audio attributes for audio focus
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        player.setAudioAttributes(audioAttributes, true);
        player.setPlaybackParameters(parameters);
        player.setPlayWhenReady(true);
        //player.prepare(concatenatingMediaSource);
        player.setMediaSource(concatenatingMediaSource);
        player.prepare();


        player.play();
        player.seekTo(position, C.TIME_UNSET);
        playerNotificationManager= new PlayerNotificationManager.Builder(this,NOTIFICATION_ID,CHANNEL_ID)
                .setChannelNameResourceId(CHANNEL_NAME)
                .setChannelDescriptionResourceId(CHANNEL_DESC)
                .setMediaDescriptionAdapter(new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public CharSequence getCurrentContentTitle(Player player) {
                        return getCurrentContentText(player);

                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        Intent intent=new Intent(getApplicationContext(),ActivityAudioPlayer.class);
                        return PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                    }

                    @Nullable
                    @Override
                    public CharSequence getCurrentContentText(Player player) {
                        return "Now Playing: ";
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        Bitmap largeIcon=getCurrentLargeIcon(player,callback);
                        if(largeIcon==null)
                            return BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.notification_large_icon);
                        return largeIcon;
                    }
                });
        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                if(dismissedByUser)
                    stopSelf();
            }

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                startForeground(notificationId,notification);
            }
        });
        notificationManager=playerNotificationManager.build();
        notificationManager.setPlayer(player);



        //playerNotificationManager=PlayerNotificationManager.cre
        //show error if media does not play
        playError();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.setPlayer(null);
        player.release();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}

