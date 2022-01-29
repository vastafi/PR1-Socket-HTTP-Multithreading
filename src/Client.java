import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    final static String HOST_NAME1 = "me.utm.md";
    final static int PORT1 = 80;
    final static String HOST_NAME2 = "utm.md";
    final static int PORT2 = 443;

    public static void main(String[] args) throws Exception {

        System.out.println("Enter 1 for me.utm.md or 2 for utm.md:");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        if (choice == 1)
            requestToMeUtmMD();
        else if (choice == 2)
            requestToUtmMD();
    }

    public static void  requestToMeUtmMD() throws InterruptedException, IOException {
        String serverResponse = getResponseFromServer(HOST_NAME1, PORT1, "/");
        List<String> listOfImg = getPics(serverResponse);
        listOfImg.remove(listOfImg.size() - 1);
        listOfImg.remove(listOfImg.size() - 1);
        System.out.println("List of images from me.utm.md:" + listOfImg);

        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);
        boolean status = true;
        while (status) {
            for (String element : listOfImg) {
                semaphore.acquire();
                exec.execute(() -> {
                    try {
                        getImg(getRealNameOfPicture(element, "http://mib.utm.md"));
                        semaphore.release();
                    } catch (Exception e) {
                        e.printStackTrace();
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
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    public static void requestToUtmMD() throws InterruptedException {
        String serverResponseSecurised = getResponseFromSecurisedServer(HOST_NAME2, PORT2, "/");
        List<String> listOfImgSecurised = getPics(serverResponseSecurised);
        listOfImgSecurised.remove(0);
        listOfImgSecurised.remove(0);
        listOfImgSecurised.remove(0);
        System.out.println("List of images from utm.md:" + listOfImgSecurised);

        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);
        boolean status = true;
        while (status) {
            for (String element : listOfImgSecurised) {
                semaphore.acquire();
                exec.execute(() -> {
                    try {
                        getImgS(getRealNameOfPicture(element, "https://utm.md"));
                        semaphore.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName());
                });
                if (element.equals(listOfImgSecurised.get(listOfImgSecurised.size() - 1))) {
                    status = false;
                    break;
                }
            }
        }
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public static List<String> getPics(String text) {
        String img;
        String regex = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        List<String> pics = new ArrayList<>();

        Pattern pImage = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher mImage = pImage.matcher(text);

        while (mImage.find()) {
            img = mImage.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }

    public static String getResponseFromServer(String hostName, int port, String getArgument) throws IOException {
        String serverResponse = null;
        int c;

        Socket socket = new Socket(hostName, port);

        InputStream response = socket.getInputStream();
        OutputStream request = socket.getOutputStream();

        StringBuilder dataRequest = new StringBuilder();
        dataRequest
                .append("GET " + getArgument + " HTTP/1.1\r\n")
                .append("Host: " + hostName + "\r\n")
                .append("Content-Type: text/html;charset=utf-8 \r\n")
                .append("Accept-Language: ro \r\n")
                .append("Content-Language: en, ase, ru \r\n")
                .append("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n")
                .append("Vary: Accept-Encoding \r\n")
                .append("\r\n");

        byte[] data = (dataRequest.toString()).getBytes();
        request.write(data);

        while ((c = response.read()) != -1) {
            serverResponse += (char) c;
        }
        socket.close();

        return serverResponse;
    }

    public static String getResponseFromSecurisedServer(String hostName, int port, String getArgument) {
        String serverResponse = "";
        try {
            SSLSocketFactory factory =
                    (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket =
                    (SSLSocket) factory.createSocket(hostName, port);
            socket.startHandshake();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            StringBuilder dataRequest = new StringBuilder();
            dataRequest
                    .append("GET " + getArgument + " HTTP/1.1\r\n")
                    .append("Host: " + hostName + "\r\n")
                    .append("Content-Type: text/html;charset=utf-8 \r\n")
                    .append("Accept-Language: ro \r\n")
                    .append("Content-Language: en, ase, ru \r\n")
                    .append("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n")
                    .append("Vary: Accept-Encoding \r\n")
                    .append("\r\n");

            out.println(dataRequest);
            out.flush();

            if (out.checkError())
                System.out.println(
                        "SSLSocketClient:java.io.PrintWriter error");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                serverResponse += inputLine + "\n";

            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Secured connection successfully");
        return serverResponse;
    }

    public static String getRealNameOfPicture(String text, String hostName) {
        String result = null;

        if (text.contains(hostName)) {
            result = text.replace(hostName, "");
            result = result.replace("'", "");
        } else result = text;
        return result;
    }

    private static void getImg(String imgName) throws Exception {
        Socket socket = new Socket(HOST_NAME1, PORT1);
        DataOutputStream bw = new DataOutputStream(socket.getOutputStream());
        bw.writeBytes("GET /" + imgName + " HTTP/1.1\r\n");
        bw.writeBytes("Host: " + HOST_NAME1 + ":80\r\n");
        bw.writeBytes("Content-Type: text/html;charset=utf-8 \r\n");
        bw.writeBytes("Accept-Language: ro \r\n");
        bw.writeBytes("Content-Language: en, ase, ru \r\n");
        bw.writeBytes("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n");
        bw.writeBytes("Vary: Accept-Encoding \r\n");
        bw.writeBytes("\r\n");

        bw.flush();

        String[] tokens = imgName.split("/");

        DataInputStream in = new DataInputStream(socket.getInputStream());
        OutputStream dos = new FileOutputStream("images/" + tokens[tokens.length - 1]);

        int count, offset;
        byte[] buffer = new byte[2048];
        boolean eohFound = false;
        while ((count = in.read(buffer)) != -1) {
            offset = 0;
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

    private static void getImgS(String imgName) {
        try {
            SSLSocketFactory factory =
                    (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket =
                    (SSLSocket) factory.createSocket(HOST_NAME2, PORT2);
            socket.startHandshake();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            out.println("GET " + imgName + " HTTP/1.1\r\nHost: " + HOST_NAME2 + " \r\n\r\n");
            out.flush();

            if (out.checkError())
                System.out.println(
                        "SSLSocketClient:java.io.PrintWriter error");
            String[] tokens = imgName.split("/");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            OutputStream dos = new FileOutputStream("images/" + tokens[tokens.length - 1]);

            int count, offset;
            byte[] buffer = new byte[2048];
            boolean eohFound = false;
            while ((count = in.read(buffer)) != -1) {
                offset = 0;
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

