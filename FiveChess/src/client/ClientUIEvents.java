package client;

import comm.*;
import util.DataUtil;

import javax.swing.*;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by YotWei on 2017/12/5.
 * client ui events
 */
public class ClientUIEvents {

    ClientUIEvents(ChessClient client) {
        this.mChessClient = client;

        this.mChessClient.getChessPanel().setUI(new ChessPanelUI());

        this.mChessClient.getConnButton().addActionListener(this::doConnectButtonAction);
        this.mChessClient.getSendMessageButton().addActionListener(this::doSendMessageButtonAction);
        this.mChessClient.getServerStartBtn().addActionListener(this::doServerStartAction);
        this.mChessClient.getStartGameBtn().addActionListener(this::doStartGameAction);
        this.mChessClient.getChessPanel().addMouseListener(new ChessMouseClickListener());

        this.mChessClient.getTextInfoArea().setEditable(false);
        this.mChessClient.getTextInfoArea().setLineWrap(true);

        //test
        this.mChessClient.getNicknameTextField().setText(DataUtil.randomNickname());
    }

    private ChessClient mChessClient;
    private ServerSocketThread mServerSocketThread;

    private int serverPort = 0;
    private boolean isOnline = false;

    //player info
    private boolean isBlack = false;
    private boolean isMyTurn = false;
    private String battleId = null;
    private int[][] chessGrid = new int[ChessJudgment.LINE_GRID_COUNT - 1][ChessJudgment.LINE_GRID_COUNT - 1];

    private void resetGame() {
        isBlack = false;
        isMyTurn = false;
        battleId = null;
        chessGrid = new int[ChessJudgment.LINE_GRID_COUNT - 1][ChessJudgment.LINE_GRID_COUNT - 1];
    }

    // ---------- UI events begin----------

    private void doStartGameAction(ActionEvent event) {
        //send a prepare socket
        byte[] encode = ChessProtocol.encode(ChessProtocol.TYPE_GAME_PREPARE,
                mChessClient.getNicknameTextField().getText().getBytes());

        String hostname = mChessClient.getHostTextField().getText();
        String portText = mChessClient.getPortTextField().getText();

        try {
            Socket socket = new Socket(hostname, Integer.parseInt(portText));
            socket.getOutputStream().write(encode);
            socket.getOutputStream().flush();
            socket.shutdownOutput();

            //get response
            byte[] resp = new byte[2];
            socket.getInputStream().read(resp);
            if (resp[0] == ChessProtocol.TYPE_SER_RESP && resp[1] == 1) {
                addTextInfo("已准备", TextHintPrefixes.TEXT_TYPE_INFO);
                mChessClient.getStartGameBtn().setEnabled(false);
            }

            socket.close();
        } catch (IOException e) {
            addTextInfo(e.getMessage(), TextHintPrefixes.TEXT_TYPE_ERROR);
            e.printStackTrace();
        }
    }

