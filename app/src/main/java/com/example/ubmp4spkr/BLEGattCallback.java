package com.example.ubmp4spkr;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.UUID;

public class BLEGattCallback extends BluetoothGattCallback {

    private MainActivity mainActivity;
    private WriteThread writeThread;

    protected BluetoothGatt bleGatt;
    protected BluetoothGattCharacteristic mainBLECharacteristic;

    private Integer previousValue = 0;
    private int timesReceived = 0;

    public BLEGattCallback(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Toast.makeText(com.example.ubmp4spkr.MainActivity.this, "Successfully connected", Toast.LENGTH_SHORT).show();
                Log.d("GattCallback", "Successfully connected");
                bleGatt = gatt;
                bleGatt.discoverServices();
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
        mainBLECharacteristic = gatt.getService(fFE0).getCharacteristic(fFE1);
        boolean canRead = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == BluetoothGattCharacteristic.PROPERTY_READ;
        boolean canWriteNR = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
        boolean supportsIndications = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
        boolean supportsNotifications = (mainBLECharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY;
        Log.d("GattCallback", "canRead = " + canRead + " canWriteNR = " + canWriteNR + " supportsIndications = " + supportsIndications + " supportsNotifications = " + supportsNotifications);
        if (canRead && canWriteNR && supportsNotifications) {
            //BluetoothGattDescriptor notificationsDescriptor = mainBLECharacteristic.getDescriptor(cCCD);
            //notificationsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //gatt.writeDescriptor(notificationsDescriptor);
            //gatt.setCharacteristicNotification(mainBLECharacteristic, true);
            mainActivity.launchPlay();
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
        Integer currentValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        boolean isThreadAlive = new Thread(writeThread).isAlive();
        boolean isPrevious = currentValue.equals(previousValue);
        if (!isThreadAlive && (!isPrevious || timesReceived > 5)) {
            writeThread = new WriteThread(currentValue, bleGatt, mainBLECharacteristic);
            new Thread(writeThread).start();
            Log.d("GattCallback", "timesReceived = " + timesReceived);
            timesReceived = 0;
        } else if (isPrevious && !isThreadAlive) {
            timesReceived++;
        }
        previousValue = currentValue;
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