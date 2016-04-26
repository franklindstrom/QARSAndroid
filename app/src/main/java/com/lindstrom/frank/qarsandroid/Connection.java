package com.lindstrom.frank.qarsandroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Connection {

    public static String getResponse(String connection_url, String request_params) {
        try {
            URL url = new URL(connection_url);

            System.out.println(connection_url);

            byte[] outputInBytes = request_params.getBytes();

            //create the connection
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(outputInBytes.length));

            // send request
            connection.getOutputStream().write(outputInBytes);
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.connect();

            if (connection.getResponseCode() != 200) {
                return "failed";
            } else {
                return convertInputStreamToString(connection.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPostResponse(String server_url, String param) {
        try {
            URL url = new URL(server_url);

            byte[] outputInBytes = param.getBytes("UTF-8");

            //create the connection
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(outputInBytes.length));

            // send request
            connection.getOutputStream().write(outputInBytes);
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.connect();

            if (connection.getResponseCode() != 200) {
                return "failed";
            } else {
                return convertInputStreamToString(connection.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // connect to soap server
    public static String connectToSoapServer(String server_url, int uid, String param) {
        String soap_action = "http://ws.qars/SetCommand";
        String soap_str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<soap:Body>\n" +
                "<ns2:SetCommand xmlns:ns2=\"http://ws.qars/\">\n" +
                "<uid>" + uid + "</uid>\n" +
                "<comm>" + param + "</comm>\n" +
                "</ns2:SetCommand>\n" +
                "</soap:Body>\n" +
                "</soap:Envelope>\n";
        try {
            // server url
            URL url = new URL(server_url);

            byte[] outputInBytes = soap_str.getBytes("UTF-8");

            // Open a connection using HttpURLConnection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            con.setRequestProperty("SOAPAction", soap_action);
            con.setRequestProperty("Content-Length", String.valueOf(outputInBytes.length));

            // send request
            con.getOutputStream().write(outputInBytes);
            con.getOutputStream().flush();
            con.getOutputStream().close();
            con.connect();

            if (con.getResponseCode() != 200) {
                return "failed";
            } else {
                return convertInputStreamToString(con.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // convert input stream of connection to string
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) result += line;
        inputStream.close();
        System.out.println(result);
        return result;
    }
}
