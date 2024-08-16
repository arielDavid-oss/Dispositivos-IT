package com.davidoss.dispositivos_it;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private  static  final String SERVER_URL = "https://aa4f-187-154-238-19.ngrok-free.app";
    private  static Socket mSocket;

    private SocketManager() {
        try {
            mSocket = IO.socket(SERVER_URL);
        } catch (URISyntaxException e) {
            Log.d("Error", e.getMessage());
        }
    }

    public static Socket getInstance() {
        if(mSocket == null) {
            new SocketManager();
        }
        return mSocket;
    }
}
