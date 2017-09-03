package com.tt.socket.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private ClientHandlerThread clientHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clientHandler = new ClientHandlerThread("Client HandlerThread");
        clientHandler.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clientHandler.quit();
    }

    @OnClick({R.id.connect, R.id.disconnect})
    public void onButtonClick(Button button) {
        if (button.getId() == R.id.connect) {
            clientHandler.connectServer("");
        } else if (button.getId() == R.id.disconnect) {
            clientHandler.disconnectServer();
        }
    }
}
