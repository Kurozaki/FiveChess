package test;

import comm.ChessProtocol;
import org.junit.Test;
import util.DataUtil;
import util.IOUtil;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by YotWei on 2017/12/4.
 * test demo, can be delete
 */

public class Demo {

    public static void main(String[] args) throws Exception {
//        Socket socket = new Socket("115.159.123.176", 8888);

//        Socket socket = new Socket("localhost", 8888);
//
//        byte[] chatData = ChessProtocol.buildChatData("kurozaki", "hello world!");
//        byte[] encode = ChessProtocol.encode(ChessProtocol.TYPE_CHAT, chatData);
//        socket.getOutputStream().write(encode);
//
//        socket.close();
//        System.out.println("success");
    }

    @Test
    public void test() throws Exception {
        byte[] bytes = ChessProtocol.buildChatData("48h4yr78yh48", "kurozaki", "hello world");
        System.out.println(Arrays.toString(bytes));
        System.out.println(new String(bytes));
    }
}
