package net.qianyiw.wearableux_101;

/**
 * Created by Qianyi on 6/7/2016.
 */
import android.util.Log;

public class CommandItem {
    private static final String TAG = "COMMAND_ITEM";
    byte[] data;
    int advTimeout;

    public static final int NCONTROLS = 3;

    CommandItem(byte newZone, int[] newPosition, int[] newValue, int newAdvTimeout)
    {
        data = new byte[25];
        data[0] = 0x08;
        data[1] = 00;
        data[2] = (byte)0xF0;
        data[3] = (byte)0x3D;

        long vehicle_id = 6;
        //long vehicle_id = newSmartModule.vehicle_id;
        for (int ii=3; ii>=0; ii--) {
            byte temp = (byte)(vehicle_id&0xFF);
            data[4+ii] = temp;
            vehicle_id >>= 8;
        }

		/*
        for (int ii=0; ii<newSmartModule.familyName.length(); ii++) {
            data[8+ii] = (byte)newSmartModule.familyName.charAt(ii);
        }
        for (int ii=newSmartModule.familyName.length(); ii<10; ii++) {
            data[8+ii] = (byte)0;
        }
		*/
        String familyName = "SEAT";
        for (int ii=0; ii<familyName.length(); ii++) {
            data[8+ii] = (byte)familyName.charAt(ii);
        }
        for (int ii=familyName.length(); ii<10; ii++) {
            data[8+ii] = (byte)0;
        }

        data[18] = newZone;

        for (int ii=0; ii<3; ii++) {
            data[19+ii*2] = (byte) newPosition[ii];
            data[20+ii*2] = (byte) newValue[ii];
        }

//        int ii=0;
//        data[19+ii*2] = (byte) newPosition[ii];
//        data[20+ii*2] = (byte) newValue[ii];

        advTimeout = newAdvTimeout;
        Log.w(TAG, "Command Added " + newZone + " " + newPosition[0] + " " + newValue[0] + " " + newPosition[1] + " " + newValue[1] + " " + newPosition[2] + " " + newValue[2] + " " + newAdvTimeout);
        //Log.w(TAG, "Command Added " + newZone + " " + newPosition[0] + " " + newValue[0]  + newAdvTimeout);
    }

    CommandItem(byte[] newData, int newAdvTimeout)
    {
        data = new byte[newData.length];

        for (int ii=0; ii<newData.length; ii++) {
            data[ii] = newData[ii];
        }
        advTimeout = newAdvTimeout;
        Log.w(TAG, "Command Added: data0= " + newData[0] + " " + newAdvTimeout);
    }
}
