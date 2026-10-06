package com.example.myapp;

import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.Nullable;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;



@CapacitorPlugin(name = "HapticFeedback")
public class HapticFeedbackPlugin extends Plugin
{
    @PluginMethod
    public void trigger(PluginCall call)
    {
        String raw_feedback_type = call.getString("haptic", "tick");

        if ("none".equals(raw_feedback_type) || raw_feedback_type == null)
        {
            call.resolve(new JSObject().put("performed", false));
            return;
        }

        Integer feedback_type = constantFor(raw_feedback_type);

        if (feedback_type == null)
        {
            call.resolve(new JSObject().put("performed", false));
            return;
        }

        getActivity().runOnUiThread( () ->
        {
            View view = getBridge().getWebView();
            view.setHapticFeedbackEnabled(true);
            boolean success = view.performHapticFeedback(feedback_type);

            call.resolve(new JSObject().put("performed", success));
        });
    }


    @Nullable
    private Integer constantFor(String kind)
    {
        int api = Build.VERSION.SDK_INT;

        switch (kind)
        {
            case "segment": return api >= 34 ? HapticFeedbackConstants.SEGMENT_FREQUENT_TICK : HapticFeedbackConstants.CLOCK_TICK;
            case "confirm": return api >= 30 ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.VIRTUAL_KEY;
            case "reject":  return api >= 30 ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.LONG_PRESS;
            case "key":     return HapticFeedbackConstants.KEYBOARD_TAP;
            case "tick":    return HapticFeedbackConstants.CLOCK_TICK;
            default:        return null;
        }
    }
}