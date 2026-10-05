package com.example.myapp;

import android.view.SoundEffectConstants;
import android.view.View;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;



@CapacitorPlugin(name = "SoundFeedback")
public class SoundFeedbackPlugin extends Plugin
{
    @PluginMethod
    public void trigger(PluginCall call)
    {
        getActivity().runOnUiThread( () ->
        {
            View view = getBridge().getWebView();
            view.setSoundEffectsEnabled(true);
            view.playSoundEffect(SoundEffectConstants.CLICK);

            call.resolve();
        });
    }
}
