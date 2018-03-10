package client;

import comm.ChessProtocol;
import comm.ServerSocketThread;
import comm.TextHintPrefixes;
import util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by YotWei on 2017/12/5.
 * client socket event listener
 */
class ClientListener implements ServerSocketThread.ServerEventListener {

    private ClientUIEvents uiEvents;

    ClientListener(ClientUIEvents uiEvents) {
        this.uiEvents = uiEvents;
    }

    @Override
    public void onServerStart() {
        uiEvents.setBindPortEnable(false);
        uiEvents.addTextInfo("启动成功", TextHintPrefixes.TEXT_TYPE_INFO);
    }

    @Override
    public void onReceiveSocket(Socket rcvSocket) {
        try {
            InputStream is = rcvSocket.getInputStream();
            byte[] data = IOUtil.readBytesFromInputStream(is);
            ChessProtocol protocol = ChessProtocol.decode(data);

            switch (protocol.getType()) {
                case ChessProtocol.TYPE_GAME_START:
                    uiEvents.doReceiveGameStartSocket(protocol.getData());
                    break;

                case ChessProtocol.TYPE_GAME_END:
                    uiEvents.doReceiveGameEndSocket(protocol.getData());
                    break;

                case ChessProtocol.TYPE_CHESS_TURN:
                    uiEvents.doReceiveChessTurnSocket(protocol.getData());
                    break;

                case ChessProtocol.TYPE_STRING_MSG:
                    uiEvents.doReceiveChatInfoSocket(protocol.getData());
                    break;

                case ChessProtocol.TYPE_GAME_RESET:
                    uiEvents.doResetGame(protocol.getData());
                    break;

                default:
                    //error type
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerClose() {
        uiEvents.setBindPortEnable(true);
        System.out.println("local close");
    }

    @Override
    public void onException(Exception e) {
        uiEvents.addTextInfo(e.getMessage(), TextHintPrefixes.TEXT_TYPE_ERROR);
    }
}
