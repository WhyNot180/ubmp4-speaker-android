package com.example.ubmp4spkr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.example.ubmp4spkr.MainActivity.*;

public class PlayActivity extends AppCompatActivity {

    private BluetoothGattCharacteristic mainBLECharacteristic;
    private BLEGattCallback gattCallback;
    private BluetoothGatt bleGatt;
    private BLEScanCallback scanCallback;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        MainActivity mainActivity = new MainActivity();
        mainBLECharacteristic = mainActivity.getMainBLECharacteristic();
        gattCallback = mainActivity.getGattCallback();
        bleGatt = mainActivity.getBleGatt();
        scanCallback = mainActivity.getScanCallback();

        mainBLECharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        byte[] lol = {0x01};
        mainBLECharacteristic.setValue(lol);
        bleGatt.writeCharacteristic(mainBLECharacteristic);
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
}