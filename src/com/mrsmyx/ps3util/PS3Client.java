package com.mrsmyx.ps3util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mrsmyx.JMAPI;
import com.mrsmyx.exceptions.JMAPIException;
import com.mrsmyx.utils.Response;

public class PS3Client {

    protected String ip;
    protected int port;
    protected Socket client;
    protected ServerSocket server;
    protected Socket data_sock;

    protected boolean Connect(String ip, int port) {
        this.ip = ip;
        this.port = port;
        if (client == null) {
            client = new Socket();
        }
        if (!client.isConnected() || client.isClosed()) {
            try {
                client = new Socket(ip, port);
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                Response first = parseResponse(Response.Build(true, br.readLine()));
                if (first.getSuccess() && first.getResponseCode() == JMAPI.PS3MAPI_RESPONSECODE.PS3MAPICONNECTED) {
                    Response second = parseResponse(Response.Build(true, br.readLine()));
                    if (second.getSuccess() && second.getResponseCode() == JMAPI.PS3MAPI_RESPONSECODE.PS3MAPICONNECTEDOK) {
                        System.out.println("Connection Established!");
                        return true;
                    }
                    return true;
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    protected JMAPI.PS3MAPI_RESPONSECODE findResponse(int value) {
        for (JMAPI.PS3MAPI_RESPONSECODE r : JMAPI.PS3MAPI_RESPONSECODE.values()) {
            if (r.getValue() == value) {
                return r;
            }
        }
        return null;
    }

    protected void OpenDataSocket() throws IOException, JMAPIException {
        try {
            Response pav = Send("PASV");
            int start = pav.getResponse().indexOf("(") + 1;
            int end = pav.getResponse().indexOf(")");
            String[] split = pav.getResponse().substring(start, end).split(",");
            String ip = String.format("%s.%s.%s.%s", split[0], split[1], split[2], split[3]);
            int port = (Integer.valueOf(split[4]) << 8) + (Integer.valueOf(split[5]));
            data_sock = new Socket();
            data_sock.connect(new InetSocketAddress(ip, port));
        } catch (IOException e) {
            throw e;
        } catch (Exception ex) {
            throw new JMAPIException("Malformed PASV");
        }
    }

    protected void ConnectDataSocket() throws Exception {
        if (data_sock != null)        // already connected (always so if passive mode)
            return;
        try {
            data_sock = server.accept();    // Accept is blocking
            server.close();
            server = null;
            if (data_sock == null) {
                throw new Exception("Could not establish server");
            }
        } catch (Exception ex) {
            throw new Exception("Failed to connect for data transfer: " + ex.getMessage());
        }
    }

    protected boolean CloseDataSocket() {
        try {
            this.data_sock.close();
            this.data_sock = null;
            this.server = null;
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return false;
    }

    protected Response parseResponse(Response response) {
        if (response.getSuccess()) {
            String res = response.getResponse();
            if (res == null) return response;
            int responseCode = Integer.valueOf(res.substring(0, 3)).intValue();
            String buffer = res.substring(4).replace("\r", "").replace("\n", "");
            if (buffer.contains("OK: ")) {
                buffer = buffer.replace("OK: ", "");
            }
            response.setResponse(buffer);
            response.setResponseCode(findResponse(responseCode));
        }
        return response;
    }

    protected Response Send(String data) {
        try {
            PrintWriter pw = new PrintWriter(client.getOutputStream());
            pw.println(data);
            pw.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String z = br.readLine();
            return parseResponse(Response.Build(true, z));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.Build(false, "Could not send command");
        }
    }

    protected void quickSend(String data) {
        try {
            PrintWriter pw = new PrintWriter(client.getOutputStream());
            pw.println(data);
            pw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected Response quickRead() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String z = br.readLine();
            return parseResponse(Response.Build(true, z));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.Build(false, "Could not send command");
        }
    }


    protected void Close() throws IOException {
        if (client.isConnected()) {
            client.close();
        }
        client = null;
    }
}
