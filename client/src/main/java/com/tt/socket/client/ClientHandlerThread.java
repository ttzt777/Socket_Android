package com.tt.socket.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.tt.mylibrary.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by tt on 2017/9/3.
 */

public class ClientHandlerThread extends HandlerThread{
    private static final int PORT = 9999;

    public static final int MSG_START_CONNECT = 0;
    public static final int MSG_SEND_COMMAND = 1;
    public static final int MSG_DISCONNECT = 2;

    private Socket client;

    private InputStream in;
    private OutputStream out;

    private String host;
    private int port;

    private boolean mIsConnected;

    private OnSocketClientCallback mListener;

    private Handler mWorkHandler;
    private Handler.Callback mWorkHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_START_CONNECT:
                    connectServerImpl();
                    break;

                case MSG_SEND_COMMAND:
                    sendCommandImpl((byte[]) message.obj);
                    break;

                case MSG_DISCONNECT:
                    disconnectServerImpl();
                    break;
            }
            return false;
        }
    };

    public ClientHandlerThread(String name) {
        super(name);
    }

    public void connectServer(String host) {
        connectServer(host, PORT);
    }

    public void connectServer(String host, int port) {
        this.host = host;
        this.port = port;

        Message message = mWorkHandler.obtainMessage(MSG_START_CONNECT);
        mWorkHandler.sendMessage(message);
    }

    public void sendCommand(byte[] bytes) {
        Message message = mWorkHandler.obtainMessage(MSG_SEND_COMMAND, bytes);
        mWorkHandler.sendMessage(message);
    }

    public void disconnectServer() {
        Message message = mWorkHandler.obtainMessage(MSG_DISCONNECT);
        mWorkHandler.sendMessage(message);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mWorkHandler = new Handler(getLooper(), mWorkHandlerCallback);
    }

    private void connectServerImpl() {
        if (mIsConnected) {
            return ;
        }

        if (host == null) {
            Timber.e("Connect to server error, host is null");
        }

        try {
            mIsConnected = true;
            client = new Socket(host, port);

            in = client.getInputStream();
            out = client.getOutputStream();

            new ReceiveThread().start();

        } catch (IOException e) {

            e.printStackTrace();
            mIsConnected = false;
            client = null;
            Timber.e("Create socket error");
            return;
        }
    }

    private void sendCommandImpl(byte[] bytes) {
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnectServerImpl() {
        if (mIsConnected) {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {

            } finally {
                client = null;
                mIsConnected = false;

                try{
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try{
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }

                in = null;
                out = null;
            }
        }
    }


    class ReceiveThread extends Thread {
        @Override
        public void run() {
            while(mIsConnected) {
                if (client != null && client.isConnected()) {
                    try {
                        byte[] bytes = new byte[100];
                        int readCount = in.read(bytes);
                        byte[] receivedData = Arrays.copyOfRange(bytes, 0, readCount);
                        Timber.d("Receive data from server: %d", StringUtil.bytesToHexString(receivedData));
                        if (mListener != null) {
                            mListener.onClientReceived(receivedData);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Timber.e("Receive data error: %s", e.toString());
                    }
                }
            }
        }
    }

    public interface OnSocketClientCallback {
        void onClientReceived(byte[] bytes);
    }
}
