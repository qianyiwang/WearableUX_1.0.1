package net.qianyiw.wearableux_101;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ADASAlertMainActivity extends AppCompatActivity{

    private BluetoothService bluetoothServiceReferenece;
    private int REQUEST_CODE = 101;
    private int NOTIFICATION_ID = 102;
    private boolean isBound, adas_demo_on;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private static final String SETTING_STATUS_FILE = "SettingFile";
//    public static final String DEVICE_ADDRESS = "5C:F3:70:6C:7F:4E";
    public static final String DEVICE_ADDRESS = "5C:F3:70:6C:7F:7B";
    private static final String WEAR_PATH = "/from-watch";

    BluetoothAdapter btAdapter;
    ArrayAdapter<String> listAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    ListView deviceList;
    Button vibeSettings;
    Button testButton;
    TextView terminal;

    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adasalert_main);
        testButton = (Button) findViewById(R.id.test_button);
        vibeSettings = (Button) findViewById(R.id.search_button);
        terminal = (TextView) findViewById(R.id.terminal);
        terminal.setGravity(Gravity.BOTTOM);
        prefs = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE).edit();
        //Use Intent to start Service
        Log.i("Service", "Service Starting...");
        int adas_demo_extra = getIntent().getIntExtra("adas_demo", 2);
        Log.v("adas extra", String.valueOf(adas_demo_extra));
        if(adas_demo_extra==1) {
            adas_demo_on = true;
            editor.putBoolean("adas_demo_mode",true).commit();
        }
        else if(adas_demo_extra==0) {
            adas_demo_on = false;
            editor.putBoolean("adas_demo_mode",false).commit();
        }
        else
        {
            adas_demo_on = prefs.getBoolean("adas_demo_mode", false);
        }
        Intent intent = new Intent(this, BluetoothService.class);
        intent.putExtra("adas_demo_mode",adas_demo_on);
        bluetoothServiceReferenece = new BluetoothService();
        startService(intent);

        //Show notification while Service is running
        sendNotification();

        deviceList = (ListView)findViewById(R.id.device_list);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothServiceReferenece.disconnectAll();

                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                if (listAdapter.getItem(position).contains("PAIRED")) {
                    BluetoothDevice selectedDevice = devices.get(position);
                    bluetoothServiceReferenece.connectDevice(selectedDevice);

//                    Intent intent1 = new Intent(getBaseContext(), PhoneMainActivity.class);
                    editor.putInt("bluetooth_status", 1).commit();
//                    startActivity(intent1);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_LONG).show();
                }

            }
        });

        final SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        init();

        if(btAdapter==null){
            Toast.makeText(getApplicationContext(),"No Bluetooth Detected", Toast.LENGTH_LONG).show();
        }
        else{
            if(!btAdapter.isEnabled()){
                turnOnBT();
            }

            getPairedDevices();
            startDiscovery();
        }

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothServiceReferenece.testNotification();
            }
        });

    }

    public void init(){
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        deviceList.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        devices = new ArrayList<>();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String pairedString = "";

                    for(int a = 0; a < pairedDevices.size(); a++){
                        //Check Here
                        if (device.getName() != null) {
                            if (device.getName().equals(pairedDevices.get(a))) {
                                //append
                                pairedString = "(PAIRED)";
                                break;
                            }
                        }
                    }

                    if(device.getName() != null) {
                        listAdapter.add(device.getName() + pairedString + "\n" + device.getAddress());
                    }
                    if(device.getAddress().equals(DEVICE_ADDRESS))
                    {
                        Toast.makeText(getApplicationContext(), "Device found!!!", Toast.LENGTH_SHORT).show();
                        btAdapter.cancelDiscovery();
                        bluetoothServiceReferenece.connectDevice(device);
//                        Intent intent1 = new Intent(getBaseContext(), PhoneMainActivity_v3.class);
                        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putInt("bluetooth_status", 1).commit();
//                        startActivity(intent1);
                        finish();
                    }
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(btAdapter.getState()==btAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }

            }
        };
        //registerReceiver(receiver, filter);
    }

    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    private void turnOnBT() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void getPairedDevices() {
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private ServiceConnection bluetoothServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("Service", "Bluetooth Service Is Bound");
            bluetoothServiceReferenece = ((BluetoothService.MyLocalBinder) service).getService();
            isBound = true;
            bluetoothServiceReferenece.updateBSPatterns();
            bluetoothServiceReferenece.updateSPPatterns();
            bluetoothServiceReferenece.updateHWPatterns();
            bluetoothServiceReferenece.updateSCPatterns();
            bluetoothServiceReferenece.updateLKPatterns();
            bluetoothServiceReferenece.updateTNPatterns();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("Service", "Bluetooth Service is Disconnected");
            bluetoothServiceReferenece = null;
            isBound = false;
        }
    };

    //Method to Unbind from the service
    private void doUnbindService() {
        unbindService(bluetoothServiceConnection);
        isBound = false;
    }

    //Method to bind to the service
    private void doBindToService() {
        if(!isBound){
            Intent bindIntent = new Intent(this, BluetoothService.class);
            isBound = bindService(bindIntent, bluetoothServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Service", "MainActivity - onStart - binding to Service...");
        doBindToService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
        Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Service", "MainActivity - onStop - unbinding...");
//        doUnbindService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Change the back button to pause activity instead of stopping
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Service", "Destroying Activity...");
        Log.i("Service", "Activitiy is finishing - Stopping Serivce...");
        //Stop the service
//        Intent intentStopService = new Intent(this, BluetoothService.class);
//        stopService(intentStopService);

    }


    //Method to Create and Ongoing Notification of Running Service
    private void sendNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Bluetooth Service Running")
                .setTicker("ADAS - Bluetooth Connected")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent startIntent = new Intent(this, ADASAlertMainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE, startIntent,0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
