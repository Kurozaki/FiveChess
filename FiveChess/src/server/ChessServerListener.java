package server;

import comm.*;
import util.DataUtil;
import util.IOUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by YotWei on 2017/12/4.
 * server implements of ServerEventListener
 */

class ChessServerListener implements ServerSocketThread.ServerEventListener {

    private ChessServer mChessServer;
    private Map<String, ChessPlayer> onlinePlayers;
    private Queue<ChessPlayer> preparePlayers;
    private Map<String, ChessBattle> chessBattles;

    ChessServerListener(ChessServer mChessServer) {
        this.mChessServer = mChessServer;
        onlinePlayers = new ConcurrentHashMap<>();
        preparePlayers = new ConcurrentLinkedQueue<>();
        chessBattles = new ConcurrentHashMap<>();
    }

    @Override
    public void onServerStart() {
        mChessServer.onStartRunning();
    }

    @Override
    public void onReceiveSocket(Socket rcvSocket) {
        try {

            //read receive data
            byte[] recvData = IOUtil.
                    readBytesFromInputStream(rcvSocket.getInputStream());
            ChessProtocol protocol = ChessProtocol.decode(recvData);

            //shutdown input stream
            rcvSocket.shutdownInput();

            switch (protocol.getType()) {

                case ChessProtocol.TYPE_LOGIN:
                    doLogin(rcvSocket, protocol.getData());
                    break;

                case ChessProtocol.TYPE_LOGOUT:
                    doLogout(rcvSocket, protocol.getData());
                    break;

                case ChessProtocol.TYPE_CHESS_TURN:
                    doChessTurn(rcvSocket, protocol.getData());
                    break;

                case ChessProtocol.TYPE_CHAT:
                    doChat(rcvSocket, protocol.getData());
                    break;

                case ChessProtocol.TYPE_GAME_PREPARE:
                    doPlayerPrepare(rcvSocket, protocol.getData());
                    break;

                default:
                    //type error
                    doTypeError(rcvSocket);
            }

//            System.out.println("online count: " + onlinePlayers.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerClose() {
        mChessServer.onStopRunning();
    }

    @Override
    public void onException(Exception e) {
        mChessServer.addTextInfoLine(e.getMessage(), 2);
    }

    /**
     * deal with logout request
     *
     * @param socket   received socket
     * @param bodyData request body data
     */
    private void doLogin(Socket socket, byte[] bodyData) throws IOException {
        byte[] resp;

        //decode login data
        InetAddress address = InetAddress.getByAddress(Arrays.copyOf(bodyData, 4));
        int port = DataUtil.byteArray2Int(Arrays.copyOfRange(bodyData, 4, 8));
        String nickname = new String(Arrays.copyOfRange(bodyData, 8, bodyData.length));

//        System.out.println(address.getHostAddress() + ":" + port);
        if (onlinePlayers.containsKey(nickname)) {
            mChessServer.addTextInfoLine(nickname + "尝试重复登录",
                    TextHintPrefixes.TEXT_TYPE_WARNING);
            resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_ERROR};
        } else {
            mChessServer.addTextInfoLine(nickname + "已登录",
                    TextHintPrefixes.TEXT_TYPE_INFO);
            ChessPlayer player = new ChessPlayer(address, port, nickname);
            onlinePlayers.put(nickname, player);
            resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_OK};
        }

        if (!socket.isInputShutdown())
            socket.shutdownInput();

