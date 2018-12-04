package com.example.thenguyen.packetwatcherapp.printer;

import android.os.Message;

import thenguyen.pw.model.Memo;
import com.example.thenguyen.packetwatcherapp.ServerActivity;

import java.util.Queue;

public class ServerPrinterThread extends Thread {
    private Queue mQueue;
    private ServerActivity.ServerHandler screenHandler;

    public ServerPrinterThread(Queue queue, ServerActivity.ServerHandler handler) {
        mQueue = queue;
        screenHandler = handler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (mQueue) {
                    while (mQueue.isEmpty()) {
                        mQueue.wait();
                    }
                }

                Memo memo = (Memo) mQueue.poll();

                // print to log
                screenHandler.sendMessage(Message.obtain(screenHandler,
                        ServerActivity.ServerHandler.APPEND_LOG, "New memo:" + memo.printMemo()));

            } catch (InterruptedException e) {
                screenHandler.sendMessage(Message.obtain(screenHandler,
                        ServerActivity.ServerHandler.APPEND_LOG, "Error:" + e.getMessage()));
            }
        }
    }
}
