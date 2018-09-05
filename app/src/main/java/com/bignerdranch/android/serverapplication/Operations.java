package com.bignerdranch.android.serverapplication;

import java.util.HashMap;

public class Operations {
    //соответие между названием методом и его кодом
    private static HashMap<Short, String> methodsNames;

    static {
        methodsNames = new HashMap<>();
    }

    public static void addMethod(Short key, String name) {
        methodsNames.put(key, name);
    }

    //получить название метода по его коду
    public static String getMethodName(Short method) {
        return methodsNames.get(method);
    }

    // метод сложения двух чисел по 2 байта
    public static int sum(short a, short b) {
        return a + b;
    }

    // метод умножения двух чисел по 2 байта
    public static int mul(short a, short b) {
        return a * b;
    }

    // метод возведения в степень для двух чисел по 2 байта
    public static int pow(short a, short b) {
        return (int) Math.pow(a, b);
    }
}
