package com.example.videoplayer.MediaPlayerClasses;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.videoplayer.R;

public class BrightnessDialog extends AppCompatDialogFragment {
    private TextView brightnessNumber;
    private ImageView cross;
    private SeekBar seekBar;
    private ActivityResultLauncher<Intent> requestBrightnessPermissionLauncher = registerForActivityResult
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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.brt_dialog_item, null);
        builder.setView(view);
        cross = view.findViewById(R.id.brt_close);
        brightnessNumber = view.findViewById(R.id.brt_number);
        seekBar = view.findViewById(R.id.brt_seekbar);
        int brightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
        brightnessNumber.setText(brightness + "");
        seekBar.setProgress(brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Context context = getContext().getApplicationContext();
                boolean canWrite = Settings.System.canWrite(context);
                if (canWrite) {
                    int sBrightness = i * 255 / 255;
                    brightnessNumber.setText(sBrightness + "");
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, sBrightness);
                } else {
                    Toast.makeText(context,"Enable write settings for brightness control",Toast.LENGTH_SHORT).show();
                    Intent brightnessIntent=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    brightnessIntent.setData(Uri.parse("package:"+context.getPackageName()));
                    requestBrightnessPermissionLauncher.launch(brightnessIntent);
//                    startActivityForResult(brightnessIntent,0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return builder.create();
    }
}
