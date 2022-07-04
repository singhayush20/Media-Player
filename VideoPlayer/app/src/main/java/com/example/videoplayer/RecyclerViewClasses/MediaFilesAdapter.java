package com.example.videoplayer.RecyclerViewClasses;

import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MEDIA_TYPE_AUDIO;
import static com.example.videoplayer.RecyclerViewClasses.MediaFilesActivity.MEDIA_TYPE_VIDEO;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videoplayer.MediaFiles;
import com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity;
import com.example.videoplayer.MediaPlayerClasses.VideoPlayerActivity;
import com.example.videoplayer.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.videoplayer.MediaPlayerClasses.AudioPlayerActivity.*;

import java.io.File;
import java.util.ArrayList;

public class MediaFilesAdapter extends RecyclerView.Adapter<MediaFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaList;
    BottomSheetDialog bottomSheetDialog;
    private int viewType;
    final private String TAG = MediaFilesAdapter.class.getSimpleName();
    private final String mediaType;

    public MediaFilesAdapter(ArrayList<MediaFiles> mediaList, Context context,String type,int viewType) {
        this.mediaList = mediaList;
        this.context = context;
        mediaType=type;
        this.viewType=viewType;
    }

    private Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mediaName.setText(mediaList.get(position).getDisplayName());
        String size = mediaList.get(position).getSize();//in bytes

        holder.mediaSize.setText(Formatter
                .formatFileSize(context, Long.parseLong(size)));
        double milliSeconds = Double.parseDouble(mediaList.get(position).getDuration());
        holder.mediaDuration.setText(timeConversion((long) milliSeconds));
        //Load the thumbnail
        if(mediaType.equals(MEDIA_TYPE_VIDEO)) {
            Glide.with(context).load(new File(mediaList.get(position).getPath())).into(holder.thumbnail);
        }
        else if(mediaType.equals(MEDIA_TYPE_AUDIO))
        {
            holder.mediaDuration.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(150, 150);
            layoutParams.setMarginStart(8);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,RelativeLayout.TRUE);
            holder.thumbnail_card.setLayoutParams(layoutParams);

