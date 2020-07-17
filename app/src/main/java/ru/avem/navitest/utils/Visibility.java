package ru.avem.navitest.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class Visibility {
    public static void addTabToTabHost(TabHost tabHost, String tag, int viewId, String label) {
        TabHost.TabSpec spec = tabHost.newTabSpec(tag);
        spec.setContent(viewId);
        spec.setIndicator(label);
        tabHost.addTab(spec);
    }

    public static void switchTabState(TabWidget tabs, int index, boolean state, TabHost tabHost) {
        tabs.getChildTabViewAt(index).setEnabled(state);
        TextView tabTextView = tabHost.getTabWidget().getChildAt(index).findViewById(android.R.id.title);
        String color;
        if (state) {
            color = "#000000";
        } else {
            color = "#AAAAAA";
        }
        tabTextView.setTextColor(Color.parseColor(color));
    }

    public static void disableView(View view) {
        view.setEnabled(false);
    }

    public static void enableView(View view) {
        view.setEnabled(true);
    }

    public static void fullScreenCall(Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static void setEmptyText(TextView textView) {
        textView.setText("");
    }

    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static void setViewAndChildrenVisibility(View view, int visibility) {
        view.setVisibility(visibility);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenVisibility(child, visibility);
            }
        }
    }
}