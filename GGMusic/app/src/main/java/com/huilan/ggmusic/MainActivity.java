package com.huilan.ggmusic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ContentResolver mContentResolver;
    private ListView mPlaylist;
    private MediaCursorAdapter mMediaCurSorAdapter;

    private BottomNavigationView navigation;
    private TextView tvBottomTitle;
    private TextView tvBottomArtist;
    private ImageView ivAlbumThumbnail;

    private MediaPlayer mMediaPlayer = null;
    Boolean play = false;


    private final String SELECTION =
            MediaStore.Audio.Media.IS_MUSIC + " = ? " + " AND " +
                    MediaStore.Audio.Media.MIME_TYPE + " LIKE ? ";

    private final String[] SELECTION_ARGS = {
            Integer.toString(1),
            "audio/mpeg"
    };

    public static final  String DATA_URI = "com.huilan.ggmusic.DATA_URI";
    public static final  String TITLE = "com.huilan.ggmusic.TITLE";
    public static final  String ARTIST = "com.huilan.ggmusic.ARTIST";


    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlaylist = findViewById(R.id.lv_playlist);

        mContentResolver = getContentResolver();
        mMediaCurSorAdapter = new MediaCursorAdapter(MainActivity.this);
        mPlaylist.setAdapter(mMediaCurSorAdapter);
        mPlaylist.setOnItemClickListener(itemClickListener);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } else {
            initPlaylist();
        }

        navigation = findViewById(R.id.navigation);
        LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_media_toolbar, navigation, true);

        ImageView ivPlay = navigation.findViewById(R.id.iv_play);
        tvBottomTitle = navigation.findViewById(R.id.tv_bottom_title);
        tvBottomArtist = navigation.findViewById(R.id.tv_bottom_artist);
        ivAlbumThumbnail = navigation.findViewById(R.id.iv_thumbnail);

        if (ivPlay != null) {
            ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (play){
                        ivPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                        mMediaPlayer.stop();
                    }else {
                        ivPlay.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                        try {
                            mMediaPlayer.prepare();
                            mMediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    play = !play;
                }
            });
        }
        navigation.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initPlaylist();
                }
                break;
            default:
                break;
        }
    }

    private void initPlaylist() {
        Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                SELECTION,
                SELECTION_ARGS,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );

        if (cursor!=null){
            System.out.println(cursor.getCount());
            mMediaCurSorAdapter.swapCursor(cursor);
            mMediaCurSorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    @Override
    protected void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {

    }

    private ListView.OnItemClickListener itemClickListener
            = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = mMediaCurSorAdapter.getCursor();
            if (cursor != null && cursor.moveToPosition(position)) {
                int titleIndex = cursor.getColumnIndex(
                        MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(
                        MediaStore.Audio.Media.ARTIST);
                int albumIdIndex = cursor.getColumnIndex(
                        MediaStore.Audio.Media.ALBUM_ID);
                int dataIndex = cursor.getColumnIndex(
                        MediaStore.Audio.Media.DATA);

                String title = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                Long albumId = cursor.getLong(albumIdIndex);
                String data = cursor.getString(dataIndex);

                Uri dataUri = Uri.parse(data);
                System.out.println(dataUri);

                Intent serviceIntent = new Intent(MainActivity.this,MusicService.class);
                serviceIntent.putExtra(MainActivity.DATA_URI,data);
                serviceIntent.putExtra(MainActivity.TITLE,title);
                serviceIntent.putExtra(MainActivity.ARTIST,artist);
                startService(serviceIntent);

                if (mMediaPlayer != null) {
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(MainActivity.this, dataUri);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                        play = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                navigation.setVisibility(View.VISIBLE);
                if (tvBottomTitle != null) {
                    tvBottomTitle.setText(title);
                }
                if (tvBottomArtist != null) {
                    tvBottomArtist.setText(artist);
                }

                Uri albumUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        albumId);

                Cursor albumCursor = mContentResolver.query(albumUri, null, null, null, null);

                if (albumCursor != null && albumCursor.getCount() >0) {
                    albumCursor.moveToFirst();
                    int albumArtIndex = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                    String albumArt = albumCursor.getString(albumArtIndex);
                    Glide.with(MainActivity.this)
                            .load(albumArt)
                            .into(ivAlbumThumbnail);
                    albumCursor.close();
                }

            }
        }
    };

}