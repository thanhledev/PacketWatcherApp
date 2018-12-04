package com.example.thenguyen.packetwatcherapp.udp;

import thenguyen.pw.helper.Lib;
import thenguyen.pw.model.Memo;
import thenguyen.pw.udp.UdpSenderRunnable;
import com.example.thenguyen.packetwatcherapp.ServerActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

import android.os.Message;

public class AndroidUdpServerRunnable extends UdpSenderRunnable {

    private Queue mQueue;
    private int queueSize;
    private int serverPort;

    private ServerActivity.ServerHandler screenHandler;

    public AndroidUdpServerRunnable(Queue queue, int size, int port, ServerActivity.ServerHandler
                                    handler) {
        mQueue = queue;
        queueSize = size;
        serverPort = port;
        screenHandler = handler;
    }

    private boolean initSocket() {
        try {
            sSocket = new DatagramSocket(serverPort);
            return true;
        } catch (SocketException e) {
            return false;
        }
    }

    @Override
    public void run() {
        if(initSocket()) {
            // lock ui
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.LOCK_UI, true));

            // print to log
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.APPEND_LOG, "Start listening for packet at" +
                            " port: " + serverPort));

            current = Thread.currentThread();

            try {
                while (true) {
                    byte[] memoByteArray = new byte[super.PACKET_SIZE];
                    DatagramPacket packet = new DatagramPacket(memoByteArray, memoByteArray.length);

                    // get packet
                    sSocket.receive(packet);

                    // packet to memo
                    Memo newMemo = Lib.arrayToMemo(packet.getData());

                    // add to queue
                    synchronized (mQueue) {
                        while (mQueue.size() == queueSize) {
                            mQueue.wait();
                        }
                    }
                    mQueue.add(newMemo);
                    synchronized (mQueue) {
                        mQueue.notify();
                    }
                }
            } catch (InterruptedException | IOException e) {
                screenHandler.sendMessage(Message.obtain(screenHandler,
                        ServerActivity.ServerHandler.APPEND_LOG, "Error:" + e.getMessage()));
            }

        } else {
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.APPEND_LOG, "Error: Cannot initialize socket"));
        }
    }

    @Override
    public void stop() {
        sSocket.close();
        current.interrupt();
        screenHandler.sendMessage(Message.obtain(screenHandler,
                ServerActivity.ServerHandler.LOCK_UI, false));
    }
}