        //write response data
        OutputStream os = socket.getOutputStream();
        os.write(resp);
        os.flush();
    }

    /**
     * deal with logout request
     */
    private void doLogout(Socket socket, byte[] bodyData) throws IOException {
        byte[] resp;
        //get nickname
        String nickname = new String(bodyData);

        if (!onlinePlayers.containsKey(nickname)) {
            resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_ERROR};
        } else {
            //remove from online list and set it offline
            ChessPlayer logoutPlayer = onlinePlayers.remove(nickname);
//            logoutPlayer.offLine();
            preparePlayers.remove(logoutPlayer);
            onlinePlayers.remove(logoutPlayer.getNickname());

            forceEndGame(logoutPlayer);
            resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_OK};

            mChessServer.addTextInfoLine(nickname + "已断开连接", TextHintPrefixes.TEXT_TYPE_INFO);
        }
        OutputStream os = socket.getOutputStream();
        os.write(resp);
        os.flush();
    }

    /**
     * chess turn from a player
     */
    private void doChessTurn(Socket socket, byte[] data) throws IOException {
        byte posX = data[0];
        byte posY = data[1];
        String battleId = new String(Arrays.copyOfRange(data, 2, 26));
        String nickname = new String(Arrays.copyOfRange(data, 26, data.length));


        if (!chessBattles.containsKey(battleId)) {
            System.out.println("battle not exist: " + battleId);
            return;
        }

        ChessBattle battle = chessBattles.get(battleId);
        boolean add = battle.addChess(nickname, posX, posY);

        if (add) {
            socket.getOutputStream().write(new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_OK});
            socket.close();

            mChessServer.addTextInfoLine(nickname + "{" + posX + ", " + posY + "}",
                    TextHintPrefixes.TEXT_TYPE_INFO);

            ChessPlayer player = battle.getChatPlayer(nickname);
            sendSocketToPlayer(player, ChessProtocol.encode(ChessProtocol.TYPE_CHESS_TURN, new byte[]{posX, posY}));

            //do [game over]
            if (battle.isGameOver()) {
                mChessServer.addTextInfoLine(nickname + " win", TextHintPrefixes.TEXT_TYPE_INFO);

                sendSocketToPlayer(battle.getPlayer1(),
                        ChessProtocol.encode(ChessProtocol.TYPE_GAME_END, nickname.getBytes()));
                sendSocketToPlayer(battle.getPlayer2(),
                        ChessProtocol.encode(ChessProtocol.TYPE_GAME_END, nickname.getBytes()));

                battle.getPlayer1().setStatus(ChessPlayer.Status.ONLINE);
                battle.getPlayer2().setStatus(ChessPlayer.Status.ONLINE);

                chessBattles.remove(battleId);
            }

        } else {
            socket.getOutputStream().write(new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_ERROR});
            socket.close();
        }
    }


    /**
     * deal with player send message
     */
    private void doChat(Socket socket, byte[] data) throws IOException {
        byte battleIdLength = data[0];
        byte nicknameLength = data[battleIdLength + 1];
        byte messageLength = data[battleIdLength + nicknameLength + 2];

        String battleId = new String(Arrays.copyOfRange(data, 1, battleIdLength + 1));
        String nickname = new String(Arrays.copyOfRange(data, battleIdLength + 2, battleIdLength + 2 + nicknameLength));
        String message = new String(Arrays.copyOfRange(data, battleIdLength + 3 + nicknameLength,
                battleIdLength + 3 + nicknameLength + messageLength));

        if (!chessBattles.containsKey(battleId)) {
            mChessServer.addTextInfoLine("不存在battleId: " + battleId, TextHintPrefixes.TEXT_TYPE_INFO);
            return;
        }

//        System.out.println("battle=" + battleId + ", " + nickname + ": " + message);
        ChessBattle battle = chessBattles.get(battleId);
        ChessPlayer player = battle.getChatPlayer(nickname);
        sendSocketToPlayer(player, ChessProtocol.encode(ChessProtocol.TYPE_STRING_MSG, message.getBytes()));
    }

    /**
     * player prepare
     */
    private void doPlayerPrepare(Socket socket, byte[] data) throws IOException {
        byte[] resp;
        String nickname = new String(data);

        if (onlinePlayers.containsKey(nickname)) {
            ChessPlayer player = onlinePlayers.get(nickname);
            player.setStatus(ChessPlayer.Status.PREPARE);

            if (!preparePlayers.contains(player)) {
                if (preparePlayers.size() > 0) {

                    //add new battle
                    String battleId = DataUtil.generateRandomString();
                    ChessBattle newBattle = initBattle(preparePlayers.poll(), player, battleId);
                    chessBattles.put(battleId, newBattle);
                } else {
                    preparePlayers.add(player);
                }

                resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_OK};
                mChessServer.addTextInfoLine(nickname + "已准备", TextHintPrefixes.TEXT_TYPE_INFO);
            } else {
                mChessServer.addTextInfoLine(nickname + "重复准备",
                        TextHintPrefixes.TEXT_TYPE_WARNING);
                resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_ERROR};
            }
        } else {
            mChessServer.addTextInfoLine(nickname + "不在线", TextHintPrefixes.TEXT_TYPE_ERROR);
            resp = new byte[]{ChessProtocol.TYPE_SER_RESP, RESP_STATUS_ERROR};
        }

        socket.getOutputStream().write(resp);
        socket.getOutputStream().flush();
    }

    /**
     * to type error
     *
     * @param socket received socket
     */
    private void doTypeError(Socket socket) throws IOException {
        if (!socket.isInputShutdown())
            socket.shutdownInput();
        socket.getOutputStream().write(new byte[]{
                ChessProtocol.TYPE_SER_RESP,
                RESP_STATUS_TYPE_ERROR}
        );
    }

    private final static byte RESP_STATUS_OK = 1;
    private final static byte RESP_STATUS_ERROR = 2;
    private final static byte RESP_STATUS_TYPE_ERROR = 3;

    private ChessBattle initBattle(ChessPlayer p1, ChessPlayer p2, String battleId) throws IOException {
        System.out.println("{" + p1.getNickname() + " and " + p2.getNickname()
                + " battle!\nbattle Id = " + battleId + "}");

        p1.setStatus(ChessPlayer.Status.IN_GAME);
        p2.setStatus(ChessPlayer.Status.IN_GAME);

        byte[] p1Data = ChessProtocol.encode(ChessProtocol.TYPE_GAME_START,
                ChessProtocol.buildStartGameData(battleId, true));
        sendSocketToPlayer(p1, p1Data);

        byte[] p2Data = ChessProtocol.encode(ChessProtocol.TYPE_GAME_START,
                ChessProtocol.buildStartGameData(battleId, false));
        sendSocketToPlayer(p2, p2Data);

        return new ChessBattle(p1, p2);
    }

    private void forceEndGame(ChessPlayer offlinePlayer) throws IOException {
        for (Map.Entry<String, ChessBattle> e : chessBattles.entrySet()) {

            if (e.getValue().getPlayer1().getNickname().equals(offlinePlayer.getNickname()) ||
                    e.getValue().getPlayer2().getNickname().equals(offlinePlayer.getNickname())) {

                ChessPlayer oppoPlayer = e.getValue().getChatPlayer(offlinePlayer.getNickname());
//                preparePlayers.remove(oppoPlayer);
                sendSocketToPlayer(oppoPlayer, ChessProtocol.encode(
                        ChessProtocol.TYPE_GAME_RESET, "对方已离线".getBytes()
                ));
                chessBattles.remove(e.getKey());
                break;
            }
        }
        System.out.println("battle:" + chessBattles);
        System.out.println("online:" + onlinePlayers);
        System.out.println("pre:" + preparePlayers);
    }

    /**
     * send a socket to a player
     *
     * @param player       player
     * @param protocolData 发送内容，包含协议全部信息
     * @throws IOException io exception
     */
    private void sendSocketToPlayer(ChessPlayer player, byte[] protocolData)
            throws IOException {
        Socket socket = new Socket(player.getAddress(), player.getPort());
        OutputStream os = socket.getOutputStream();
        os.write(protocolData);
        os.flush();
        socket.close();
    }
}
