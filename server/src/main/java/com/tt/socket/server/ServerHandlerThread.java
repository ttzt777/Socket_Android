package com.tt.socket.server;

import android.os.HandlerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import timber.log.Timber;

/**
 * Created by zhaotao on 2017/9/3.
 */

public class ServerHandlerThread extends HandlerThread {

    private static final int PORT = 9999;

    private List<Socket> mClientList = new ArrayList<>();

    private volatile ServerSocket server;

    private ExecutorService mExecutorService;

    private volatile boolean mIsServerWork;

    public ServerHandlerThread(String name) {
        super(name);
    }

    private void startServer() {
        if (mIsServerWork) {
            return;
        }

        new ServerThread().start();
    }

    class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                mIsServerWork = true;
                server = new ServerSocket(PORT);
            } catch (IOException e) {
                e.printStackTrace();
                mIsServerWork = false;
            }

            while (mIsServerWork) {
                try {
                    Socket client = server.accept();
                    mClientList.add(client);
                    mExecutorService.execute(new ServiceForClient(client));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ServiceForClient implements Runnable {
        private Socket client;
        private BufferedReader in;

        public ServiceForClient(Socket client) {
            this.client = client;

            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (client.isConnected()) {
                    if (in.readLine() != null) {
                        Timber.d("receive data from client");
                    } else {
                        Timber.d("receive null from client");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
