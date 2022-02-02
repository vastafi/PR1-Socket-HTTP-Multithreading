package out;


import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLTest {
    public URLTest() {
    }

    private static void sendGet() throws Exception {
        String url = "http://me.utm.md/img/logo.png";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        InputStream in = con.getInputStream();
        FileOutputStream out = new FileOutputStream("test.jpg");

        try {
            byte[] bytes = new byte[2048];

            int length;
            while((length = in.read(bytes)) != -1) {
                out.write(bytes, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }

    }

    public static void main(String[] args) throws Exception {
        sendGet();
    }
}
