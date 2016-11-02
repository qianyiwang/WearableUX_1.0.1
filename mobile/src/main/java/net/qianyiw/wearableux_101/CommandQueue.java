package net.qianyiw.wearableux_101;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Pietro on 11/8/2015.
 */
public class CommandQueue {
    private static CommandQueue ourInstance = new CommandQueue();

    public static CommandQueue getInstance() {
        return ourInstance;
    }

    private CommandQueue() {
    }

    Context mContext = null;

    static ArrayList<CommandItem> listCommands = new ArrayList<CommandItem>();
    static private BluetoothAdapter mBluetoothAdapter;
    static private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    static private AdvertiseData mAdvertiseData;
    static private AdvertiseSettings mAdvertiseSettings;
    static Handler mHandlerTimeout;
    private static final int ADVERTISING_TIMEOUT = 300;
    private static final int SHORT_ADVERTISING_TIMEOUT = 10;
    static private boolean initializedSuccessfully = false;
    static public boolean advertisingOn = false;

    static private final String TAG = "COMMAND_QUEUE";

    boolean initialize(Context context) {
        if (mContext != null)   // Check if already initialized
            return initializedSuccessfully;

        mContext = context;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter != null) {
                mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                if (mBluetoothLeAdvertiser != null) {
                    setAdvertiseSettings();
                    mHandlerTimeout = new Handler();
                    initializedSuccessfully = true;
                    return true;
                }
            }
        }

        initializedSuccessfully = false;
        return false;
    }

    protected AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "onStartFailure " + errorCode);
            int mErrorCode = errorCode;
        }

        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.w(TAG, "onStartSuccess");
        }
    };

    Runnable timeOut = new Runnable() {
        @Override
        public void run() {
            if (listCommands.isEmpty()) {
                stopAdvertising();
                advertisingOn = false;
            }
            else {
                CommandItem command = listCommands.get(0);
                sendCommand(command);
                listCommands.remove(0);
            }
        }
    };

    public void addCommand(byte accZone, int[] position, int[] value) {
        if (listCommands.isEmpty()) {
            sendCommand(accZone, position, value, ADVERTISING_TIMEOUT);
/*
            CommandItem command = new CommandItem(smartModule, accZone, position, value, ADVERTISING_TIMEOUT);
            listCommands.add(command);
            command = listCommands.get(0);
            sendCommand(command);
            listCommands.remove(0);
*/
        }
        else {
            CommandItem command = new CommandItem(accZone, position, value, ADVERTISING_TIMEOUT);
            listCommands.add(command);
        }
        CommandItem commandWrapper = new CommandItem((byte)0, position, value, SHORT_ADVERTISING_TIMEOUT);
        listCommands.add(commandWrapper);
    }

    public void addCommand(byte[] data) {
        if (listCommands.isEmpty()) {
            sendCommand(data, ADVERTISING_TIMEOUT);
        }
        else {
            CommandItem command = new CommandItem(data, ADVERTISING_TIMEOUT);
            listCommands.add(command);
        }
        byte[] dataWrapper = new byte[2];
        dataWrapper[0] = dataWrapper[1] = 0;
        CommandItem commandWrapper = new CommandItem(dataWrapper, SHORT_ADVERTISING_TIMEOUT);
        listCommands.add(commandWrapper);
    }

    private void sendCommand(byte accZone, int[] position, int[] value, int advTimeout) {
        //Log.w(TAG, "Command Sent " + accZone + " " + position[0] + " " + value[0] + " " + position[1] + " " + value[1] + " " + position[2] + " " + value[2] + " " + advTimeout);
        advertisingOn = true;
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        //mHandlerTimeout.removeCallbacks(timeOut);
        mAdvertiseData = setAdvertiseData(accZone, position, value);
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
        mHandlerTimeout.postDelayed(timeOut, advTimeout);
    }

    private void sendCommand(byte[] data, int advTimeout) {
        advertisingOn = true;
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        //mHandlerTimeout.removeCallbacks(timeOut);
        mAdvertiseData = setAdvertiseData(data);
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
        mHandlerTimeout.postDelayed(timeOut, advTimeout);
    }

    private void sendCommand(CommandItem command) {
        //sendCommand(command.smartModule, command.accZone, command.position, command.value, command.advTimeout);
        sendCommand(command.data, command.advTimeout);
    }

    public void stopAdvertising() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        advertisingOn = false;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected AdvertiseData setAdvertiseData(byte accZone, int[] position, int[] value) {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        int manufacturerID = 0x0008;
        mBuilder.addManufacturerData(manufacturerID, mManufacturerData.array());
        mManufacturerData.put(0, (byte) 0xF0);
        mManufacturerData.put(1, (byte)0x3D);

        //long vehicle_id = smartModule.vehicle_id;
        long vehicle_id = 6; // or 6 ...
        for (int ii=3; ii>=0; ii--) {
            byte temp = (byte)(vehicle_id&0xFF);
            mManufacturerData.put(2+ii, temp);
            vehicle_id >>= 8;
        }

        /*
        for (int ii=0; ii<smartModule.familyName.length(); ii++) {
            mManufacturerData.put(6+ii, (byte)smartModule.familyName.charAt(ii));
        }
        for (int ii=smartModule.familyName.length(); ii<10; ii++) {
            mManufacturerData.put(6+ii, (byte)0);
        }
        */
        String familyName = "SEAT";
        for (int ii=0; ii<familyName.length(); ii++) {
            mManufacturerData.put(6+ii, (byte)familyName.charAt(ii));
        }
        for (int ii=familyName.length(); ii<10; ii++) {
            mManufacturerData.put(6+ii, (byte)0);
        }

        mManufacturerData.put(16, accZone);

        for (int ii=0; ii<3; ii++) {
            mManufacturerData.put(17+ii*2, (byte) position[ii]);
            mManufacturerData.put(18+ii*2, (byte) value[ii]);
        }
//        int ii=0;
//        mManufacturerData.put(17+ii*2, (byte) position[ii]);
//        mManufacturerData.put(18+ii*2, (byte) value[ii]);
        String v = bytesToHex(mManufacturerData.array());
        Log.w(TAG, "MANUFACTURER_DATA " + v);
        mBuilder.setIncludeDeviceName(false);
        //mAdvertiseData = mBuilder.build();
        return mBuilder.build();
    }

    protected AdvertiseData setAdvertiseData(byte[] data) {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(data.length-2);
        int manufacturerID = data[0]+(data[1]<<8);
        mBuilder.addManufacturerData(manufacturerID, mManufacturerData.array());
        for (int ii=0; ii<data.length-2; ii++) {
            mManufacturerData.put(ii, (byte) data[ii+2]);
        }
        String v = bytesToHex(mManufacturerData.array());
        Log.w(TAG, "MANUFACTURER_DATA " + v);
        mBuilder.setIncludeDeviceName(false);
        return mBuilder.build();
    }

    protected void setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();
        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mBuilder.setConnectable(false);
        mBuilder.setTimeout(ADVERTISING_TIMEOUT-10);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        mAdvertiseSettings = mBuilder.build();
    }
}
