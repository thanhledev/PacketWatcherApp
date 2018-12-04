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

import com.example.thenguyen.packetwatcherapp.tcp.AndroidManualTcpClientRunnable;
import com.example.thenguyen.packetwatcherapp.udp.AndroidManualUdpClientRunnable;

import thenguyen.pw.model.Memo;

public class ManualClientActivity extends AppCompatActivity {

    EditText serverAddress;
    EditText serverPort;
    EditText memoTitle;
    EditText memoContent;
    Spinner packetSpinner;
    TextView statusLabel;
    Button sendBtn;

    ManualClientHandler handler;
    private String protocol;
    private Thread sendingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_client);

        setTitle(getResources().getString(R.string.app_name) + " Client Screen - Manual");

        // get widgets
        serverAddress = findViewById(R.id.serverIP);
        serverPort = findViewById(R.id.serverPort);
        memoTitle = findViewById(R.id.memoTitle);
        memoContent = findViewById(R.id.memoContent);
        packetSpinner = findViewById(R.id.packetProtocol);
        sendBtn = findViewById(R.id.sendButton);
        statusLabel = findViewById(R.id.statusLabel);

        // init handler
        handler = new ManualClientHandler(this);

        // handle widget events
        packetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                protocol = packetSpinner.getSelectedItem().toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        sendBtn.setOnClickListener(v -> {
            // create memo
            Memo memo = new Memo(memoTitle.getText().toString(), memoContent.getText().toString());
            if(protocol.equals("tcp")) {
                sendMemoViaTcp(memo);
            } else if (protocol.equals("udp")) {
                sendMemoViaUdp(memo);
            } else {
                statusLabel.setText("Please choose a protocol");
            }
        });
    }

    private void sendMemoViaTcp(Memo memo) {
        // init & start sending thread
        sendingThread = new Thread(new AndroidManualTcpClientRunnable(serverAddress.getText().toString(),
                Integer.parseInt(serverPort.getText().toString()),memo, handler));
        sendingThread.setDaemon(true);
        sendingThread.start();
    }

    private void sendMemoViaUdp(Memo memo) {
        // init & start sending thread
        sendingThread = new Thread(new AndroidManualUdpClientRunnable(serverAddress.getText().toString(),
                Integer.parseInt(serverPort.getText().toString()),memo, handler));
        sendingThread.setDaemon(true);
        sendingThread.start();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    public static class ManualClientHandler extends Handler {
        public static final int UPDATE_STATUS = 0;

        private ManualClientActivity ownerActivity;

        public ManualClientHandler(ManualClientActivity owner) {
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
