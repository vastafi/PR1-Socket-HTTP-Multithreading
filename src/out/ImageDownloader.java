package out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class ImageDownloader {

        public static void getAndWrite(String urlString, File path) throws IOException {
        String var10000 = path.toPath().getParent().toString();
        String toWriteTo = var10000 + System.getProperty("file.separator");
        URL url = new URL(urlString);
        Socket socket = new Socket(url.getHost(), 80);
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("GET " + url.getPath() + " HTTP/1.1");
        pw.println("Host: " + url.getHost());
        pw.println();
        pw.flush();
        FileOutputStream fileOutputStream = new FileOutputStream(toWriteTo + url.getPath().replaceAll(".*/", ""));
        InputStream inputStream = socket.getInputStream();
        boolean headerEnded = false;
        byte[] bytes = new byte[2048];

        while(true) {
            int length;
            while((length = inputStream.read(bytes)) != -1) {
                if (headerEnded) {
                    fileOutputStream.write(bytes, 0, length);
                } else {
                    for(int i = 0; i < 2048; ++i) {
                        if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                            headerEnded = true;
                            fileOutputStream.write(bytes, i + 4, 2048 - i - 4);
                            break;
                        }
                    }
                }
            }

            inputStream.close();
            fileOutputStream.close();
            System.out.println(toWriteTo + url.getPath().replaceAll(".*/", ""));
            return;
        }
    }
}