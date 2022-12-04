package com.example.ubmp4spkr;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.DeadSystemException;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.RequiresApi;

public class WriteThread extends Thread{

    Integer currentValue;
    private BluetoothGatt bleGatt;
    private BluetoothGattCharacteristic mainBLECharacteristic;

    private static final long[][] rhythm = {{4, 4, 4, 4, 4, 4, 8, 4, 4, 8, 4, 4, 8, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 16},
            {16,0, 0, 0, 16,0, 0, 16,0, 0, 16,0, 0, 16,0, 0, 0, 16,0, 0, 0, 8, 0, 8, 0, 16},
            {16,0, 0 ,0, 16,0, 0, 16,0, 0, 16,0, 0, 16,0, 0, 0, 16,0, 0, 0, 0, 0, 0, 0, 0}};

    private static final long[][] pitches = {{Notes.E.getPitchData(4), Notes.D.getPitchData(4), Notes.C.getPitchData(4), Notes.D.getPitchData(4), Notes.E.getPitchData(4), Notes.E.getPitchData(4), Notes.E.getPitchData(4), Notes.D.getPitchData(4), Notes.D.getPitchData(4), Notes.D.getPitchData(4), Notes.E.getPitchData(4), Notes.G.getPitchData(4), Notes.G.getPitchData(4), Notes.E.getPitchData(4), Notes.D.getPitchData(4), Notes.C.getPitchData(4), Notes.D.getPitchData(4), Notes.E.getPitchData(4), Notes.E.getPitchData(4), Notes.E.getPitchData(4), Notes.E.getPitchData(4), Notes.D.getPitchData(4), Notes.D.getPitchData(4), Notes.E.getPitchData(4), Notes.D.getPitchData(4), Notes.C.getPitchData(4)},
            {Notes.E.getPitchData(3), Notes.E.getPitchData(3), Notes.F.getPitchData(3), Notes.E.getPitchData(3), Notes.E.getPitchData(3), Notes.E.getPitchData(3), Notes.G.getPitchData(3), Notes.G.getPitchData(2), Notes.C.getPitchData(2)},
            {Notes.C.getPitchData(3), Notes.C.getPitchData(3), Notes.B.getPitchData(2), Notes.C.getPitchData(3), Notes.C.getPitchData(3), Notes.C.getPitchData(3)}};
    private static final byte[][] effects = {{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
            {4, 4, 4, 4, 4, 4, 4, 4, 4},
            {4, 4, 4, 4, 4, 4}};
    // ((BPM * 1000)/60) / BASE
    // for base: 1 = 1/16, 1.5 = 3/32, 2 = 1/8, 3 = 3/16 etc...
    private static final long sixteenthNoteDuration = ((120 * 1000)/60) / 4;

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

        long getPitchData(long octave) {
            if (octave > 6) {
                Log.e("Note Processing", "WARNING: OCTAVE IS TOO HIGH");
            }
            return this.lowerNotePeriod / (1L << octave) / 1000;
        }
    }

    public WriteThread(BluetoothGatt bleGatt, BluetoothGattCharacteristic mainBLECharacteristic) {
        this.bleGatt = bleGatt;
        this.mainBLECharacteristic = mainBLECharacteristic;
    }

    @Override
    public void run() {
        //TODO: add more rhythm stuff
        int[] pitchCounters = {0, 0, 0};
        for(int i = 0; i < rhythm.length; i++) {
            long minRhythm = Math.min(rhythm[0][i], Math.min(rhythm[1][i], rhythm[2][i]));
            if ((rhythm[0][i] != 0) || pitchCounters[0] != pitches[0].length) {
                pitchCounters[0]++;
                sendNotes(0, pitchCounters[0]);
            }
            if ((rhythm[1][i] != 0) || pitchCounters[1] != pitches[1].length) {
                pitchCounters[1]++;
                sendNotes(1, pitchCounters[1]);
            }
            if ((rhythm[2][i] != 0) || pitchCounters[2] != pitches[2].length) {
                pitchCounters[2]++;
                sendNotes(2, pitchCounters[2]);
            }
            try {
                sleep(minRhythm * sixteenthNoteDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    private void sendValue(byte value) {
        byte[] byteValue = {value};
        mainBLECharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mainBLECharacteristic.setValue(byteValue);
        bleGatt.writeCharacteristic(mainBLECharacteristic);
    }

    private void sendNotes(int channel, int noteIndex) {
        sendValue((byte)channel);
        while (currentValue != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendValue(effects[channel][noteIndex]);
        while (currentValue != 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte highPitch = (byte) ((byte) (pitches[channel][noteIndex] >> 8));
        sendValue(highPitch);
        while (currentValue != 2) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte lowPitch = (byte) ((byte) (pitches[channel][noteIndex] & 0x00FF));
        sendValue(lowPitch);
    }

    public void setCurrentValue(Integer value) {
        this.currentValue = value;
    }
}
