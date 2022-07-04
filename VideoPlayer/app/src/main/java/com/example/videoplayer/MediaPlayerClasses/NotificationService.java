package com.example.videoplayer.MediaPlayerClasses;

import static com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity.audioTitle;
import static com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity.mAudioFilesArrayList;
import static com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity.player;
import static com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity.position;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.videoplayer.R;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class NotificationService extends Service {
    private static final String TAG=NotificationService.class.getSimpleName();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.i(TAG+" ###","entered onCreate() in NotificationService()");

        PlayerNotificationManager.Builder playerNotificationManager=new PlayerNotificationManager.Builder(getApplicationContext(),1,"Media Player App");
        playerNotificationManager.setNextActionIconResourceId(R.id.exo_next);
        playerNotificationManager.setPreviousActionIconResourceId(R.id.exo_prev);
        playerNotificationManager.setPlayActionIconResourceId(com.google.android.exoplayer2.R.id.exo_play);
        playerNotificationManager.setPauseActionIconResourceId(R.id.exo_pause);
        PlayerNotificationManager notificationManager=playerNotificationManager.build();
        notificationManager.setPlayer(player);
        notificationManager.setColorized(true);
        notificationManager.setUseChronometer(true);
        notificationManager.setUseNextActionInCompactView(true);
        notificationManager.setUsePreviousActionInCompactView(true);
        notificationManager.setUseNextAction(true);
        notificationManager.setUsePreviousAction(true);
        notificationManager.setUsePlayPauseActions(true);
        notificationManager.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);

        notificationManager.setSmallIcon(R.drawable.ic_sharp_music_note_24);
        playerNotificationManager.setChannelDescriptionResourceId(1);

        playerNotificationManager.setMediaDescriptionAdapter(createMediaDescriptionAdapter());

//        PlayerNotificationManager playerNotificationManager1=new PlayerNotificationManager(getApplicationContext(), 1, 2, createMediaDescriptionAdapter(), new PlayerNotificationManager.NotificationListener() {
//            @Override
//            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
//                PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
//                if(dismissedByUser) {
//                    player.stop();
//                }
//            }
//
//            @Override
//            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
//                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
//                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
//                Log.i(TAG+" ###","entered onNotificationPosted: "+notificationId);
//                if(notificationId!=0)
//                    startForeground(notificationId,notification);
//                else Log.e(TAG+" ###","notificationId is zero: "+notificationId);
//            }
//        },null,R.drawable.ic_sharp_music_note_24,R.drawable.ic_play,R.drawable.ic_pause,
//                null,R.drawable.ic_rewind,R.drawable.ic_fast_forward,R.drawable.ic_previous,R.drawable.ic_next,1);
        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {

                PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
                if(dismissedByUser) {
                    player.stop();
                }
            }

            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
                Log.i(TAG+" ###","entered onNotificationPosted: "+notificationId);
                if(notificationId!=0)
                    startForeground(notificationId,notification);
                else Log.e(TAG+" ###","notificationId is zero: "+notificationId);
            }
        });

    }
    private PlayerNotificationManager.MediaDescriptionAdapter createMediaDescriptionAdapter()
    {
        return new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public CharSequence getCurrentContentTitle(Player player1) {
                return audioTitle;
            }

            @Nullable
            @Override
            public PendingIntent createCurrentContentIntent(Player player1) {
                Intent intent=new Intent(getApplicationContext(), AudioPlayerActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("media_title", audioTitle);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("mediaArrayList", mAudioFilesArrayList);
                intent.putExtras(bundle);
                return PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            }

            @Override
            public CharSequence getCurrentContentText(Player player1) {
                    return "MediaPlayer";
            }

            @Nullable
            @Override
            public Bitmap getCurrentLargeIcon(Player player1, PlayerNotificationManager.BitmapCallback callback) {
                return BitmapFactory.decodeResource(getResources(),R.drawable.notification_large_icon);
            }
        };
    }
}
