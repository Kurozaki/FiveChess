package server;

import comm.ServerSocketThread;
import comm.TextHintPrefixes;

import javax.swing.*;

/**
 * Created by YotWei on 2017/12/4.
 * server for five chess
 */

public class ChessServer {

    private JPanel mPanel;
    private JButton bindBtn;
    private JTextField portField;
    private JTextArea infoText;

    private volatile boolean isRunning = false;

    private ServerSocketThread socketThread;

    private ChessServer() {
        socketThread = new ServerSocketThread(new ChessServerListener(this));

        infoText.setEditable(false);

        bindBtn.addActionListener(ev -> {
            if (!portField.getText().matches("^\\d+$")) {
                addTextInfoLine("端口格式错误", TextHintPrefixes.TEXT_TYPE_ERROR);
                return;
            }

            try {
                if (!isRunning) {
                    socketThread.start(Integer.parseInt(portField.getText()));
                } else {
                    socketThread.stop();
                }
            } catch (Exception e) {
                addTextInfoLine(e.getMessage(), TextHintPrefixes.TEXT_TYPE_ERROR);
                e.printStackTrace();
            }
        });
    }

    void onStartRunning() {
        isRunning = true;
        portField.setEditable(false);
        bindBtn.setText("停止");
        addTextInfoLine("服务器已启动", TextHintPrefixes.TEXT_TYPE_INFO);
    }

    void onStopRunning() {
        isRunning = false;
        portField.setEditable(true);
        bindBtn.setText("启动");
        addTextInfoLine("服务器已停止运行", TextHintPrefixes.TEXT_TYPE_INFO);
    }

    void addTextInfoLine(String text, int type) {
        infoText.append(TextHintPrefixes.textPrefix(type) + " " + text + "\n");
        infoText.setCaretPosition(infoText.getDocument().getLength());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("服务端");
        frame.setContentPane(new ChessServer().mPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(400, 200);
        frame.pack();
        frame.setVisible(true);
    }
}