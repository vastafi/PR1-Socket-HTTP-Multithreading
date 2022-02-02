package out;

import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;

public class Client {

    public static void main(String[] args) throws IOException {
        String serverResponse = null;
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        Security.addProvider(new Provider());
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket)sslsocketfactory.createSocket("utm.md", 443);
        InputStream response = sslsocket.getInputStream();
        OutputStream request = sslsocket.getOutputStream();
        byte[] data = "GET / HTTP/1.1\r\nHost: utm.md\r\n\r\n".getBytes();
        request.write(data);
        request.flush();

        while(response.available() > 0) {
            System.out.println(response.read());
            serverResponse = serverResponse + (char)response.read();
            System.out.println("exit");
        }

        sslsocket.close();
        System.out.println("Secured connection successfully");
    }
}