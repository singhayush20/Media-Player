package com.example.videoplayer.MediaPlayerClasses;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = VideoPlayerActivity.class.getSimpleName();
    PlayerView playerView;
    SimpleExoPlayer player;
    //    StyledPlayerView playerView;
//    ExoPlayer player;
    MediaFilesAdapter mediaFilesAdapter;
    int position;
    String videoTitle;
    ArrayList<MediaFiles> mVideoFilesArrayList;
    TextView title;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, previousButton;
    ImageView videoBack, lock, unlock, scaling, menu_more;
    RelativeLayout root;
    private ControlsMode controlsMode;

    private enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    //Horizontal recycler view variables
    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlaybackIconsAdapter playbackIconsAdapter;
    RecyclerView recyclerViewIcons;
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PlaybackParameters parameters;
    ImageView videoList;
    View decorView;

    float speed;
    private ActivityResultLauncher<Intent> audioEffect = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

                /**
                 * Called when result is available
                 *
                 * @param result
                 */
                @Override
                public void onActivityResult(ActivityResult result) {

                }
            });
    //For FilePicker
//    DialogProperties dialogProperties;
//    FilePickerDialog filePickerDialog;

//    private final ActivityResultLauncher<String> fileChooserIntentLauncher=registerForActivityResult
//            (new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//
//                /**
//                 * Called when result is available
//                 *
//                 * @param result
//                 */
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    assert result.getData() != null;
//
//                    subtitleUri=result.getData().getData();
//                    Log.i(TAG+"###","result.getData().getData() for file chooser: "+subtitleUri);
//                    //Cursor cursor=getContentResolver().query(subtitleUri,null,null,null,null);
//
//                }
//            });

    Uri subtitleUri;
    PictureInPictureParams.Builder pictureInPicture;
    boolean isCrossChecked;
    FrameLayout eqContainer;

    ActivityResultLauncher<String> fileChooserIntentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    Log.i(TAG + " ###", "Uri returned for the subtitle file:" +
                            "\n" + uri.toString());
                    // Handle the returned Uri
                    subtitleUri = uri;
                }
            });
    WindowInsetsControllerCompat windowInsetsController;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());

        setFullScreen();
        setContentView(R.layout.activity_video_player);


        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.e(TAG + " ###", "in VideoPlayerActivity: " + e);
        }


        playerView = findViewById(R.id.exoplayer_view);
        //Hide the action bar find alternative
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("media_title");
        mVideoFilesArrayList = getIntent().getExtras().getParcelableArrayList("mediaArrayList");
        //To set the default screen orientation according to width and height of video
        screenOrientation();
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        title = findViewById(R.id.video_title);
        title.setText(videoTitle);

        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.exo_scaling);
        root = findViewById(R.id.root_layout);
        nightMode = findViewById(R.id.night_mode);
        //Video Playlist
        videoList = findViewById(R.id.video_list);
        videoList.setOnClickListener(this);
        recyclerViewIcons = findViewById(R.id.recyclerView_icon);
        eqContainer = findViewById(R.id.equalizer);
        menu_more=findViewById(R.id.video_more);
        menu_more.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPicture = new PictureInPictureParams.Builder();
        }
        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_nightmode, "Night Mode"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate_screen, "Rotate"));

        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

