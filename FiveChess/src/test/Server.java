package test;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by YotWei on 2017/12/4.
 * a test class for server
 */

public class Server {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        int port = scanner.nextInt();

        ServerSocket server = new ServerSocket(port);
        System.out.println("server start");
        System.out.println("listening...");
        while (true) {
            Socket socket = server.accept();
            System.out.println("socket receive:\n");

            byte[] buffer = new byte[4];
            while (socket.getInputStream().read(buffer) != -1) {
                System.out.println(Arrays.toString(buffer));
            }
            socket.shutdownInput();
            socket.getOutputStream().write("hello".getBytes());
            socket.getOutputStream().flush();
            socket.close();
        }
    }
}