    private void doServerStartAction(ActionEvent event) {
        String bindPortText = mChessClient.getServerPort().getText();
        if (!bindPortText.matches("^\\d+$")) {
            addTextInfo("端口错误", TextHintPrefixes.TEXT_TYPE_ERROR);
            return;
        }

        serverPort = Integer.parseInt(bindPortText);
        mServerSocketThread = new ServerSocketThread(new ClientListener(this));
        try {
            mServerSocketThread.start(serverPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doConnectButtonAction(ActionEvent event) {
        if (!isOnline) {
            playerLogin();
        } else {
            playerLogout();
        }
    }

    private void doSendMessageButtonAction(ActionEvent event) {

        if (battleId == null) {
            addTextInfo("尚未开局，还不能聊天", TextHintPrefixes.TEXT_TYPE_INFO);
            return;
        }

        String sendMessage = mChessClient.getInputMessageTextField().getText();
        if (sendMessage.length() == 0) {
            addTextInfo("发送消息不得为空", TextHintPrefixes.TEXT_TYPE_ERROR);
            return;
        }

        String nickname = mChessClient.getNicknameTextField().getText();
        String hostname = mChessClient.getHostTextField().getText();
        String portText = mChessClient.getPortTextField().getText();

        try {
            Socket socket = new Socket(hostname, Integer.parseInt(portText));
            byte[] sendData = ChessProtocol.encode(ChessProtocol.TYPE_CHAT,
                    ChessProtocol.buildChatData(battleId, nickname, sendMessage));
            socket.getOutputStream().write(sendData);
            socket.getOutputStream().flush();
            socket.close();

            //add text
            addTextInfo("我: " + sendMessage, TextHintPrefixes.TEXT_TYPE_NONE);
            mChessClient.getInputMessageTextField().setText("");
        } catch (IOException e) {
            addTextInfo(e.getMessage(), TextHintPrefixes.TEXT_TYPE_ERROR);
            e.printStackTrace();
        }
    }

    public void doWindowClosingAction(WindowEvent event) {
        try {
            //logout first
            if (mServerSocketThread != null) {
                playerLogout();
                mServerSocketThread.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- UI events end----------


    // ---------- util methods ----------

    public void addTextInfo(String info, int type) {
        String addText = TextHintPrefixes.textPrefix(type) + " " + info + "\n";
        mChessClient.getTextInfoArea().append(addText);
        mChessClient.getTextInfoArea().setCaretPosition(
                mChessClient.getTextInfoArea().getDocument().getLength());
    }

    public void setBindPortEnable(boolean enable) {
        mChessClient.getServerPort().setEnabled(enable);
        mChessClient.getServerStartBtn().setEnabled(enable);
    }

    private void setConnectInfoEditable(boolean enable) {
        mChessClient.getConnButton().setText(enable ? "连接" : "断开");
        mChessClient.getHostTextField().setEnabled(enable);
        mChessClient.getPortTextField().setEnabled(enable);
        mChessClient.getNicknameTextField().setEnabled(enable);
    }

    // ---------- util methods end ----------


    // ---------- general methods  ----------

    private void playerLogin() {
        if (mServerSocketThread == null || !mServerSocketThread.isRunning()) {
            addTextInfo("未开启服务", TextHintPrefixes.TEXT_TYPE_WARNING);
            return;
        }

        String nickname = mChessClient.getNicknameTextField().getText();
        String hostName = mChessClient.getHostTextField().getText();
        String portText = mChessClient.getPortTextField().getText();
        if (!nickname.matches("^\\w{8,16}$")) {
            addTextInfo("ID格式错误，请使用8-16位字母或数字作为ID", TextHintPrefixes.TEXT_TYPE_WARNING);
            return;
        }
        if (!portText.matches("^\\d+$")) {
            addTextInfo("连接端口错误", TextHintPrefixes.TEXT_TYPE_ERROR);
            return;
        }
        int port = Integer.parseInt(portText);
        if (port == serverPort) {
            addTextInfo("本地端口冲突", TextHintPrefixes.TEXT_TYPE_ERROR);
            return;
        }

        try {
            Socket socket = new Socket(hostName, port);
            socket.setSoTimeout(1000);

            byte[] outputData = ChessProtocol.encode(
                    ChessProtocol.TYPE_LOGIN,
                    ChessProtocol.buildLoginData(InetAddress.getLocalHost().getAddress(),
                            serverPort, nickname));
            socket.getOutputStream().write(outputData);
            socket.getOutputStream().flush();
            socket.shutdownOutput();

            byte[] resp = new byte[2];
            socket.getInputStream().read(resp);
            socket.close();

            //这里的数字改成常量比较好
            if (resp[0] == ChessProtocol.TYPE_SER_RESP && resp[1] == 1) {
                addTextInfo("登录成功", TextHintPrefixes.TEXT_TYPE_INFO);

                //do success
                setConnectInfoEditable(false);
                isOnline = true;
            } else {
                addTextInfo("登录失败", TextHintPrefixes.TEXT_TYPE_INFO);
            }
        } catch (IOException e) {
            addTextInfo(e.getMessage(), TextHintPrefixes.TEXT_TYPE_ERROR);
            e.printStackTrace();
        }
    }

    //send a logout socket
    private void playerLogout() {
        String nickname = mChessClient.getNicknameTextField().getText();
        String hostName = mChessClient.getHostTextField().getText();
        String portText = mChessClient.getPortTextField().getText();

        try {
            Socket socket = new Socket(hostName, Integer.parseInt(portText));
            socket.setSoTimeout(1000);

            socket.getOutputStream().write(ChessProtocol.encode(
                    ChessProtocol.TYPE_LOGOUT,
                    nickname.getBytes()
            ));
            socket.getOutputStream().flush();
            socket.shutdownOutput();

            byte[] resp = new byte[2];
            socket.getInputStream().read(resp);
            socket.close();

            if (resp[0] == ChessProtocol.TYPE_SER_RESP && resp[1] == 1) {
                addTextInfo("已断开", TextHintPrefixes.TEXT_TYPE_INFO);
                isOnline = false;
                setConnectInfoEditable(true);
                mChessClient.getStartGameBtn().setEnabled(true);
            } else {
                addTextInfo("断开失败", TextHintPrefixes.TEXT_TYPE_INFO);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void doReceiveGameStartSocket(byte[] data) {
        boolean isFirst = data[0] == 1;
        String battleId = new String(Arrays.copyOfRange(data, 1, data.length));

        resetGame();
        mChessClient.getChessPanel().repaint();

        isBlack = isFirst;
        isMyTurn = isFirst;
        this.battleId = battleId;

        addTextInfo("开局！你是" + (isBlack ? "黑" : "白") + "方",
                TextHintPrefixes.TEXT_TYPE_INFO);
    }

    void doReceiveChatInfoSocket(byte[] data) {
        addTextInfo("对方: " + new String(data), TextHintPrefixes.TEXT_TYPE_NONE);
    }

    void doReceiveChessTurnSocket(byte[] data) {
//        addTextInfo(Arrays.toString(data), TextHintPrefixes.TEXT_TYPE_INFO);

        int posX = data[0], posY = data[1];

        isMyTurn = true;

        JPanel panel = mChessClient.getChessPanel();
        int gridWidth = (int) (1f * panel.getWidth() / ChessJudgment.LINE_GRID_COUNT);
        int gridHeight = ((int) (1f * panel.getHeight() / ChessJudgment.LINE_GRID_COUNT));

        addChess(posX, posY, false);

        Graphics2D g = (Graphics2D) panel.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(isBlack ? Color.white : Color.black);

        int cx = (posX + 1) * gridWidth, cy = (posY + 1) * gridHeight;
        int d = (int) (gridWidth * 0.9);

        g.fillOval(cx - d / 2, cy - d / 2, d, d);
    }

    void doReceiveGameEndSocket(byte[] data) {
        addTextInfo(new String(data) + " 赢了", TextHintPrefixes.TEXT_TYPE_INFO);

        mChessClient.getStartGameBtn().setEnabled(true);
        battleId = null;
        isMyTurn = false;
//        resetGame();
    }

    void doResetGame(byte[] data) {
        addTextInfo(new String(data), TextHintPrefixes.TEXT_TYPE_INFO);
        mChessClient.getStartGameBtn().setEnabled(true);
        battleId = null;
    }

    private boolean addChess(int x, int y, boolean isMyTurn) {
        if (chessGrid == null) {
            return false;
        }
        if (x >= 0 && x <= chessGrid.length && y >= 0 && y < chessGrid.length
                && chessGrid[x][y] == 0) {
            chessGrid[x][y] = isMyTurn ? 1 : -1;
            return true;
        }
        return false;
    }


    // ---------- general methods end ----------

    private class ChessPanelUI extends PanelUI {

        private int gridWidth, gridHeight;

        @Override
        public void paint(Graphics g, JComponent c) {
            gridWidth = (int) (1f * c.getWidth() / ChessJudgment.LINE_GRID_COUNT);
            gridHeight = (int) (1f * c.getHeight() / ChessJudgment.LINE_GRID_COUNT);

            g.setColor(new Color(254, 222, 140));
            g.fillRect(gridWidth / 2, gridHeight / 2, c.getWidth() - gridWidth, c.getHeight() - gridHeight);

            g.setColor(Color.black);
            g.drawRect(gridWidth / 2, gridHeight / 2, c.getWidth() - gridWidth, c.getHeight() - gridHeight);

            for (int i = 1; i < ChessJudgment.LINE_GRID_COUNT; i++) {
                int leftOffset = i * gridWidth, topOffset = gridHeight * i;
                g.drawLine(leftOffset, gridHeight, leftOffset, c.getHeight() - gridHeight - 1);
                g.drawLine(gridWidth, topOffset, c.getWidth() - gridWidth - 1, topOffset);
            }

            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int d = (int) (gridWidth * 0.9);

            g.setColor(isBlack ? Color.black : Color.white);
            for (int x = 0; x < chessGrid.length; x++) {
                for (int y = 0; y < chessGrid[x].length; y++) {
                    if (chessGrid[x][y] == 1) {
                        int cx = (x + 1) * gridWidth, cy = (y + 1) * gridHeight;
                        g.fillOval(cx - d / 2, cy - d / 2, d, d);
                    }
                }
            }

            g.setColor(isBlack ? Color.white : Color.black);
            for (int x = 0; x < chessGrid.length; x++) {
                for (int y = 0; y < chessGrid[x].length; y++) {
                    if (chessGrid[x][y] == -1) {
                        int cx = (x + 1) * gridWidth, cy = (y + 1) * gridHeight;
                        g.fillOval(cx - d / 2, cy - d / 2, d, d);
                    }
                }
            }

        }
    }

    private class ChessMouseClickListener extends AbstractMouseListener {

        @Override
        public void mousePressed(MouseEvent event) {

            //检查当前状态能否下棋
            if (mServerSocketThread == null) {
                return;
            }
            if (!isMyTurn) {
                addTextInfo(battleId == null ? "尚未开局" : "你还不能下",
                        TextHintPrefixes.TEXT_TYPE_INFO);
                return;
            }

            JPanel panel = mChessClient.getChessPanel();
            int gridWidth = (int) (1f * panel.getWidth() / ChessJudgment.LINE_GRID_COUNT);
            int gridHeight = ((int) (1f * panel.getHeight() / ChessJudgment.LINE_GRID_COUNT));

            int posX = (event.getX() - (gridWidth >>> 1)) / gridWidth;
            int posY = (event.getY() - (gridHeight >>> 1)) / gridHeight;

//            boolean add = addChess(posX, posY, true);
//            if (!add) return;

            try {
                Socket socket = new Socket(mChessClient.getHostTextField().getText(),
                        Integer.parseInt(mChessClient.getPortTextField().getText()));
                String nickname = mChessClient.getNicknameTextField().getText();

                byte[] sendData = new byte[battleId.getBytes().length + 2 + nickname.getBytes().length];
                sendData[0] = ((byte) posX);
                sendData[1] = ((byte) posY);

                System.arraycopy(battleId.getBytes(), 0, sendData, 2, battleId.getBytes().length);
                System.arraycopy(nickname.getBytes(), 0, sendData, battleId.getBytes().length + 2,
                        nickname.getBytes().length);

                socket.getOutputStream().write(ChessProtocol.
                        encode(ChessProtocol.TYPE_CHESS_TURN, sendData));
                socket.shutdownOutput();

                byte[] resp = new byte[2];
                socket.getInputStream().read(resp);
//                System.out.println(Arrays.toString(resp));
                socket.close();

                if (resp[0] == ChessProtocol.TYPE_SER_RESP && resp[1] != 1) {

                    addTextInfo("这里不能下", TextHintPrefixes.TEXT_TYPE_WARNING);
                    return;
                }

            } catch (IOException e) {
                addTextInfo(e.getMessage(), TextHintPrefixes.TEXT_TYPE_ERROR);
                e.printStackTrace();
            }

            isMyTurn = false;

            addChess(posX, posY, true);

            Graphics2D g = (Graphics2D) panel.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(isBlack ? Color.black : Color.white);

            int cx = (posX + 1) * gridWidth, cy = (posY + 1) * gridHeight;
            int d = (int) (gridWidth * 0.9);

            g.fillOval(cx - d / 2, cy - d / 2, d, d);
        }

    }
}

abstract class AbstractMouseListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }
}