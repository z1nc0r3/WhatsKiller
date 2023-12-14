package com.zincore.whatskiller;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class FSAccessibilityService extends AccessibilityService {
    private static final String ANDROID_WIDGET_BUTTON = "android.widget.Button";
    private static final Object CALLBACK_ACCESS_LOCK = new Object();

    private static WeakReference<FSClient> sCallback;

    private FSClient getClient() {
        synchronized (CALLBACK_ACCESS_LOCK) {
            if (sCallback == null) {
                return null;
            }
        }
        return sCallback.get();
    }

    public static void setClient(FSClient callback) {
        if (callback == null) {
            return;
        }
        synchronized (CALLBACK_ACCESS_LOCK) {
            sCallback = new WeakReference<>(callback);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        FSClient client = getClient();
        if (client != null) {
            performForceStop(event);
        }
    }

    @Override
    public void onInterrupt() {
        // TODO : nothing here.
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void performForceStop(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();

        performClickButtonByText(source, "FORCE STOP");
        performClickButtonByText(source, "OK");

        if (checkButtonStatus(source))
            Objects.requireNonNull(getClient()).onAppStopped();
    }

    private void performClickButtonByText(AccessibilityNodeInfo source, String text) {
        try {
            List<AccessibilityNodeInfo> forceStopNodes = source.findAccessibilityNodeInfosByText(text);
            if (forceStopNodes != null && !forceStopNodes.isEmpty()) {
                for (int i = 0; i < forceStopNodes.size(); i++) {
                    AccessibilityNodeInfo node = forceStopNodes.get(i);
                    if (node == null) {
                        continue;
                    }
                    if (ANDROID_WIDGET_BUTTON.contentEquals(node.getClassName())) {
                        if (node.isEnabled()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean checkButtonStatus(AccessibilityNodeInfo source) {
        List<AccessibilityNodeInfo> forceStopNode = source.findAccessibilityNodeInfosByText("FORCE STOP");
        if (forceStopNode != null && !forceStopNode.isEmpty()) {
            AccessibilityNodeInfo node = forceStopNode.get(0);
            if (node == null)
                return false;
            else
                return !node.isEnabled();
        } else return false;
    }

    public interface FSClient {
        void onAppStopped();
    }

}