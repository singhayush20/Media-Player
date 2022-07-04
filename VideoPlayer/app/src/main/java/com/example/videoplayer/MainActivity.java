package com.example.videoplayer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.videoplayer.Fragments.FragmentAdapter;
import com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityResultLauncher<Intent> requestPermissionLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

                /**
                 * Called when result is available
                 *
                 * @param result
                 */
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.i(TAG+" ###","onActivityResult() entered");
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        Log.i(TAG + " ###", "(onActivityResult()) permission is granted!");
                        Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, AllowAccessActivity.class));
                        finish();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG+" ###","entred onCreate()");
        setContentView(R.layout.activity_main);
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
        {
            Log.i(TAG+" ###","(onCreate) permission is denied");
            Toast.makeText(MainActivity.this, "Click on Permissions and allow storage permission", Toast.LENGTH_SHORT).show();
            //If the user grants the permission manually in settings and again
            //If the permission is denied by the user and goes back to the app, onResume() is called
            // and navigates to the MainActivity, we navigate the user to
            //the settings to grant the permission
            //Include the settings code
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);

            requestPermissionLauncher.launch(intent);
        }

        //Find the viewPager that will allow the user to swipe between fragment
        ViewPager2 viewPager=findViewById(R.id.view_Pager);
        //Create an adapter that knows which fragment should be shown on each page
        FragmentAdapter fragmentAdapter=new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
        final String [] tabNames={"Videos","Music"};
        final int[] tabIcons={R.drawable.ic_sharp_slow_motion_video_24,R.drawable.ic_sharp_music_note_24};
        //Make the Tab Layout
        TabLayout tabLayout=findViewById(R.id.tabs);
        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        new TabLayoutMediator(tabLayout,viewPager,(tab,position)->
        {
            tab.setText(tabNames[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG+" ###","entered onResume()");
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG + " ###", "(onActivityResult()) permission is granted!");
            Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, AllowAccessActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.rateus:
                Uri uri=Uri.parse("https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
                Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
            case R.id.refresh_folders:
                finish();
                startActivity(getIntent());
                break;
            case R.id.share_app:
                Intent shareIntent=new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Check this app via\n"+"https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent,"Share VideoPlayer App via"));
                break;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG+" ###","onDestroy() called");
        if(AudioPlayerActivity.playerNotificationManager!=null) {
            AudioPlayerActivity.playerNotificationManager.setPlayer(null);
        }
        if(AudioPlayerActivity.player!=null)
        AudioPlayerActivity.player.release();
    }

}