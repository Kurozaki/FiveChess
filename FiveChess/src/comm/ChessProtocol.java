package comm;

import util.DataUtil;

import java.util.Arrays;

/**
 * Created by YotWei on 2017/12/4.
 * a data class for communication protocol format
 */

public class ChessProtocol {
    public static final byte TYPE_LOGIN = 0x1;
    public static final byte TYPE_LOGOUT = 0x2;
    public static final byte TYPE_GAME_START = 0x3;
    public static final byte TYPE_GAME_END = 0x4;
    public static final byte TYPE_CHAT = 0x5;           //客户端发出聊天信息到服务器，请求转发到指定客户端
    public static final byte TYPE_STRING_MSG = 0x6;     //数据内容为文本
    public static final byte TYPE_SER_RESP = 0x7;
    public static final byte TYPE_GAME_PREPARE = 0x8;
    public static final byte TYPE_CHESS_TURN = 0x9;
    public static final byte TYPE_GAME_RESET = 0xa;

    private byte type;
    private byte length = 0;
    private byte[] data;

    private ChessProtocol() {
    }

    public byte getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public static byte[] encode(byte type, byte[] data) {
        byte len = 0;
        if (data != null) {
            len = (byte) data.length;
        }
        byte[] bytes = new byte[2 + len];
        bytes[0] = type;
        bytes[1] = len;
        if (data != null) {
            System.arraycopy(data, 0, bytes, 2, data.length);
        }
        return bytes;
    }

    public static ChessProtocol decode(byte[] bytes) {
        ChessProtocol protocol = new ChessProtocol();
        protocol.type = bytes[0];
        protocol.length = bytes[1];
        if (protocol.length > 0) {
            protocol.data = Arrays.copyOfRange(bytes, 2, bytes.length);
        }
        return protocol;
    }

    /**
     * @param bindHost bind host (local address)
     * @param bindPort local bind port
     * @param nickname your nickname
     */
    public static byte[] buildLoginData(byte[] bindHost, int bindPort, String nickname) {
        return DataUtil.mergeByteArrays(
                bindHost,
                DataUtil.int2ByteArray(bindPort),
                nickname.getBytes());
    }

    /**
     * @param nickname    sender nickname
     * @param chatMessage sending message
     */
    public static byte[] buildChatData(String battleId, String nickname, String chatMessage) {
        byte[] battleIdBytes = battleId.getBytes();
        byte[] nicknameBytes = nickname.getBytes();
        byte[] chatMessageBytes = chatMessage.getBytes();

        byte[] battlePattern = new byte[battleIdBytes.length + 1];
        battlePattern[0] = ((byte) battleIdBytes.length);
        System.arraycopy(battleIdBytes, 0, battlePattern, 1, battleIdBytes.length);

        byte[] nicknamePattern = new byte[nicknameBytes.length + 1];
        nicknamePattern[0] = ((byte) nicknameBytes.length);
        System.arraycopy(nicknameBytes, 0, nicknamePattern, 1, nicknameBytes.length);

        byte[] chatMsgPattern = new byte[chatMessageBytes.length + 1];
        chatMsgPattern[0] = ((byte) chatMessageBytes.length);
        System.arraycopy(chatMessageBytes, 0, chatMsgPattern, 1, chatMessageBytes.length);

        return DataUtil.mergeByteArrays(battlePattern, nicknamePattern, chatMsgPattern);
    }

    public static byte[] buildStartGameData(String battleId, boolean isFirst) {
        byte[] battleIdBytes = battleId.getBytes();

        byte[] data = new byte[battleIdBytes.length + 1];
        data[0] = (byte) (isFirst ? 1 : 0);
        System.arraycopy(battleIdBytes, 0, data, 1, battleIdBytes.length);
        return data;
    }

    @Override
    public String toString() {
        return "ChessProtocol{" +
                "type=" + type +
                ", length=" + length +
                ", comm=" + Arrays.toString(data) +
                '}';
    }
}