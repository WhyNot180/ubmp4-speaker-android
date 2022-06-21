package com.example.ubmp4spkr;

import static com.example.ubmp4spkr.MainActivity.bleGatt;
import static com.example.ubmp4spkr.MainActivity.mainBLECharacteristic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.example.ubmp4spkr.MainActivity.*;

public class PlayActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        MainActivity.playIsInstantiated = true;
    }

    public void pause(View v) {
        ImageView pauseButton = (ImageView) v;
        pauseButton.setVisibility(View.INVISIBLE);
        pauseButton.setEnabled(false);
        findViewById(R.id.playButton).setEnabled(true);
        findViewById(R.id.playButton).setVisibility(View.VISIBLE);
    }

    public void play(View v) {
        ImageView playButton = (ImageView) v;
        playButton.setVisibility(View.INVISIBLE);
        playButton.setEnabled(false);
        findViewById(R.id.pauseButton).setEnabled(true);
        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkForValue(Integer value) {
        mainBLECharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        switch (value) {
            case 1:
                mainBLECharacteristic.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                bleGatt.writeCharacteristic(mainBLECharacteristic);
                break;
            case 55:
                mainBLECharacteristic.setValue();
                bleGatt.writeCharacteristic(mainBLECharacteristic);
                break;
        }
    }
}