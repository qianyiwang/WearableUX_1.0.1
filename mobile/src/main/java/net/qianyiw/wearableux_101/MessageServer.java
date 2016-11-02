package net.qianyiw.wearableux_101;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

/**
 * Created by QWANG97 on 3/30/2016.
 */
public class MessageServer implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient myApiClient;
    private static final String PHONE_PATH = "/from-phone";
    Node mNode;
    Context mContext;
    ArrayList<Node> allNodes = new ArrayList<Node>();
    private static final long CONNECTION_TIMEOUT = 30;
    public MessageServer(Context context)
    {
        myApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.mContext = context;
    }

    public void sendMessage(String str) {

        if(!myApiClient.isConnected()||myApiClient.isConnecting()){
//            Toast.makeText(mContext, "re-connecting", 0).show();
            myApiClient.connect();
        }

        for (Node mNode: allNodes) {

            if (mNode != null && myApiClient != null) {
                Log.v("sendMessageTo", mNode.getDisplayName());

//            myApiClient.blockingConnect(CONNECTION_TIMEOUT, TimeUnit.SECONDS);

                Wearable.MessageApi.sendMessage(myApiClient,
                        mNode.getId(), PHONE_PATH, str.getBytes())
                        .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.v("status", "Failed message");
                                } else {
                                    Log.v("status", "Message succeeded");
                                }
                            }
                        });
            } else {
                Log.v("status", "node problem");
            }
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(myApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            if (node != null && node.isNearby()) {
                                mNode = node;
                                allNodes.add(mNode);// put all the nodes into an array
                                Log.v("Status", "Connected to " + mNode.getDisplayName());
//                                Log.v("All Nodes", String.valueOf(allNodes.get(0)));
                            } else {
                                Log.v("Status", "Not connected!");
                            }
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
