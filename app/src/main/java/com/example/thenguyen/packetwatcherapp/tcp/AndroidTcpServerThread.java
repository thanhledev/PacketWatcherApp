package com.example.thenguyen.packetwatcherapp.tcp;

import com.example.thenguyen.packetwatcherapp.ServerActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import android.os.Message;

public class AndroidTcpServerThread extends Thread {

    private Queue mQueue;
    private int queueSize;
    private int serverPort;
    private String serverType;

    private ServerActivity.ServerHandler screenHandler;

    private ServerSocket serverSocket;

    public AndroidTcpServerThread(String type, Queue queue, int size, int port, ServerActivity.ServerHandler
                                  handler) {
        serverType = type;
        mQueue = queue;
        queueSize = size;
        serverPort = port;
        screenHandler = handler;
    }

    private boolean initServerSocket() {
        try {
            serverSocket = new ServerSocket(this.serverPort);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void run() {
        if(initServerSocket()) {
            // lock ui
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.LOCK_UI, true));

            // print to log
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.APPEND_LOG, "Start listening for packet at" +
                            "port: " + serverPort));

            try {
                while (true) {
                    Socket socket = serverSocket.accept();

                    Thread newTcpConn = serverType.equals("auto") ?
                            new Thread(new AndroidTcpServerMultipleRunnable(socket, mQueue,
                                    queueSize, screenHandler)):
                            new Thread(new AndroidTcpServerSingleRunnable(socket, mQueue,
                                    queueSize, screenHandler));
                    newTcpConn.setDaemon(true);
                    newTcpConn.start();
                }
            } catch (IOException e) {
                screenHandler.sendMessage(Message.obtain(screenHandler,
                        ServerActivity.ServerHandler.APPEND_LOG, "Error: " + e.getMessage()));
            }

        } else {
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.APPEND_LOG, "Error: Cannot initialize socket"));
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {

        }

        screenHandler.sendMessage(Message.obtain(screenHandler,
                ServerActivity.ServerHandler.LOCK_UI, false));
    }
}
