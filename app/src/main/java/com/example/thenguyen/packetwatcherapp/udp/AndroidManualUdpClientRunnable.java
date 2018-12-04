package com.example.thenguyen.packetwatcherapp.udp;

import thenguyen.pw.helper.Lib;
import thenguyen.pw.model.Memo;
import com.example.thenguyen.packetwatcherapp.ManualClientActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.Message;

public class AndroidManualUdpClientRunnable implements Runnable {

    private String serverIp;
    private int serverPort;
    private Memo sendMemo;
    ManualClientActivity.ManualClientHandler screenHandler;

    public AndroidManualUdpClientRunnable(String host, int port, Memo memo, ManualClientActivity.ManualClientHandler
                                    handler) {
        serverIp = host;
        serverPort = port;
        sendMemo = memo;
        screenHandler = handler;
    }

    @Override
    public void run() {
        try {
            // init server information
            InetAddress serverAddr = InetAddress.getByName(serverIp);
            // init socket
            DatagramSocket udpSocket = new DatagramSocket(serverPort);
            // create packet
            byte[] memoByteArray = Lib.objectToByteArray(sendMemo);
            DatagramPacket packet = new DatagramPacket(memoByteArray, memoByteArray.length,
                    serverAddr, serverPort);
            // send packet
            udpSocket.send(packet);
            // close socket
            udpSocket.close();
            // update status label
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ManualClientActivity.ManualClientHandler.UPDATE_STATUS, "Send packet to "
                            + serverIp + ":" + serverPort + " successfully!"));

        } catch (IOException e) {
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ManualClientActivity.ManualClientHandler.UPDATE_STATUS, "Error: " + e.getMessage()));
        }
    }
}
