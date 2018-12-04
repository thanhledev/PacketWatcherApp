package com.example.thenguyen.packetwatcherapp.tcp;

import thenguyen.pw.model.Memo;
import thenguyen.pw.tcp.TcpSenderRunnable;
import com.example.thenguyen.packetwatcherapp.ServerActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Queue;

import android.os.Message;

public class AndroidTcpServerSingleRunnable extends TcpSenderRunnable {

    private ServerActivity.ServerHandler screenHandler;

    private Queue mQueue;
    private int queueSize;

    public AndroidTcpServerSingleRunnable(Socket socket, Queue queue, int size,
                                          ServerActivity.ServerHandler handler) {
        super(socket);
        mQueue = queue;
        queueSize = size;
        screenHandler = handler;
    }

    @Override
    public void run() {
        current = Thread.currentThread();

        try {
            ObjectInputStream inStream = new ObjectInputStream(
                    new BufferedInputStream(sSocket.getInputStream()));

            // get memo
            Memo newMemo = (Memo) inStream.readObject();

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
        } catch (ClassNotFoundException | IOException | InterruptedException e) {
            screenHandler.sendMessage(Message.obtain(screenHandler,
                    ServerActivity.ServerHandler.APPEND_LOG, "Error:" + e.getMessage()));
        }
    }

    @Override
    public void stop() {
        current.interrupt();
    }
}
