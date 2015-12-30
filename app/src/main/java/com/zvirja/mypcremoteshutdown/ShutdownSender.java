package com.zvirja.mypcremoteshutdown;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Zvirja on 9/18/2014.
 */
public class ShutdownSender extends AsyncTask<SendInfo,SendInfo,Integer> {
    private TextView resultTextView;
    private Context context;

    private static final int ERROR_CONNECTION_NOT_PRESENT = -1;
    private static final int ERROR_UNABLE_RESOLVE_ADDR = -2;
    private static final int ERROR_EXCEPTION_DURING_SENDING = -3;
    private static final int ERROR_EXCEPTION_DURING_CONNECTIVITY_CHECK = -4;


    private boolean connectionPresent;
    private int resultAction = 0;
    private String exception;



    private InetAddress targetAddress;

    public ShutdownSender(Context context, TextView resultTw) {
        this.context = context;
        this.resultTextView = resultTw;
        this.connectionPresent = false;
    }

    @Override
    protected Integer doInBackground(SendInfo... infos) {
        if(this.resultAction != 0)
        {
            return 0;
        }

        if (!this.connectionPresent)
        {
            this.resultAction = ERROR_CONNECTION_NOT_PRESENT;
            return 0;
        }

        if(this.targetAddress == null)
        {
            this.resultAction = ERROR_UNABLE_RESOLVE_ADDR;
            return 0;
        }


        try
        {
            SendInfo dataToSend = infos[0];

            DatagramSocket socket = new DatagramSocket();


            DatagramPacket packet = new DatagramPacket(dataToSend.Data,dataToSend.Data.length, this.targetAddress, dataToSend.Port);
            socket.send(packet);

            this.resultAction = 0;
        }
        catch (Exception ex)
        {
            this.exception = ex.getMessage();
            this.resultAction = ERROR_EXCEPTION_DURING_SENDING;
        }
        return 0;
    }

    InetAddress getTargetAddress() throws IOException {
        WifiManager wifi = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        if(dhcp == null)
        {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(dhcp.gateway);
        return InetAddress.getByAddress(buffer.array());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        try {

        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiState = connMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        connectionPresent = wifiState.getState() == NetworkInfo.State.CONNECTED;

        if (!connectionPresent)
            return;

            this.targetAddress = getTargetAddress();
        } catch (Exception ex) {
           this.resultAction = ERROR_EXCEPTION_DURING_CONNECTIVITY_CHECK;
            this.exception = ex.getMessage();
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        String resultMessage = "Successfully sent";
        switch (this.resultAction)
        {
            case ERROR_CONNECTION_NOT_PRESENT:
                resultMessage = "Connection is not present";
                break;
            case  ERROR_UNABLE_RESOLVE_ADDR:
                resultMessage = "Unable to resolve target address";
                break;
            case ERROR_EXCEPTION_DURING_SENDING:
                resultMessage = this.exception;
                break;
            case ERROR_EXCEPTION_DURING_CONNECTIVITY_CHECK:
                resultMessage = this.exception;
                break;
        }

        this.resultTextView.setText(resultMessage);
    }


}
