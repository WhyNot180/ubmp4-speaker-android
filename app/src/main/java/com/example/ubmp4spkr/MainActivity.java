package com.example.ubmp4spkr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {
    private final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private int ENABLE_BLUETOOTH_RESULT_CODE;
    private final int ENABLE_COARSE_LOCATION_REQUEST_CODE = 2;
    private final int ENABLE_FINE_LOCATION_REQUEST_CODE = 3;

    protected static boolean playIsInstantiated;
    private PlayActivity playActivity;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    protected static BLEScanCallback scanCallback;
    protected static BluetoothGatt bleGatt;
    protected static BLEGattCallback gattCallback;
    protected static BluetoothGattCharacteristic mainBLECharacteristic;

    public class BLEGattCallback extends BluetoothGattCallback {
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
                BluetoothGattDescriptor notificationsDescriptor = mainBLECharacteristic.getDescriptor(cCCD);
                notificationsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(notificationsDescriptor);
                gatt.setCharacteristicNotification(mainBLECharacteristic, true);
                launchPlay();
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
            if (playIsInstantiated) {
                Log.i("GattCallback", "Successfully notified of characteristic. Value = " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                PlayActivity.checkForValue(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            }
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

    public class BLEScanCallback extends ScanCallback{
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String name = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            Log.d("Scan Success", "BLE Device Found! Name: " + result.getDevice().getName() + " Address: " + result.getDevice().getAddress());
            //Toast.makeText(com.example.ubmp4spkr.MainActivity.this, "Name: " + result.getDevice().getName() + " Address: " + result.getDevice().getAddress() , Toast.LENGTH_SHORT).show();
            if (name != null) {
                if (name.equals("HC-08") && address.equals("94:E3:6D:7C:FA:15")) {
                    bluetoothLeScanner.stopScan(scanCallback);
                    isScanning = false;
                    Log.w("Connecting", "Connecting to: " + result.getDevice().getName() + " " + result.getDevice().getAddress());
                    result.getDevice().connectGatt(com.example.ubmp4spkr.MainActivity.this, false, gattCallback);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("Scan Success", "Scan Success Batch");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    }

    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        scanCallback = new BLEScanCallback();
        gattCallback = new BLEGattCallback();
        findViewById(R.id.scanButton).setOnClickListener(v -> {
            if (isScanning) {
                stopBLEScan();
            } else {
                startBLEScan();
            }
        });
        promptForPermissions();
        Log.d("LeApp", "Created");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LeApp", "OnActivity Result");
        if (resultCode == RESULT_CANCELED) {
            finishAffinity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void promptForBluetooth() {
        Log.d("LeApp", "Prompt for bluetooth");
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
            Log.d("LeApp", "Asked for bluetooth");
        }
    }

    private void promptForPermissions() {
        promptForBluetooth();
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ENABLE_FINE_LOCATION_REQUEST_CODE);
        //Toast.makeText(this, "Thanks for enabling location and Bluetooth", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST_CODE:
            case ENABLE_FINE_LOCATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    break;
                } else {
                    finishAffinity();
                }
                break;
        }
    }

    private void startBLEScan() {
        int fineLocation = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean isFineLocationPermissionGranted = fineLocation == PackageManager.PERMISSION_GRANTED;
        boolean isCoarseLocationPermissionGranted = coarseLocation == PackageManager.PERMISSION_GRANTED;
        boolean isLocationPermissionGranted = isFineLocationPermissionGranted && isCoarseLocationPermissionGranted;
        Log.d("locationPermissions", "All: " + isLocationPermissionGranted);
        Log.d("locationPermissions", "Coarse: " + isCoarseLocationPermissionGranted);
        Log.d("locationPermissions", "Fine: " + isFineLocationPermissionGranted);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted && !bluetoothAdapter.isEnabled()) {
            promptForPermissions();
        } else {
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
            isScanning = true;
        }
    }

    private void stopBLEScan() {
        int fineLocation = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean isFineLocationPermissionGranted = fineLocation == PackageManager.PERMISSION_GRANTED;
        boolean isCoarseLocationPermissionGranted = coarseLocation == PackageManager.PERMISSION_GRANTED;
        boolean isLocationPermissionGranted = isFineLocationPermissionGranted && isCoarseLocationPermissionGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted && !bluetoothAdapter.isEnabled()) {
            promptForPermissions();
        } else {
            bluetoothLeScanner.stopScan(scanCallback);
            isScanning = false;
        }
    }

    public BluetoothGattCharacteristic getMainBLECharacteristic() {
        return mainBLECharacteristic;
    }

    public BLEGattCallback getGattCallback() {
        return gattCallback;
    }

    public BluetoothGatt getBleGatt() {
        return bleGatt;
    }

    public BLEScanCallback getScanCallback() {
        return scanCallback;
    }

    public void launchPlay(){
        Intent i = new Intent(this, PlayActivity.class);
        startActivity(i);
    }
}