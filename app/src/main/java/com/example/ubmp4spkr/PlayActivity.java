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

    public class BLEGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //Toast.makeText(com.example.ubmp4spkr.MainActivity.this, "Successfully connected", Toast.LENGTH_SHORT).show();
                    Log.d("GattCallback", "Successfully connected");
                    //TODO: add more connection stuff here
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //Toast.makeText(com.example.ubmp4spkr.MainActivity.this, "Successfully disconnected", Toast.LENGTH_SHORT).show();
                    Log.d("GattCallback", "Successfully disconnected");
                    gatt.close();
                }
            } else {
                //Toast.makeText(com.example.ubmp4spkr.MainActivity.this, "Error Encountered: " + status, Toast.LENGTH_LONG).show();
                Log.e("GattCallback", "Error " + status + " encountered! Disconnecting...");
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("GattCallback", "Number of services: " + gatt.getServices().size());
            UUID fFE0 = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
            UUID fFE1 = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
            UUID cCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
            Log.d("GattCallback", "Contains: " + gatt.getServices().toString());
            Log.d("GattCallback", "Successfully got FFE1: " + gatt.getService(fFE0).getCharacteristic(fFE1).getUuid());
            boolean canRead = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == BluetoothGattCharacteristic.PROPERTY_READ;
            boolean canWriteNR = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
            boolean supportsIndications = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
            boolean supportsNotifications = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY;
            Log.d("GattCallback", "canRead = " + canRead + " canWriteNR = " + canWriteNR + " supportsIndications = " + supportsIndications + " supportsNotifications = " + supportsNotifications);
            if (canRead && canWriteNR && supportsNotifications) {
                BluetoothGattDescriptor notificationsDescriptor = mainBLECharacteristic.getDescriptor(cCCD);
                notificationsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(notificationsDescriptor);
                //gatt.setCharacteristicNotification(mainBLECharacteristic, true);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("GattCallback", "Successfully read characteristic. Value = " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            } else if (status == BluetoothGatt.GATT_READ_NOT_PERMITTED) {
                Log.e("GattCallback", "Read Not Permitted!");
            } else {
                Log.e("GattCallback", "Read failed: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("GattCallback", "Successfully notified of characteristic. Value = " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            PlayActivity.checkForValue(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("GattCallback", "Successfully enabled notifications");
            } else {
                Log.e("GattCallback", "Failed to enable notifications: " + status);
            }
        }
    }

    private static final long[][] pitches = {{Notes.G.getPitchData(4), Notes.G.getPitchData(4), Notes.D.getPitchData(5)},
                                            {0,                        Notes.D.getPitchData(4), 0},
                                            {Notes.G.getPitchData(2),  Notes.B.getPitchData(3), Notes.B.getPitchData(2)}};

    private static final int[][] rhythmEffectData = {{0x44, 0x44, 0x44},
                                                    {0x54, 0x44, 0x54},
                                                    {0x44, 0x44, 0x44}};

    private static int noteIndex = 0;

    enum Notes {
        C (2935780),
        Cs (2771363),
        D (2615804),
        Ds (2467866),
        E (2330097),
        F (2198809),
        Fs (2076125),
        G (1959184),
        Gs (1848998),
        A (1745455),
        As (1647220),
        B (1554908);

        private final long lowerNotePeriod;

        Notes(long lowerNotePeriod) {
            this.lowerNotePeriod = lowerNotePeriod;
        }

        long getPitchData(long something) {
            if (something > 6) {
                Log.e("Note Processing", "WARNING: OCTAVE IS TOO HIGH");
            }
            return this.lowerNotePeriod / (1 << something) / 1000;
        }
    }

    // Whole, Dotted Half, Half, Dotted Quarter, Quarter, Dotted Eighth, Eighth, Sixteenth
    private static final int[] rhythmLengths = {100, 75, 50, 38, 25, 19, 13, 6};
    private static final byte[] rhythmSilence = {10};

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

    private static void sendValue(int value) {
        mainBLECharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mainBLECharacteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        bleGatt.writeCharacteristic(mainBLECharacteristic);
    }

    private static void sendRhythm(Integer value) {
        switch (value) {
            case 55:
                sendValue(rhythmLengths[0]);
                break;
            case 54:
                sendValue(rhythmLengths[1]);
                break;
            case 53:
                sendValue(rhythmLengths[2]);
                break;
            case 52:
                sendValue(rhythmLengths[3]);
                break;
            case 51:
                sendValue(rhythmLengths[4]);
                break;
            case 50:
                sendValue(rhythmLengths[5]);
                break;
            case 49:
                sendValue(rhythmLengths[6]);
                break;
            case 48:
                sendValue(rhythmLengths[7]);
                break;
            default:
                Log.e("Note Processing", "RHYTHM NOT FOUND!");
                break;
        }
    }

    private static void sendRhythmEffectData(Integer value) {
        switch (value) {
            case 88:
                sendValue(rhythmEffectData[0][noteIndex]);
                break;
            case 89:
                sendValue(rhythmEffectData[1][noteIndex]);
                break;
            case 90:
                sendValue(rhythmEffectData[2][noteIndex]);
                noteIndex++;
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkForValue(Integer value) {
        switch (value) {
            case 97:
                sendValue((int)(pitches[0][noteIndex] >> 8));
                break;
            case 98:
                sendValue((int)(pitches[0][noteIndex] & 0xFF));
                break;
            case 99:
                sendValue((int)(pitches[1][noteIndex] >> 8));
                break;
            case 100:
                sendValue((int)(pitches[1][noteIndex] & 0xFF));
                break;
            case 101:
                sendValue((int)(pitches[2][noteIndex] >> 8));
                break;
            case 102:
                sendValue((int)(pitches[2][noteIndex] & 0xFF));
                break;
            case 88:
            case 89:
            case 90:
                sendRhythmEffectData(value);
                break;
            case 1:
                sendValue(1);
                break;
            case 55:
            case 54:
            case 53:
            case 52:
            case 51:
            case 50:
            case 49:
            case 48:
                sendRhythm(value);
                break;
            default:
                Log.e("Note Processing", "VALUE IS NOT RECOGNIZED!");
                break;
        }
    }
}