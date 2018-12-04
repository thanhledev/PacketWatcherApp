package com.example.thenguyen.packetwatcherapp.tcp;

import thenguyen.pw.model.Memo;
import com.example.thenguyen.packetwatcherapp.ManualClientActivity;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Message;

public class AndroidManualTcpClientRunnable implements Runnable {

    private String serverIp;
    private int serverPort;
    private Memo sendMemo;
    ManualClientActivity.ManualClientHandler screenHandler;

    public AndroidManualTcpClientRunnable(String host, int port, Memo memo, ManualClientActivity.ManualClientHandler
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

            // create socket
            Socket tcpSocket = new Socket(serverAddr, serverPort);
            ObjectOutputStream outStream = new ObjectOutputStream(
                    new BufferedOutputStream(tcpSocket.getOutputStream()));

            // send packet
            outStream.writeObject(sendMemo);
            outStream.flush();

            // close socket
            outStream.close();
            tcpSocket.close();

            // update label
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ManualClientActivity.ManualClientHandler.UPDATE_STATUS, "Send packet to "
                    + serverIp + ":" + serverPort + " successfully!"));

        } catch (IOException e) {
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ManualClientActivity.ManualClientHandler.UPDATE_STATUS, "Error: " + e.getMessage()));
        }
    }
}
