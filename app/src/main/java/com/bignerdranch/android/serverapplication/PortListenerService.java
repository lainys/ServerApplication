package com.bignerdranch.android.serverapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class PortListenerService extends Service {

    private int mPort = -1;
    private Thread mThread = null;
    private ServerSocket mServerSocket = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        mPort = intent.getIntExtra("port", 1234);
        mThread = new Thread() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(mPort);
                    while (true) {
                        if (isInterrupted()) {
                            Log.d("tag", "Stopped");
                            mThread = null;
                            mServerSocket = null;
                            break;
                        }
                        // ждем подключение клиента
                        Socket client = mServerSocket.accept();

                        Log.d("tag", "Client accepted");

                        // запускаем цикл обработки клиента
                        Thread thread = new Thread(new ClientThread(client));
                        thread.start();
                    }
                } catch (SocketException e) {
                    Log.d("tag", "Stopped socket");
                    mThread = null;
                    mServerSocket = null;
                } catch (IOException e) {
                    Log.w("tag", e);
                    Toast.makeText(PortListenerService.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mThread.start();

        Log.d("tag", "Started");

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mThread != null) mThread.interrupt();
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d("tag", "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
