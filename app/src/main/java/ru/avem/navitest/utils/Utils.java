package ru.avem.navitest.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;
import java.util.Locale;

public class Utils {
    public static final Locale RU_LOCALE = new Locale("ru");

    private Utils() {
        throw new AssertionError();
    }

    public static void setSpinnerAdapter(Context context, Spinner spinner, List<?> list) {
        ArrayAdapter<?> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }


}
