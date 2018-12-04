package com.example.thenguyen.packetwatcherapp;

import thenguyen.pw.model.Memo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.thenguyen.packetwatcherapp.printer.ServerPrinterThread;
import com.example.thenguyen.packetwatcherapp.tcp.AndroidTcpServerThread;
import com.example.thenguyen.packetwatcherapp.udp.AndroidUdpServerRunnable;

import java.util.LinkedList;
import java.util.Queue;

public class ServerActivity extends AppCompatActivity {

    Button startBtn;
    Button stopBtn;
    Spinner protocolSpinner;
    Spinner typeSpinner;
    EditText serverLog;
    EditText serverPort;

    ServerHandler handler;

    private Thread serverThread;
    private Thread printThread;
    private AndroidUdpServerRunnable udpServerRunnable;

    private Queue memoQueue;
    private static final int queueSize = 100;
    private String protocol;
    private String serverType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        setTitle(getResources().getString(R.string.app_name) + " Server Screen");

        // get widgets
        startBtn = findViewById(R.id.startButton);
        stopBtn = findViewById(R.id.stopButton);
        protocolSpinner = findViewById(R.id.protocolSpinner);
        typeSpinner = findViewById(R.id.typeSpinner);
        serverLog = findViewById(R.id.serverLog);
        serverPort = findViewById(R.id.serverPort);

        // init handler
        handler = new ServerHandler(this);

        // init queue
        memoQueue = new LinkedList<Memo>();

        // handle button clicks
        startBtn.setOnClickListener((v) -> {
            startServer();
        });

        stopBtn.setOnClickListener((v) -> {
            stopServer();
        });

        protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                protocol = protocolSpinner.getSelectedItem().toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serverType = typeSpinner.getSelectedItem().toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void startServer() {
        if(!protocol.isEmpty() && !serverType.isEmpty()) {
            // init & start printerThread
            printThread = new ServerPrinterThread(memoQueue, handler);
            printThread.setDaemon(true);
            printThread.start();

            // init & start serverThread
            if(protocol.equals("tcp")) {
                serverThread = new AndroidTcpServerThread(serverType, memoQueue, queueSize,
                        Integer.parseInt(serverPort.getText().toString()),
                        handler);
            } else {
                udpServerRunnable = new AndroidUdpServerRunnable(memoQueue,
                        queueSize, Integer.parseInt(serverPort.getText().toString()),
                        handler);
                serverThread = new Thread(udpServerRunnable);
            }

            serverThread.setDaemon(true);
            serverThread.start();

        } else {
            Toast.makeText(getApplicationContext(), "Please select protocol & type", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void stopServer() {
        printThread.interrupt();

        if(protocol.equals("tcp")) {
            serverThread.interrupt();
        } else {
            udpServerRunnable.stop();
        }
    }

    private void appendServerLog(String log) {
        serverLog.append(log + "\n");
    }

    private void lockUI(boolean isLock) {
        serverPort.setEnabled(isLock? false : true);
        protocolSpinner.setEnabled(isLock? false : true);
        startBtn.setEnabled(isLock? false : true);
        stopBtn.setEnabled(isLock? true : false);
    }

    public static class ServerHandler extends Handler {
        public static final int APPEND_LOG = 1;
        public static final int LOCK_UI = 2;

        private ServerActivity ownerActivity;

        public ServerHandler(ServerActivity owner) {
            ownerActivity = owner;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APPEND_LOG:
                    ownerActivity.appendServerLog((String)msg.obj);
                    break;
                default:
                    ownerActivity.lockUI((boolean) msg.obj);
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