//        dialogProperties=new DialogProperties();
//        filePickerDialog=new FilePickerDialog(VideoPlayerActivity.this);
//        filePickerDialog.setTitle("Select a Subtitle File");


        /*
        Horizontal list of playback icons
         */
        playbackIconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconsAdapter);
        playbackIconsAdapter.notifyDataSetChanged();
        playbackIconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(int position) {
                //expand and collapse
                if (position == 0) {
                    if (expand) {
                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_nightmode, "Night Mode"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate_screen, "Rotate"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelArrayList.size() == 5) {
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volumeoff, "Mute"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_fast_forward, "Speed"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_subtitle, "Subtitle"));
                        }
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_left, ""));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = true;
                    }

                } else if (position == 1) {
                    //Night Mode
                    if (dark) {
                        //Set visibility to gone
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_nightmode, "Night"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {
                        nightMode.setVisibility(View.VISIBLE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_nightmode, "Day"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        dark = true;

                    }
                } else if (position == 2) {
                    //Popup
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Rational aspectRatio = new Rational(16, 9);
                        pictureInPicture.setAspectRatio(aspectRatio);
                        enterPictureInPictureMode(pictureInPicture.build());
                    } else {
                        Toast.makeText(getApplicationContext(), "Picture-in-Picture is not supported!", Toast.LENGTH_SHORT).show();
                        Log.wtf(TAG + " ###", "Picture in Picture not supported: true");
                    }

                } else if (position == 3) {
                    //Equalizer
//                    if(eqContainer.getVisibility()==View.GONE) {
//                        eqContainer.setVisibility(View.VISIBLE);
//                    }
//                    final int sessionID=player.getAudioSessionId();


                    //For built-in equalizer
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if ((intent.resolveActivity(getPackageManager()) != null)) {
                        audioEffect.launch(intent);
//                        startActivityForResult(intent,123);
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "No Equalizer Found", Toast.LENGTH_SHORT).show();
                    }
                    playbackIconsAdapter.notifyDataSetChanged();


                } else if (position == 4) {
                    //Rotate
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconsAdapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playbackIconsAdapter.notifyDataSetChanged();
                    }


                } else if (position == 5) {
                    //Mute
                    if (mute) {
                        player.setVolume(1);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volumeoff, "Mute"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        mute = false;
                    } else {
                        player.setVolume(0);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume, "Unmute"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        mute = true;
                    }

                } else if (position == 6) {
                    //Volume
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconsAdapter.notifyDataSetChanged();

                } else if (position == 7) {
                    //Brightness
                    BrightnessDialog brightnessDialog = new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconsAdapter.notifyDataSetChanged();


                } else if (position == 8) {
                    //Playback Speed
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select Playback Speed").setPositiveButton("OK", null);
                    String[] items = {"0.5x", "1x Normal Speed", "1.25x", "1.5x", "2x"};
                    final int[] checkedItem = {-1};//keep it -1 if no option selected by default
                    alertDialog.setSingleChoiceItems(items, checkedItem[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
//                            Toast.makeText(getApplicationContext(),"checkedItem[0]: "+checkedItem[0],Toast.LENGTH_SHORT).show();
                            switch (which) {
                                case 0:
                                    speed = 0.5f;
                                    checkedItem[0] = which;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed = 1f;
                                    checkedItem[0] = which;
//                                    Toast.makeText(getApplicationContext(),"checkedItem[0]: "+checkedItem[0],Toast.LENGTH_SHORT).show();
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed = 1.25f;
                                    checkedItem[0] = which;
//                                    Toast.makeText(getApplicationContext(),"checkedItem[0]: "+checkedItem[0],Toast.LENGTH_SHORT).show();
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed = 1.5f;
                                    checkedItem[0] = which;
//                                    Toast.makeText(getApplicationContext(),"checkedItem[0]: "+checkedItem[0],Toast.LENGTH_SHORT).show();

                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed = 2f;
                                    checkedItem[0] = which;
//                                    Toast.makeText(getApplicationContext(),"checkedItem[0]: "+checkedItem[0],Toast.LENGTH_SHORT).show();
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();

                } else if (position == 9) {
                    //Subtitle


//                    Intent fileChooserIntent=new Intent();
//                    fileChooserIntent.setType("file/*");
//                    fileChooserIntent.setAction(Intent.ACTION_GET_CONTENT);
                    //fileChooserIntentLauncher.launch("file/*");
                    // playVideoSubtitle(subtitleUri);

                }
            }
        });
        //Play video through URI
        playVideo();


    }

    private void playVideo() {
        String path = mVideoFilesArrayList.get(position).getPath();
        Uri uri = Uri.parse(path);
        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(this);
        /*
        player = new ExoPlayer.Builder(this).build();
        instead use the below method to also set the fast forward and backward
        https://stackoverflow.com/questions/69710780/how-to-resolve-exoplayer-error-aapt-error-attribute-fastforward-increment-no
         */
        builder.setSeekForwardIncrementMs(10000);
        builder.setSeekBackIncrementMs(10000);
        player = builder.build();

        //DefaultDataSourceFactory is deprecated (8:30)
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        //, Util.getUserAgent(this,"app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFilesArrayList.size(); i++) {
            new File(String.valueOf(mVideoFilesArrayList.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        //Prevent screen from dimming after screen timeout reach
        playerView.setKeepScreenOn(true);
        //Set audio attributes for audio focus
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();
        player.setAudioAttributes(audioAttributes, true);
        player.setPlaybackParameters(parameters);
        //player.prepare(concatenatingMediaSource);
        player.setMediaSource(concatenatingMediaSource);
        player.prepare();
        player.play();
        player.seekTo(position, C.TIME_UNSET);
        //show error if media does not play
        playError();

    }
//    private void playVideoSubtitle(Uri subtitle) {
//
//        long oldPosition=player.getCurrentPosition();
//        player.stop();
//
//        String path = mVideoFilesArrayList.get(position).getPath();
//        Uri uri = Uri.parse(path);
//        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(this);
//        /*
//        player = new ExoPlayer.Builder(this).build();
//        instead use the below method to also set the fast forward and backward
//        https://stackoverflow.com/questions/69710780/how-to-resolve-exoplayer-error-aapt-error-attribute-fastforward-increment-no
//         */
//        builder.setSeekForwardIncrementMs(10000);
//        builder.setSeekBackIncrementMs(10000);
//        player = builder.build();
//
//        //DefaultDataSourceFactory is deprecated (8:30)
//        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
//        //, Util.getUserAgent(this,"app"));
//        concatenatingMediaSource = new ConcatenatingMediaSource();
//        for (int i = 0; i < mVideoFilesArrayList.size(); i++) {
//            new File(String.valueOf(mVideoFilesArrayList.get(i)));
//            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
//                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
//
//            Format.Builder textFormatBuilder= new Format.Builder();
//            textFormatBuilder.setContainerMimeType(MimeTypes.APPLICATION_SUBRIP);
//            textFormatBuilder.setAccessibilityChannel(Format.NO_VALUE);
//            textFormatBuilder.setLanguage("app");
//            Format textFormat=textFormatBuilder.build();
//
//            MediaItem.SubtitleConfiguration.Builder subtitleConfigBuilder=new MediaItem.SubtitleConfiguration.Builder(subtitle);
//            MediaItem.SubtitleConfiguration subtitleConfiguration=subtitleConfigBuilder.build();
//            SingleSampleMediaSource subtitleSource=new SingleSampleMediaSource(subtitle,dataSourceFactory,textFormat,C.TIME_UNSET);
////            Format textFormat=Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,Format.NO_VALUE,"app");
////            MediaSource subtitleSource=new SingleSampleMediaSource.Factory(dataSourceFactory)
////                    .setTreatLoadErrorsAsEndOfStream(true)
////                    .createMediaSource(subtitleConfiguration,C.TIME_UNSET);
//            MergingMediaSource mergingMediaSource=new MergingMediaSource(mediaSource,subtitleSource);
//
//
//            concatenatingMediaSource.addMediaSource(mergingMediaSource);
//        }
//        playerView.setPlayer(player);
//        //Prevent screen from dimming after screen timeout reach
//        playerView.setKeepScreenOn(true);
//        //Set audio attributes for audio focus
//        AudioAttributes audioAttributes=new AudioAttributes.Builder()
//                .setUsage(C.USAGE_MEDIA)
//                .setContentType(C.CONTENT_TYPE_MOVIE)
//                .build();
//        player.setAudioAttributes(audioAttributes,true);
//        player.setPlaybackParameters(parameters);
//        //player.prepare(concatenatingMediaSource);
//        player.setMediaSource(concatenatingMediaSource);
//        player.prepare();
//        player.play();
//        player.seekTo(position, oldPosition);
//        //show error if media does not play
//        playError();
//
//    }

    private void screenOrientation() {
        try {
            //Check height and width of video using bitmap
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFilesArrayList.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this, uri);
            bitmap = retriever.getFrameAtTime();
            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth > videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception e) {
            Log.e("Media Meta Data Retriever:", "Error changing orientation!");

        }
    }

    private void playError() {
        //Player.EventListener is deprecated
        player.addListener(new Player.Listener() {


            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying())
            player.stop();//stop if back button is pressed
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
            if (isInPictureInPictureMode()) {
                player.setPlayWhenReady(true);
            } else {
                player.setPlayWhenReady(false);
                player.getPlaybackState();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        player.setPlayWhenReady(true);
        player.getPlaybackState();
//        setFullScreen();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
//                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.);
//


        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
//        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.);
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
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
                    videoTitle = mVideoFilesArrayList.get(position).getTitle();
                    title.setText(videoTitle);
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No next Video", Toast.LENGTH_SHORT).show();
                    position--;
                }
                break;
            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    videoTitle = mVideoFilesArrayList.get(position).getTitle();
                    title.setText(videoTitle);
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No previous Video", Toast.LENGTH_SHORT).show();
                    position++;
                }
                break;
            case R.id.video_back:
                if (player != null)
                    player.release();
                finish();
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "UNLOCKED", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
                controlsMode = controlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "LOCKED", Toast.LENGTH_SHORT).show();
                break;
            case R.id.video_list:
                PlaylistDialog playlistDialog = new PlaylistDialog(mVideoFilesArrayList, mediaFilesAdapter);
                playlistDialog.show(getSupportFragmentManager(), playlistDialog.getTag());
                break;
            case R.id.video_more:
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
                                String filepath=mVideoFilesArrayList.get(position).getPath();
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

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);
            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        isCrossChecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            playerView.hideController();
        } else {
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isCrossChecked)//user clicks on stop button
        {
            player.release();
            finish();
        }
    }
}
