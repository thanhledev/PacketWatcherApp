package com.example.thenguyen.packetwatcherapp.tcp;

import android.annotation.SuppressLint;
import android.os.Message;

import com.example.thenguyen.packetwatcherapp.AutoClientActivity;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import com.thedeanda.lorem.*;

import thenguyen.pw.model.Memo;

public class AndroidAutoTcpClientRunnable implements Runnable {
    private String serverIp;
    private int serverPort;
    private int sendingPacket;
    private Lorem lorem;
    AutoClientActivity.AutoClientHandler screenHandler;

    public AndroidAutoTcpClientRunnable(String host, int port, int packet, AutoClientActivity.AutoClientHandler
            handler) {
        serverIp = host;
        serverPort = port;
        sendingPacket = packet;
        screenHandler = handler;
        lorem = LoremIpsum.getInstance();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void run() {
        try {
            // init server information
            InetAddress serverAddr = InetAddress.getByName(serverIp);

            // create socket
            Socket tcpSocket = new Socket(serverAddr, serverPort);
            ObjectOutputStream outStream = new ObjectOutputStream(
                    new BufferedOutputStream(tcpSocket.getOutputStream()));

            for (int i = 1; i <= sendingPacket; i++) {

                // send packet
                outStream.writeObject(new Memo(lorem.getWords(3,7),
                        lorem.getWords(5,15)));
                outStream.flush();

                //update statistics
                screenHandler.sendMessage(Message.obtain(screenHandler,
                        AutoClientActivity.AutoClientHandler.UPDATE_SENT,
                        String.format("%d", i)));

                screenHandler.sendMessage(Message.obtain(screenHandler,
                        AutoClientActivity.AutoClientHandler.UPDATE_REMAIN,
                        String.format("%d", sendingPacket - i)));

                /*screenHandler.sendMessage(Message.obtain(screenHandler,
                        AutoClientActivity.AutoClientHandler.UPDATE_STATUS,
                        String.format("Sending packet#%d successfully.Remain %d", i, sendingPacket - i)));*/
                // adjust to test
                //Thread.sleep(5);
            }

            // send quit message
            outStream.writeObject(new Memo("quit","quit"));
            outStream.flush();

            screenHandler.sendMessage(Message.obtain(screenHandler,
                    AutoClientActivity.AutoClientHandler.UPDATE_STATUS, "Send "
                            + sendingPacket + " tcp packets to "
                            + serverIp + ":" + serverPort + " successfully!"));

            // close socket
            outStream.close();
            tcpSocket.close();

        } catch (IOException e) { //IOException| InterruptedException e
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    AutoClientActivity.AutoClientHandler.UPDATE_STATUS, "Error: " + e.getMessage()));
        }
    }
}
