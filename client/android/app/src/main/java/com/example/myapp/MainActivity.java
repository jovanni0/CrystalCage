package com.example.myapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;



public class MainActivity extends BridgeActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        registerPlugin(ServerDiscoveryPlugin.class);
        registerPlugin(HapticFeedbackPlugin.class);
        registerPlugin(SoundFeedbackPlugin.class);

        super.onCreate(savedInstanceState);
    }
}