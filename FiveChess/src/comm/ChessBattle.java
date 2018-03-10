package comm;

import java.awt.*;

/**
 * Created by YotWei on 2017/12/5.
 * a chess battle
 */
public class ChessBattle {

    private ChessJudgment judgment;
    private ChessPlayer[] players;
    private ChessPlayer inTurnPlayer;
    private ChessPlayer winner;

    private boolean isGameOver = false;

    public ChessBattle(ChessPlayer player1, ChessPlayer player2) {
        judgment = new ChessJudgment();
        players = new ChessPlayer[]{player1, player2};
        player1.setColor(Color.black);
        player2.setColor(Color.white);

        inTurnPlayer = player1;
    }


    public ChessPlayer getChatPlayer(String nickname) {
        if (players[0].getNickname().equals(nickname))
            return players[1];
        else
            return players[0];
    }

    public boolean addChess(String playerNickname, int x, int y) {
        if (isGameOver)
            return false;
        if (!inTurnPlayer.getNickname().equals(playerNickname))
            return false;

        if (!judgment.isLegalPlace(x, y)) return false;

        //update game data
        isGameOver = judgment.add(x, y, inTurnPlayer == players[0] ? 1 : -1);
        if (isGameOver) {
            winner = inTurnPlayer;
        }
        inTurnPlayer = inTurnPlayer == players[0] ? players[1] : players[0];
        System.out.println(inTurnPlayer.getNickname() + " in turn");

        return true;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public ChessPlayer getPlayer1() {
        return players == null ? null : players[0];
    }

    public ChessPlayer getPlayer2() {
        return players == null ? null : players[1];
    }
}
