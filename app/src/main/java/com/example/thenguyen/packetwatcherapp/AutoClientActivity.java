package com.example.thenguyen.packetwatcherapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;

import com.example.thenguyen.packetwatcherapp.tcp.AndroidAutoTcpClientRunnable;
import com.example.thenguyen.packetwatcherapp.udp.AndroidAutoUdpClientRunnable;

public class AutoClientActivity extends AppCompatActivity {

    EditText serverAddress;
    EditText serverPort;
    Spinner packetSpinner;
    EditText packetSent;
    Button sendBtn;
    TextView statusLabel;

    AutoClientActivity.AutoClientHandler handler;
    private String protocol;
    private int sendingPacket;
    private Thread sendingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_client);

        // set title
        setTitle(getResources().getString(R.string.app_name) + " Client Screen - Auto");

        serverAddress = findViewById(R.id.autoServerIP);
        serverPort = findViewById(R.id.autoServerPort);
        packetSpinner = findViewById(R.id.autoPacketProtocol);
        packetSent = findViewById(R.id.autoSendingPacket);
        sendBtn = findViewById(R.id.autoSendButton);
        statusLabel = findViewById(R.id.autoStatusLabel);
        statusLabel.setMovementMethod(new ScrollingMovementMethod());

        // init handler
        handler = new AutoClientActivity.AutoClientHandler(this);

        packetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                protocol = packetSpinner.getSelectedItem().toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sendBtn.setOnClickListener(v -> {
            sendingPacket = Integer.parseInt(packetSent.getText().toString());
            if(protocol.equals("tcp")) {
                sendMemoViaTcp();
            } else if (protocol.equals("udp")) {
                sendMemoViaUdp();
            } else {
                statusLabel.setText("Please specify protocol and number of outgoing packet.");
            }
        });
    }

    private void sendMemoViaTcp() {
        // init & start sending thread
        sendingThread = new Thread(new AndroidAutoTcpClientRunnable(serverAddress.getText().toString(),
                Integer.parseInt(serverPort.getText().toString()), sendingPacket, handler));
        sendingThread.setDaemon(true);
        sendingThread.start();
    }

    private void sendMemoViaUdp() {
        // init & start sending thread
        sendingThread = new Thread(new AndroidAutoUdpClientRunnable(serverAddress.getText().toString(),
                Integer.parseInt(serverPort.getText().toString()), sendingPacket, handler));
        sendingThread.setDaemon(true);
        sendingThread.start();
    }

    private void updateStatus(String message) {
        statusLabel.append(message + "\n");
    }

    public static class AutoClientHandler extends Handler {
        public static final int UPDATE_STATUS = 0;

        private AutoClientActivity ownerActivity;

        public AutoClientHandler(AutoClientActivity owner) {
            ownerActivity = owner;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_STATUS:
                    ownerActivity.updateStatus((String)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