//            holder.thumbnail_card.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        }
        if(viewType==0)
        {
            holder.menu_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
                    View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout,
                            view.findViewById(R.id.bottom_sheet));
                    bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            holder.itemView.performClick();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Rename To");
                            EditText editText = new EditText(context);
                            String path = mediaList.get(holder.getAbsoluteAdapterPosition()).getPath();
                            final File file = new File(path);

                            String mediaName = file.getName();
                            mediaName = mediaName.substring(0, mediaName.lastIndexOf("."));
                            editText.setText(mediaName);
                            alertDialog.setView(editText);
                            editText.requestFocus();
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Apply validation check
                                    if(TextUtils.isEmpty(editText.getText().toString()))
                                    {
                                        Toast.makeText(context,"Can't rename empty file",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String onlyPath = file.getParentFile().getAbsolutePath();
//                                    Uri contentUri=null;
//                                    if(mediaType.equals(MEDIA_TYPE_VIDEO))
//                                        contentUri= ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                                                Long.parseLong(mediaList.get(holder.getAbsoluteAdapterPosition()).getId()));
//                                    else if(mediaType.equals(MEDIA_TYPE_AUDIO))
//                                        contentUri=ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                                                Long.parseLong(mediaList.get(holder.getAbsoluteAdapterPosition()).getId()));
//                                    else
//                                        Toast.makeText(context.getApplicationContext(), "wrong media type!",Toast.LENGTH_SHORT).show();
//                                    if(contentUri!=null)
//                                    {
//                                        context.getContentResolver().update(contentUri,)
//
//                                    }
                                    //Log.i(TAG + " ###", "Original Path: " + onlyPath);
                                    String ext = file.getAbsolutePath();
                                    //Log.i(TAG + " ###", "ext: file.getAbsolutePath(): " + ext);
                                    ext = ext.substring(ext.lastIndexOf("."));
                                    //Log.i(TAG + " ###", "ext: file.getAbsolutePath(): " + ext);

                                    String newPath = onlyPath + "/" + editText.getText().toString().trim() + ext;
                                    //Log.i(TAG + " ###", "New Path: " + newPath);

                                    File newFile = new File(newPath);
                                    Log.i(TAG+" ###","renaming: in onClick(): newFile name: "+newFile.getName());
                                    boolean rename=false;
                                    try {
                                        rename = file.renameTo(newFile);
                                        //Log.i(TAG+" ###","file name after renaming: "+file.getName());
                                    } catch (Exception e) {
                                        Log.e(TAG + " ###", "Exception while renaming: " + e);
                                    }
                                    //Log.i(TAG + " ###", "rename: " + rename);
                                    if (rename) {
                                        ContentResolver resolver = context.getApplicationContext().getContentResolver();
                                        resolver.delete(MediaStore.Files.getContentUri("external"),
                                                MediaStore.MediaColumns.DATA + "=?", new String[]
                                                        {
                                                                file.getAbsolutePath()
                                                        });
                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        intent.setData(Uri.fromFile(newFile));
                                        context.getApplicationContext().sendBroadcast(intent);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "File Renamed", Toast.LENGTH_SHORT).show();
                                        //To show the instantaneous change in the name
                                        //Otherwise we have to close and reopen the app to see the change
                                        SystemClock.sleep(200);
                                        ((Activity) context).recreate();//automatically refreshes the activity

                                    } else {
                                        Toast.makeText(context, "Process Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //dismiss the dialog
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.create().show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view) {
                            Uri mUri=Uri.parse(mediaList.get(holder.getAbsoluteAdapterPosition()).getPath());
                            Intent shareIntent=new Intent(Intent.ACTION_SEND);
                            shareIntent.setType(mediaType+"/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM,mUri);
                            context.startActivity(Intent.createChooser(shareIntent,"Share media file via: "));
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
                            alertDialog.setTitle("Delete");
                            alertDialog.setMessage("Do you want to delete this file?");
                            alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri contentUri=null;
                                    if(mediaType.equals(MEDIA_TYPE_VIDEO))
                                        contentUri= ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                                Long.parseLong(mediaList.get(holder.getAbsoluteAdapterPosition()).getId()));
                                    else if(mediaType.equals(MEDIA_TYPE_AUDIO))
                                        contentUri=ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                Long.parseLong(mediaList.get(holder.getAbsoluteAdapterPosition()).getId()));
                                    else
                                        Toast.makeText(context.getApplicationContext(), "wrong media type!",Toast.LENGTH_SHORT).show();
                                    File file=new File(mediaList.get(holder.getAbsoluteAdapterPosition()).getPath());
                                    //Log.i(TAG+" ###",""+file.getName());
                                    boolean delete=file.delete();
                                    if(delete)
                                    {
//                                    RecoverableSecurityException recoverableSecurityException= null;
//                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                                        recoverableSecurityException = new RecoverableSecurityException();
//                                    }
                                        context.getContentResolver().delete(contentUri,null,null);
                                        mediaList.remove(holder.getAbsoluteAdapterPosition());
                                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                                        notifyItemRangeChanged(holder.getAbsoluteAdapterPosition(),mediaList.size());
                                        Toast.makeText(context,"File deleted successfully!",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(context,"Media file deletion failed!",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
                            alertDialog.setTitle("Properties");
                            String name="File: "+mediaList.get(holder.getAbsoluteAdapterPosition()).getDisplayName();
                            String path="Path: "+mediaList.get(holder.getAbsoluteAdapterPosition()).getPath();
                            int indexOfPath=path.lastIndexOf("/");
                            path="Path: "+path.substring(0,indexOfPath);
                            String size="Size: "+ Formatter
                                    .formatFileSize(context, Long.parseLong(mediaList.get(holder.getAbsoluteAdapterPosition()).getSize()));
                            String length="Length: "+timeConversion((long) milliSeconds);
                            String name_With_Format=mediaList.get(holder.getAbsoluteAdapterPosition()).getDisplayName();
                            int index=name_With_Format.lastIndexOf(".");
                            //Log.i(TAG+" ###","name: "+name+"\n   index:"+index+"\n  name_With_Format: "+name_With_Format);
                            String format="Format: "+name_With_Format.substring(index+1);
                            MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(mediaList.get(holder.getAbsoluteAdapterPosition()).getPath());
                            String height=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                            String width=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                            String resolution="Resolution: "+width+"x"+height;

                            alertDialog.setMessage(name+"\n\n"+path+"\n\n"+size+"\n\n"
                                    +length+"\n\n"+format+"\n\n"+resolution);
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetDialog.setContentView(bsView);
                    bottomSheetDialog.show();
                }
            });

        }
        else
        {
            holder.menu_more.setVisibility(View.GONE);
            holder.mediaName.setTextColor(Color.WHITE);
            holder.mediaSize.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(mediaType.equals(MEDIA_TYPE_VIDEO))
                {
                    if(AudioPlayerActivity.playerNotificationManager!=null)
                        AudioPlayerActivity.playerNotificationManager.setPlayer(null);
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("position", holder.getAbsoluteAdapterPosition());
                    intent.putExtra("media_title", mediaList.get(holder.getAbsoluteAdapterPosition()).getDisplayName());
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("mediaArrayList", mediaList);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    if(viewType==1)
                    {
                        ((Activity)context).finish();
                    }
                }
                else if(mediaType.equals(MEDIA_TYPE_AUDIO))
                {
                    Intent intent=new Intent(context, AudioPlayerActivity.class);
                    intent.putExtra("position", holder.getAbsoluteAdapterPosition());
                    intent.putExtra("media_title", mediaList.get(holder.getAbsoluteAdapterPosition()).getDisplayName());
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("mediaArrayList", mediaList);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    if(viewType==1)
                    {
                        ((Activity)context).finish();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, menu_more;
        TextView mediaName, mediaSize, mediaDuration;
        CardView thumbnail_card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail_card=itemView.findViewById(R.id.thumbnail_card);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            menu_more = itemView.findViewById(R.id.media_menu_more);
            mediaName = itemView.findViewById(R.id.media_name);
            mediaSize = itemView.findViewById(R.id.media_size);
            mediaDuration = itemView.findViewById(R.id.media_duration);
        }
    }

    public String timeConversion(long value) {
        String mediaTime;
        int duration = (int) value;
        int hrs = (duration / 3600000);
        int mns = (duration / 60000) % 60000;
        int scs = duration % 60000 / 1000;
        if (hrs > 0)
            mediaTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        else
            mediaTime = String.format("%02d:%02d", mns, scs);
        return mediaTime;
    }
    void updateMediaFiles(ArrayList<MediaFiles> files)
    {
        mediaList=new ArrayList<>();
        mediaList.addAll(files);
        notifyDataSetChanged();
    }
}
