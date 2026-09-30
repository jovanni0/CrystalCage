package com.example.myapp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;



@CapacitorPlugin(name = "AndroidServerDiscovery")
public class ServerDiscoveryPlugin extends Plugin
{
    private static final int DISCOVERY_PORT = 7490;


    @PluginMethod
    public void discover(PluginCall call)
    {
        int timeout_ms = call.getInt("timeout_ms", 2000);

        new Thread( () ->
        {
            JSArray results = new JSArray();

            try
            {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(timeout_ms);

                byte[] message = "DISCOVER_SANCTUM".getBytes();
                InetAddress broadcastAddr = getBroadcastAddress(getContext());
                socket.send(new DatagramPacket(message, message.length, broadcastAddr, DISCOVERY_PORT));

                byte[] buf = new byte[1024];
                long deadline = System.currentTimeMillis() + timeout_ms;

                while (System.currentTimeMillis() < deadline)
                {
                    try
                    {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String reply = new String(packet.getData(), 0, packet.getLength());

                        JSONObject json = new JSONObject(reply);
                        JSObject obj = new JSObject();

                        obj.put("host", packet.getAddress().getHostAddress());
                        obj.put("port", json.getInt("port"));
                        obj.put("name", json.getString("name"));
                        obj.put("https", json.optBoolean("https", false));

                        results.put(obj);
                    }
                    catch (SocketTimeoutException e)
                    {
                        break;
                    }
                    catch (org.json.JSONException e)
                    {
                        // malformed/unexpected reply on this port. ignore and keep listening
                    }
                }
                socket.close();
            }
            catch (Exception e)
            {
                call.reject("Discovery failed: " + e.getMessage());

                return;
            }

            JSObject response = new JSObject();
            response.put("servers", results);

            call.resolve(response);
        }).start();
    }


    private InetAddress getBroadcastAddress(Context context)
            throws Exception
    {
        WifiManager wifiManager = (WifiManager)context
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        DhcpInfo dhcp = wifiManager.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];

        for (int k = 0; k < 4; k++)
        {
            quads[k] = (byte) ((broadcast >> (k * 8)) & 0xFF);
        }

        return InetAddress.getByAddress(quads);
    }
}