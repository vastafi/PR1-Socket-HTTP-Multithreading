package out;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class AnotherTask {
    static final String HOST_NAME = "me.utm.md";
    static final int PORT = 80;

    public AnotherTask() {
    }

    public static void main(String[] args) throws Exception {
        String serverResponseSecurised = getResponseFromSecurisedServer("utm.md", 443, "/");
        System.out.println(serverResponseSecurised);
        List<String> listOfImg = getPics(serverResponseSecurised);
        System.out.println(listOfImg);
        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);
        boolean status = true;

        while(true) {
            while(status) {
                Iterator var6 = listOfImg.iterator();

                while(var6.hasNext()) {
                    String element = (String)var6.next();
                    semaphore.acquire();
                    exec.execute(() -> {
                        try {
                            getImgS(getRealNameOfPictureUTM(element));
                            semaphore.release();
                        } catch (Exception var3) {
                            var3.printStackTrace();
                        }

                        System.out.println(Thread.currentThread().getName());
                    });
                    if (element.equals(listOfImg.get(listOfImg.size() - 1))) {
                        status = false;
                        break;
                    }
                }
            }

            exec.shutdown();
            exec.awaitTermination(9223372036854775807L, TimeUnit.MILLISECONDS);
            return;
        }
    }

    public static List<String> getPics(String text) {
        String regex = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        List<String> pics = new ArrayList();
        Pattern pImage = Pattern.compile(regex, 2);
        Matcher mImage = pImage.matcher(text);

        while(mImage.find()) {
            String img = mImage.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);

            while(m.find()) {
                pics.add(m.group(1));
            }
        }

        return pics;
    }

    public static String getResponseFromServer(String hostName, int port, String getArgument) throws IOException {
        String serverResponse = null;
        Socket socket = new Socket(hostName, port);
        InputStream response = socket.getInputStream();
        OutputStream request = socket.getOutputStream();
        byte[] data = ("GET " + getArgument + " HTTP/1.1\nHost: " + hostName + "\n\n").getBytes();
        request.write(data);

        int c;
        while((c = response.read()) != -1) {
            serverResponse = serverResponse + (char)c;
        }

        socket.close();
        return serverResponse;
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
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");
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

        System.out.println("Secured connection successfully");
        return serverResponse;
    }

    public static String getRealNameOfPicture(String text) {
        String result = null;
        if (text.contains("http://mib.utm.md")) {
            result = text.replace("http://mib.utm.md", "");
            result = result.replace("'", "");
        } else {
            result = text;
        }

        return result;
    }

    public static String getRealNameOfPictureUTM(String text) {
        String result = null;
        if (text.contains("https://utm.md")) {
            result = text.replace("https://utm.md", "");
            result = result.replace("'", "");
        } else {
            result = text;
        }

        return result;
    }

    private static void getImg(String imgName) throws Exception {
        Socket socket = new Socket("me.utm.md", 80);
        DataOutputStream bw = new DataOutputStream(socket.getOutputStream());
        bw.writeBytes("GET /" + imgName + " HTTP/1.1\r\n");
        bw.writeBytes("Host: me.utm.md:80\r\n\r\n");
        bw.flush();
        String[] tokens = imgName.split("/");
        DataInputStream in = new DataInputStream(socket.getInputStream());
        OutputStream dos = new FileOutputStream("images/" + tokens[tokens.length - 1]);
        byte[] buffer = new byte[2048];
        boolean eohFound = false;

        int count;
        while((count = in.read(buffer)) != -1) {
            int offset = 0;
            if (!eohFound) {
                String string = new String(buffer, 0, count);
                int indexOfEOH = string.indexOf("\r\n\r\n");
                if (indexOfEOH != -1) {
                    count = count - indexOfEOH - 4;
                    offset = indexOfEOH + 4;
                    eohFound = true;
                } else {
                    count = 0;
                }
            }

            dos.write(buffer, offset, count);
            dos.flush();
        }

        in.close();
        dos.close();
        System.out.println("Transfer for image: status done");
        socket.close();
    }

    private static void getImgS(String imgName) throws Exception {
        try {
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket)factory.createSocket("utm.md", 443);
            socket.startHandshake();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.println("GET " + imgName + " HTTP/1.1\r\nHost: utm.md \r\n\r\n");
            out.flush();
            if (out.checkError()) {
                System.out.println("SSLSocketClient:  java.io.PrintWriter error");
            }

            String[] tokens = imgName.split("/");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            OutputStream dos = new FileOutputStream("images/" + tokens[tokens.length - 1]);
            byte[] buffer = new byte[2048];
            boolean eohFound = false;

            int count;
            while((count = in.read(buffer)) != -1) {
                int offset = 0;
                if (!eohFound) {
                    String string = new String(buffer, 0, count);
                    int indexOfEOH = string.indexOf("\r\n\r\n");
                    if (indexOfEOH != -1) {
                        count = count - indexOfEOH - 4;
                        offset = indexOfEOH + 4;
                        eohFound = true;
                    } else {
                        count = 0;
                    }
                }

                dos.write(buffer, offset, count);
                dos.flush();
            }

            in.close();
            dos.close();
            System.out.println("Transfer for image: status done");
            socket.close();
        } catch (Exception var13) {
            var13.printStackTrace();
        }

    }
}
