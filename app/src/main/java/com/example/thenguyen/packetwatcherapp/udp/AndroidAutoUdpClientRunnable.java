package com.example.thenguyen.packetwatcherapp.udp;

import android.os.Message;

import com.example.thenguyen.packetwatcherapp.AutoClientActivity;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import thenguyen.pw.helper.Lib;
import thenguyen.pw.model.Memo;

public class AndroidAutoUdpClientRunnable implements Runnable {
    private String serverIp;
    private int serverPort;
    private int sendingPacket;
    private Lorem lorem;
    AutoClientActivity.AutoClientHandler screenHandler;

    public AndroidAutoUdpClientRunnable(String host, int port, int packet, AutoClientActivity.AutoClientHandler
            handler) {
        serverIp = host;
        serverPort = port;
        sendingPacket = packet;
        screenHandler = handler;
        lorem = LoremIpsum.getInstance();
    }

    @Override
    public void run() {
        try {
            // init server information
            InetAddress serverAddr = InetAddress.getByName(serverIp);
            // init socket
            DatagramSocket udpSocket = new DatagramSocket(serverPort);
            for(int i = 1; i <= sendingPacket; i++) {
                // create fake memo
                Memo memo = new Memo(lorem.getWords(3,7), lorem.getWords(5,15));
                // create packet
                byte[] memoByteArray = Lib.objectToByteArray(memo);
                DatagramPacket packet = new DatagramPacket(memoByteArray, memoByteArray.length,
                        serverAddr, serverPort);
                // send packet
                udpSocket.send(packet);

                //update statistics
                screenHandler.sendMessage(Message.obtain(screenHandler,
                        AutoClientActivity.AutoClientHandler.UPDATE_SENT,
                        String.format("%d", i)));

                screenHandler.sendMessage(Message.obtain(screenHandler,
                        AutoClientActivity.AutoClientHandler.UPDATE_REMAIN,
                        String.format("%d", sendingPacket - i)));

                // update status label
                /*screenHandler.sendMessage(Message.obtain(screenHandler,
                        AutoClientActivity.AutoClientHandler.UPDATE_STATUS,
                        String.format("Sending packet#%d successfully.Remain %d", i, sendingPacket - i)));*/

                // adjust to test
                //Thread.sleep(5);
            }

            // close socket
            udpSocket.close();

            screenHandler.sendMessage(Message.obtain(screenHandler,
                    AutoClientActivity.AutoClientHandler.UPDATE_STATUS, "Send "
                            + sendingPacket + " udp packets to "
                            + serverIp + ":" + serverPort + " successfully!"));

        } catch (IOException e) { // IOException | InterruptedException e
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    AutoClientActivity.AutoClientHandler.UPDATE_STATUS, "Error: " + e.getMessage()));
        }
    }
}
