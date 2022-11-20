package com.example.ubmp4spkr;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class WriteThread implements Runnable{

    Integer currentValue;
    private BluetoothGatt bleGatt;
    private BluetoothGattCharacteristic mainBLECharacteristic;

    public WriteThread(Integer value, BluetoothGatt bleGatt, BluetoothGattCharacteristic mainBLECharacteristic) {
        this.currentValue = value;
        this.bleGatt = bleGatt;
        this.mainBLECharacteristic = mainBLECharacteristic;
    }

    @Override
    public void run() {
        checkForValue(currentValue);

    }

    private static final long[][] pitches = {{Notes.G.getPitchData(4), Notes.G.getPitchData(4), Notes.D.getPitchData(5), Notes.D.getPitchData(5), Notes.E.getPitchData(5), Notes.E.getPitchData(5), Notes.D.getPitchData(5), 0,                       Notes.C.getPitchData(5), Notes.C.getPitchData(5), Notes.B.getPitchData(4), Notes.B.getPitchData(4), Notes.A.getPitchData(4), Notes.A.getPitchData(4), Notes.G.getPitchData(4)},
            {0,                        Notes.D.getPitchData(4), 0,                       Notes.G.getPitchData(4), 0,                       Notes.G.getPitchData(4), 0,                       Notes.G.getPitchData(4), 0,                       Notes.F.getPitchData(4), 0,                       Notes.G.getPitchData(4), Notes.G.getPitchData(4), Notes.F.getPitchData(4), 0},
            {Notes.G.getPitchData(2),  Notes.B.getPitchData(3), Notes.B.getPitchData(2), Notes.D.getPitchData(4), Notes.C.getPitchData(3), Notes.E.getPitchData(4), Notes.B.getPitchData(2), Notes.D.getPitchData(4), Notes.A.getPitchData(2), Notes.C.getPitchData(4), Notes.G.getPitchData(2), Notes.D.getPitchData(4), Notes.D.getPitchData(2), Notes.A.getPitchData(3), Notes.G.getPitchData(2)}};

    private static final byte[][] rhythmEffectData = {{0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x19, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11},
            {0x19, 0x11, 0x19, 0x11, 0x19, 0x11, 0x19, 0x11, 0x19, 0x11, 0x19, 0x11, 0x11, 0x11, 0x19},
            {0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11}};

    // Whole, Dotted Half, Half, Dotted Quarter, Quarter, Dotted Eighth, Eighth, Sixteenth
    private static final byte[] rhythmLengths = {100, 75, 50, 38, 25, 19, 13, 6};
    private static final byte rhythmSilence = 1;
    private static int[] noteIndexes = { 0, 0, 0 };

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

    private void sendValue(byte value) {
        byte[] byteValue = {value};
        mainBLECharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mainBLECharacteristic.setValue(byteValue);
        bleGatt.writeCharacteristic(mainBLECharacteristic);
    }

    private void sendRhythm(Integer value) {
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

    private void sendRhythmEffectData(Integer value) {
        switch (value) {
            case 88:
                sendValue(rhythmEffectData[0][noteIndexes[0]]);
                break;
            case 89:
                sendValue(rhythmEffectData[1][noteIndexes[1]]);
                break;
            case 90:
                sendValue(rhythmEffectData[2][noteIndexes[2]]);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkForValue(Integer value) {
        switch (value) {
            case 97:
                sendValue((byte)(pitches[0][noteIndexes[0]] >> 8));
                break;
            case 98:
                sendValue((byte)(pitches[0][noteIndexes[0]] & 0xFF));
                if (noteIndexes[0] != 15) {
                    noteIndexes[0]++;
                }
                break;
            case 99:
                sendValue((byte)(pitches[1][noteIndexes[1]] >> 8));
                break;
            case 100:
                sendValue((byte)(pitches[1][noteIndexes[1]] & 0xFF));
                noteIndexes[1]++;
                break;
            case 101:
                sendValue((byte)(pitches[2][noteIndexes[2]] >> 8));
                break;
            case 102:
                sendValue((byte)(pitches[2][noteIndexes[2]] & 0xFF));
                noteIndexes[2]++;
                break;
            case 88:
            case 89:
            case 90:
                sendRhythmEffectData(value);
                break;
            case 1:
                sendValue((byte)1);
                Log.d("Note Processing", "sent 1");
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
            case 58:
                sendValue(rhythmSilence);
            default:
                Log.e("Note Processing", "VALUE IS NOT RECOGNIZED! " + value);
                break;
        }
    }
}
