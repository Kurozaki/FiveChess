package comm;

import java.awt.*;
import java.net.InetAddress;

/**
 * Created by YotWei on 2017/12/4.
 * chess player
 */
public class ChessPlayer {
    private InetAddress mAddress;
    private int mPort;
    private String nickname;

    public ChessPlayer(InetAddress mAddress, int mPort, String nickname) {
        this.mAddress = mAddress;
        this.mPort = mPort;
        this.nickname = nickname;
        this.mStatus = Status.ONLINE;
    }

    public InetAddress getAddress() {
        return mAddress;
    }

    public int getPort() {
        return mPort;
    }

    public String getNickname() {
        return nickname;
    }

    private Status mStatus = Status.OFFLINE;
    private Color mColor = Color.BLACK;

    public void setColor(Color color) {
        this.mColor = color;
    }

    public void offLine() {
        this.mStatus = Status.OFFLINE;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    public Status getStatus() {
        return mStatus;
    }

    @Override
    public String toString() {
        return "ChessPlayer{" +
                "mAddress=" + mAddress.getHostAddress() +
                ", mPort=" + mPort +
                ", nickname='" + nickname + '\'' +
                '}';
    }

    public enum Status {
        OFFLINE, ONLINE, PREPARE, IN_GAME,
    }
}

