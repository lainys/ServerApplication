package com.bignerdranch.android.serverapplication;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ClientThread implements Runnable {

    //длина сообщения
    private short lenMessage;
    // код операции
    private short codeOperation;
    // первый аргумент
    private short firstArg;
    // второй аргумент
    private short secondArg;
    // ответ
    private int answer;
    // сокет клиента
    private Socket client;

    public ClientThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        // парсим запрос
        getQuery();
        // вычисляем ответ
        calculate(codeOperation, firstArg, secondArg);
        // отправляем ответ клиенту
        sendAnswer(answer);
    }

    public void getQuery() {
        try {
            // парсим все элементы запроса
            lenMessage = (short) (client.getInputStream().read() << 8 | client.getInputStream().read());
            codeOperation = (short) (client.getInputStream().read() << 8 | client.getInputStream().read());
            firstArg = (short) (client.getInputStream().read() << 8 | client.getInputStream().read());
            secondArg = (short) (client.getInputStream().read() << 8 | client.getInputStream().read());

        } catch (IOException e) {
            System.out.println("Возникла ошибка во время чтения запроса");
        }
    }

    public void calculate(short code, short a, short b) {
        try {
            // указываем аргументы функции
            Class[] params = new Class[]{short.class, short.class};
            // находим функцию в классе по коду
            Method method = Operations.class.getMethod(Operations.getMethodName(code), params);
            // вычисляем результат функции
            answer = Integer.parseInt(method.invoke(Operations.class, a, b).toString());
        } catch (Exception e) {
            System.out.println("Возникла ошибка во выполнения запроса");
        }
    }

    public void sendAnswer(int answer) {
        try {
            // отправляем длину сообщения
            client.getOutputStream().write((byte) lenMessage >>> 8);
            client.getOutputStream().write((byte) lenMessage);
            // код операции
            client.getOutputStream().write((byte) (codeOperation >> 8));
            client.getOutputStream().write((byte) codeOperation);
            // ответ
            client.getOutputStream().write((byte) (answer >> 24));
            client.getOutputStream().write((byte) (answer >> 16));
            client.getOutputStream().write((byte) (answer >> 8));
            client.getOutputStream().write((byte) answer);

            client.getOutputStream().flush();
        } catch (IOException e) {

            System.out.println("Возникла ошибка при передаче ответа клиенту");
        }
    }

}
