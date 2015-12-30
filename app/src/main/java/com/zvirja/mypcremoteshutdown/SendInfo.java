package com.zvirja.mypcremoteshutdown;

/**
 * Created by Zvirja on 2/22/2015.
 */
public class SendInfo {
    byte[] Data;
    int Port;

    public SendInfo(byte[] data, int port) {
        Data = data;
        Port = port;
    }
}
