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

public class Main {
    final static String host_MeUTM_md = "me.utm.md";
    final static int port_MeUTM_md = 80;
    final static String host_UTM_md = "utm.md";
    final static int port_UTM_md = 443;

    public static void main(String[] args) throws Exception {

        System.out.println("Choose an option: \n" +
                "1.-> me.utm.md \n" +
                "2.-> utm.md \n" +
                "You choice: ");

        Scanner scanner = new Scanner(System.in);
        int number = scanner.nextInt();
        if (number  == 1)
            requestMeUTM_md();
        else if (number == 2)
            requestUTM_md();
    }

    public static void requestMeUTM_md() throws InterruptedException, IOException {
        String Response = getResponse(host_MeUTM_md, port_MeUTM_md, "/");
        List<String> listOfImg = getImages(Response);
        listOfImg.remove(listOfImg.size() - 1);
        System.out.println("List of images from me.utm.md:\n" + listOfImg +"\n");

        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);
        boolean status = true;
        while (status) {
            for (String image : listOfImg) {
                semaphore.acquire();
                exec.execute(() -> {
                    try {
                        getImg(getNameImages(image, "http://me.utm.md"));
                        semaphore.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName());
                });
                if (image.equals(listOfImg.get(listOfImg.size() - 1))) {
                    status = false;
                    break;
                }
            }
        }
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    public static void requestUTM_md() throws InterruptedException {
        String ResponseSecurised = getResponseSecured(host_UTM_md, port_UTM_md, "/");
        List<String> listImageSecurised= getImages(ResponseSecurised);
        listImageSecurised.remove(0);
        System.out.println("List of images from utm.md:" + listImageSecurised);

        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);
        boolean status = true;
        while (status) {
            for (String element : listImageSecurised) {
                semaphore.acquire();
                exec.execute(() -> {
                    try {
                        getImgagesSecured(getNameImages(element, "https://utm.md"));
                        semaphore.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName());
                });
                if (element.equals(listImageSecurised.get(listImageSecurised.size() - 1))) {
                    status = false;
                    break;
                }
            }
        }
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public static List<String> getImages(String text) {
        String img;
        String regex = "<img.*src\\s*=\\s*(.*?)(jpg|png|gif)[^>]*?>";
        List<String> images = new ArrayList<>();

        Pattern pImage = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher mImage = pImage.matcher(text);

        while (mImage.find()) {
            img = mImage.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                images .add(m.group(1));
            }
        }
        return images ;
    }

    public static String getResponse(String hostName, int port, String getArgument) throws IOException {
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
                .append("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n")
                .append("Accept-Language: ro \r\n")
                .append("Content-Language: en, ase, ru \r\n")
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

    public static String getResponseSecured(String hostName, int port, String getArgument) {
        String serverResponse = "";
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(hostName, port);
            socket.startHandshake();

            PrintWriter outputWriter = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            StringBuilder dataRequest = new StringBuilder();
            dataRequest
                    .append("GET " + getArgument + " HTTP/1.1\r\n")
                    .append("Host: " + hostName + "\r\n")
                    .append("Content-Type: text/html;charset=utf-8 \r\n")
                    .append("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n")
                    .append("Accept-Language: ro \r\n")
                    .append("Content-Language: en, ase, ru \r\n")
                    .append("Vary: Accept-Encoding \r\n")
                    .append("\r\n");

            outputWriter.println(dataRequest);
            outputWriter.flush();

            if (outputWriter.checkError())
                System.out.println(
                        "SSLSocketClient:java.io.PrintWriter error");

            BufferedReader InputReader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            String inputLine;
            while ((inputLine = InputReader.readLine()) != null)
                serverResponse += inputLine + "\n";

            InputReader.close();
            outputWriter.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Secured connection successfully");
        return serverResponse;
    }

    public static String getNameImages(String text, String hostName) {
        String result = null;

        if (text.contains(hostName)) {
            result = text.replace(hostName, "");
            result = result.replace("'", "");
        } else result = text;
        return result;
    }

    private static void getImg(String imgName) throws Exception {
        Socket socket = new Socket(host_MeUTM_md , port_MeUTM_md);
        DataOutputStream bw = new DataOutputStream(socket.getOutputStream());
        bw.writeBytes("GET /" + imgName + " HTTP/1.1\r\n");
        bw.writeBytes("Host: " + host_MeUTM_md  + ":80\r\n");
        bw.writeBytes("Content-Type: text/html;charset=utf-8 \r\n");
        bw.writeBytes("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 \r\n");
        bw.writeBytes("Accept-Language: ro \r\n");
        bw.writeBytes("Content-Language: en, ase, ru \r\n");
        bw.writeBytes("Vary: Accept-Encoding \r\n");
        bw.writeBytes("\r\n");

        bw.flush();

        String[] tokens = imgName.split("/");

        final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        final OutputStream fileOutputStream = new FileOutputStream("images/" + tokens[tokens.length - 1]);

        boolean headerEnded = false;

        byte[] bytes = new byte[2048];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            if (headerEnded)
                fileOutputStream.write(bytes, 0, length);
            else {
                for (int i = 0; i < 2048; i++) {
                    if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                        headerEnded = true;
                        fileOutputStream.write(bytes, i+4, 2048-i-4);
                        break;
                    }
                }
            }
        }
        inputStream.close();
        fileOutputStream.close();

        System.out.println("Status done");

        socket.close();
    }

    private static void getImgagesSecured(String imgName) {
        try {
            SSLSocketFactory factory =
                    (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket =
                    (SSLSocket) factory.createSocket(host_UTM_md , port_UTM_md);
            socket.startHandshake();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            out.println("GET " + imgName + " HTTP/1.1\r\nHost: " + host_UTM_md  + " \r\n\r\n");
            out.flush();

            if (out.checkError())
                System.out.println(
                        "SSLSocketClient:java.io.PrintWriter error");
            String[] tokens = imgName.split("/");

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            OutputStream outputStream  = new FileOutputStream("images/" + tokens[tokens.length - 1]);
            boolean headerEnded = false;

            byte[] bytes = new byte[2048];
            int length;
            while ((length = inputStream.read(bytes)) != -1) {
                if (headerEnded)
                    outputStream.write(bytes, 0, length);
                else {
                    for (int i = 0; i < 2048; i++) {
                        if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                            headerEnded = true;
                            outputStream.write(bytes, i+4, 2048-i-4);
                            break;
                        }
                    }
                }
            }
            inputStream.close();
            outputStream.close();

            System.out.println("Status done");
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

