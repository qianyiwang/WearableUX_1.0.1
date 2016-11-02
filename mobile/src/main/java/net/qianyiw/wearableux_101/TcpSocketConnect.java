package net.qianyiw.wearableux_101;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by QWANG97 on 4/1/2016.
 */
public class TcpSocketConnect {

    Context context;
    PrintStream out = null;
    Socket socket = null;
    String dstAddress;
    int dstPort;
    MyClientTask myClientTask;

    public TcpSocketConnect(String addr, String port, Context context)
    {
        this.context = context;

        dstAddress = addr;
        dstPort = Integer.parseInt(port);

        myClientTask = new MyClientTask(dstAddress, dstPort);
        myClientTask.execute();

    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(socket!=null){
                Toast.makeText(context, "PORT:"+dstPort+"CONNECTED", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, "CONNECTION FAILED", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                socket = new Socket(dstAddress, dstPort);
                Log.v("SOCKET", "CONNECTED");
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("SOCKET", "CONNECTED FAILED");
            }

            return null;
        }

    }
    public void writeSocket(String msg)
    {
        Log.v("write socket::", msg);
        try {
            if(socket!=null)
            {
                out = new PrintStream(socket.getOutputStream(), true);
                out.write((msg).getBytes());
            }
            else
            {
                Toast.makeText(context, "Please join the network", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.v("out error", e.toString());
            Toast.makeText(context, "Write socket error", Toast.LENGTH_SHORT).show();
        }

//        try {
//            if(socket!=null)
//            {
//                out.write((msg).getBytes());
//            }
//            else
//            {
//                Toast.makeText(context, "Please join the network", Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //out.close();
    }
}
