package com.example.ubmp4spkr;

import static com.example.ubmp4spkr.MainActivity.bleGatt;
import static com.example.ubmp4spkr.MainActivity.mainBLECharacteristic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.example.ubmp4spkr.MainActivity.*;

import java.util.UUID;

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
        UUID cCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor notificationsDescriptor = mainBLECharacteristic.getDescriptor(cCCD);
        notificationsDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        bleGatt.writeDescriptor(notificationsDescriptor);
        bleGatt.setCharacteristicNotification(mainBLECharacteristic, false);
    }

    public void play(View v) {
        ImageView playButton = (ImageView) v;
        playButton.setVisibility(View.INVISIBLE);
        playButton.setEnabled(false);
        findViewById(R.id.pauseButton).setEnabled(true);
        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
        UUID cCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor notificationsDescriptor = mainBLECharacteristic.getDescriptor(cCCD);
        notificationsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bleGatt.writeDescriptor(notificationsDescriptor);
        bleGatt.setCharacteristicNotification(mainBLECharacteristic, true);
    }
}