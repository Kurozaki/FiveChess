package client;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by YotWei on 2017/12/4.
 * the client
 */
public class ChessClient {
    private JPanel mPanel;
    private JButton mConnBtn;
    private JTextField mHostTxtFld;
    private JButton mStartGameBtn;
    private JTextField mInputMsgTxtFld;
    private JTextField mPortTxtFld;
    private JPanel mChessPanel;
    private JButton svrStartBtn;
    private JTextField svrPort;
    private JTextField nicknameTxtFld;
    private JButton mSendMsgBtn;
    private JTextArea textInfoArea;

    private ClientUIEvents uiEvents;

    public ChessClient() {
        uiEvents = new ClientUIEvents(this);
    }

    public JPanel getChessPanel() {
        return mChessPanel;
    }

    public JTextField getHostTextField() {
        return mHostTxtFld;
    }

    public JTextField getPortTextField() {
        return mPortTxtFld;
    }

    public JTextField getNicknameTextField() {
        return nicknameTxtFld;
    }

    public JTextArea getTextInfoArea() {
        return textInfoArea;
    }

    public JTextField getInputMessageTextField() {
        return mInputMsgTxtFld;
    }

    public JTextField getServerPort() {
        return svrPort;
    }

    public JButton getConnButton() {
        return mConnBtn;
    }

    public JButton getSendMessageButton() {
        return mSendMsgBtn;
    }

    public JButton getServerStartBtn() {
        return svrStartBtn;
    }

    public JButton getStartGameBtn() {
        return mStartGameBtn;
    }

    public static void main(String[] args) {
        ChessClient chessClient = new ChessClient();
        JFrame frame = new JFrame("ChessClient");
        frame.setContentPane(chessClient.mPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(150, 30);
        frame.pack();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chessClient.uiEvents.doWindowClosingAction(e);
            }
        });
        frame.setVisible(true);
    }


}
