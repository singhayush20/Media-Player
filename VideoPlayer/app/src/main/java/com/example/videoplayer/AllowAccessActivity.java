package com.example.videoplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AllowAccessActivity extends AppCompatActivity {
    private static final String TAG = AllowAccessActivity.class.getSimpleName();
    Button allowButton;
    //Request code to ask for permission to access storage if the user does not grant it
    public static final int STORAGE_PERMISSION_REQUEST = 10;//any number
    public static final int REQUEST_PERMISSION_SETTINGS = 20;
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
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG + " ###", "(onActivityResult()) permission is granted!");
                        Toast.makeText(AllowAccessActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                        finish();
                    }
                }
            });

    /**
     * {@inheritDoc}
     * <p>
     * Perform initialization of all fragments.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);
        allowButton = findViewById(R.id.allowAccessButton);
//        SharedPreferences preferences = getSharedPreferences("AllowAccess", MODE_PRIVATE);
//        String value = preferences.getString("Allow", "");
//        Log.i(TAG + " ###", "(onCreate) value obtained for \"Allow\" from shared Preferences: " + value);
//        if (value.equals("OK")) {
//            //Navigate to next activity
//            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
//            Log.i(TAG + " ###", "(onCreate) value is OK, MainActivity started!");
//            finish();
//        } else {
//            Log.i(TAG + " ###", "(onCreate) value is not OK, entered else");
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString("Allow", "OK");
//            editor.apply();
//        }
        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First we check the that the user has granted the storage permission or not
                //Then navigate to main activity
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG + " ###", "(onClick()) permission is found to be granted, starting MainActivity");
                    Toast.makeText(AllowAccessActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                } else {
                    //Ask for the permission again
                    Log.i(TAG + " ###", "(onClick())Permission is not granted, entered else");
                    //Toast.makeText(AllowAccessActivity.this, "Storage Permission required!", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, STORAGE_PERMISSION_REQUEST);
                }

            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//
//    }

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
        Log.i(TAG + " ###", " (onResume()) checking if permission is granted!");
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG + " ###", "(onResume()) permission is granted!");
            //Toast.makeText(AllowAccessActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG + " ###", "entered the onRequestPermissionResult() method, requestCode: " + requestCode);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    Log.i(TAG + " ###", " permission is denied, showRationale: " + showRationale);

                    //If the user clicks on Never Ask again, we display a dialog
                    if (!showRationale) {
                        Log.i(TAG + " ###", " onRequestPermissionsResult: entered the if(), showRationale: " + (showRationale));

                        //user clicked on Never Ask Again button
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("App Permission")
                                .setMessage("For playing media files, the app needs to access the storage" +
                                        "\n\n" + "Now follow the below steps" + "\n\n" +
                                        "1.Open Settings from the button below\n" +
                                        "2.Click on Permissions\n" +
                                        "3.Allow access for storage")
                                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Move to settings
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        Log.i(TAG+" ###","(onClick()) uri is: "+uri);

                                        requestPermissionLauncher.launch(intent);//, REQUEST_PERMISSION_SETTINGS);
                                    }
                                }).create().show();
                    } else //User clicked on Deny
                    {
                        Log.i(TAG + " ###", " (onRequestPermissionsResult) entered the else block(inner): ");
                        //Ask for the permission again
                        Toast.makeText(AllowAccessActivity.this, "Storage Permission required!", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, STORAGE_PERMISSION_REQUEST);
                    }
                } else { //User clicked on Allow
                    //Navigate to MainActivity
                    Log.i(TAG + " ###", "(onRequestPermissionsResult) entered the outer else, permisson is granted");
                    Toast.makeText(AllowAccessActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                }
            }
        }
    }
}
