package out;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketClient {
    public SSLSocketClient() {
    }

    public static void main(String[] args) throws Exception {
        String test = getResponseFromSecurisedServer("utm.md", 443, "/");
        System.out.println(test);
    }

    public static String getResponseFromSecurisedServer(String hostName, int port, String getArgument) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        String serverResponse = "";

        try {
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket)factory.createSocket(hostName, port);
            socket.startHandshake();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.println("GET " + getArgument + " HTTP/1.1\r\nHost: " + hostName + "\r\n\r\n");
            out.flush();
            if (out.checkError()) {
                System.out.println("SSLSocketClient:java.io.PrintWriter error");
            }

            BufferedReader in;
            String inputLine;
            for(in = new BufferedReader(new InputStreamReader(socket.getInputStream())); (inputLine = in.readLine()) != null; serverResponse = serverResponse + inputLine + "\n") {
            }

            in.close();
            out.close();
            socket.close();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        System.out.println("Secured performed successfully");
        return serverResponse;
    }
}
