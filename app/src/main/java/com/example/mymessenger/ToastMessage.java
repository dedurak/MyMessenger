package com.example.mymessenger;

import android.content.Context;
import android.widget.Toast;

public class ToastMessage {
    private Context context;
    private CharSequence text;
    private int duration;

    public ToastMessage(Context context, CharSequence text, int duration) {
        this.context = context;
        this.text = text;
        this.duration = duration;
    }

    public Toast getToast() {
        Toast toast = Toast.makeText(context, text, duration);
        return toast;
    }
}
