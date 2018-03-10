package comm;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by YotWei on 2017/12/4.
 * a thread encapsulating socket events
 */

public class ServerSocketThread {

    private ServerEventListener mServerListener;
    private ServerSocket mServerSocket;
    private boolean isRunning = false;

    private int mBindPort = 0;
    private int mStopSocketPort = 0;

    public ServerSocketThread(ServerEventListener listener) {
        this.mServerListener = listener;
    }

    public void start(int mBindPort) throws Exception {
        if (mServerSocket != null) {
            throw new Exception("server socket are running");
        }

        this.mBindPort = mBindPort;
        runOnThread();
    }

    public void stop() throws Exception {
        Socket socket = new Socket("localhost", mBindPort);
        mStopSocketPort = socket.getLocalPort();
        socket.close();
    }

    private void runOnThread() {
        new Thread(() -> {

            try {
                mServerSocket = new ServerSocket(mBindPort);
                isRunning = true;

                if (mServerListener != null) {
                    mServerListener.onServerStart();
                }

                while (isRunning && mServerSocket != null) {

                    Socket socket = mServerSocket.accept();

                    if (isFromServerLocal(socket)) {
                        System.out.println("a socket from local, closing...");
                        isRunning = false;
                        mServerSocket.close();
                        stopServer();
                        socket.close();
                        break;
                    }

                    if (mServerListener != null) {
                        mServerListener.onReceiveSocket(socket);
                    }
                    socket.close();
                }
            } catch (Exception e) {
                if (mServerListener != null) {
                    mServerListener.onException(e);
                }
                e.printStackTrace();
            }
        }).start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void stopServer() {
        mServerSocket = null;
        if (mServerListener != null) {
            mServerListener.onServerClose();
        }
    }

    private boolean isFromServerLocal(Socket socket) {
        return socket.getInetAddress().getHostAddress().equals("127.0.0.1")
                && socket.getPort() == mStopSocketPort;
    }


    public interface ServerEventListener {

        //called when server start
        void onServerStart();

        //called when server received a socket
        void onReceiveSocket(Socket rcvSocket);

        //called when server close
        void onServerClose();

        //called when catch a exception
        void onException(Exception e);
    }
}

