package com.example.ubmp4spkr;

import static com.example.ubmp4spkr.MainActivity.gattCallback;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class PlayActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
    }

    public void pause(View v) {
        ImageView pauseButton = (ImageView) v;
        pauseButton.setVisibility(View.INVISIBLE);
        pauseButton.setEnabled(false);
        findViewById(R.id.playButton).setEnabled(true);
        findViewById(R.id.playButton).setVisibility(View.VISIBLE);
        gattCallback.threadWait();

    }

    public void play(View v) {
        ImageView playButton = (ImageView) v;
        playButton.setVisibility(View.INVISIBLE);
        playButton.setEnabled(false);
        findViewById(R.id.pauseButton).setEnabled(true);
        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
        gattCallback.threadNotify();
    }
}