package out;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Task {
    public Task() {
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress("me.utm.md", 80), 2000);
            Scanner scanner = new Scanner(socket.getInputStream());

            while(scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        } catch (Throwable var5) {
            try {
                socket.close();
            } catch (Throwable var4) {
                var5.addSuppressed(var4);
            }

            throw var5;
        }

        socket.close();
    }
}
