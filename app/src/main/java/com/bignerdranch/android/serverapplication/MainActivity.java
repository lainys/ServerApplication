package com.bignerdranch.android.serverapplication;

import android.arch.core.util.Function;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.lang.ref.WeakReference;

import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    int mPort;

    ToggleButton mServerButton;

    Button mInitButton;

    EditText mPortField;
    EditText mAddressField;

    public static int getConfig(String configAddress) throws Exception {
        // инициализация xml документа для парсинга
        Document config = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configAddress);

        // список всех методов из конфигурации
        NodeList methods = config.getElementsByTagName("cmd");

        // парсинг каждого метода
        for (int i = 0; i < methods.getLength(); i++) {
            NamedNodeMap attributes = methods.item(i).getAttributes();
            Short key = Short.parseShort(attributes.getNamedItem("value").getNodeValue(), 16);
            String name = attributes.getNamedItem("method").getNodeValue();
            // добавляем в словарь операций
            Operations.addMethod(key, name);
        }

        // порт для сервера
        return Integer.parseInt(config.getDocumentElement().getAttribute("port"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServerButton = findViewById(R.id.server_button);
        mServerButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                Intent intent = new Intent(this, PortListenerService.class);
                intent.putExtra("port", mPort);
                startService(intent);
            } else {
                stopService(new Intent(this, PortListenerService.class));
            }
        });

        mInitButton = findViewById(R.id.init_button);
        mInitButton.setOnClickListener(view -> {
            mServerButton.setEnabled(false);
            String configAddress = mAddressField.getText().toString() + ":" + mPortField.getText().toString() + "/config.xml";
            new Task(this, configAddress, (port) -> {
                mServerButton.setEnabled(true);
                mPort = port;
                return null;
            }).execute();
        });

        mPortField = findViewById(R.id.port_field);
        mAddressField = findViewById(R.id.address_field);
    }

    private static class Task extends AsyncTask<Void, Void, Integer> {

        private WeakReference<Context> context;
        private String address;
        private Function<Integer, Void> onSuccess;

        Task(Context context, String address, Function<Integer, Void> onSuccess) {
            this.context = new WeakReference<>(context);
            this.address = address;
            this.onSuccess = onSuccess;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return getConfig(address);
            } catch (Exception e) {
                Log.w("tag", e);
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != -1) onSuccess.apply(result);
            else if (context.get() != null)
                Toast.makeText(context.get(), "Ошибка", Toast.LENGTH_SHORT).show();
        }
    }
}
