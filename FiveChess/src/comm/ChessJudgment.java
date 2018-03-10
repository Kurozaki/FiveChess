package comm;

/**
 * Created by YotWei on 2017/12/4.
 * a game judgment
 */

public class ChessJudgment {

    public static final int LINE_GRID_COUNT = 20;

    public ChessJudgment() {
        this(LINE_GRID_COUNT);
    }

    public ChessJudgment(int gridCount) {
        this.gridCount = gridCount;
        pStatus = new int[this.gridCount - 1][this.gridCount - 1];
    }

    private int[][] pStatus;
    private int gridCount;

    public boolean add(int x, int y, int player) {

        if (player == 0)
            return false;

        pStatus[x][y] = player;
        int counter = 0;

        for (int i = -4; i <= 4; i++) {
            if (ablePlaceChess(x + i, y) && pStatus[x + i][y] == player) {
                counter++;
                if (counter == 5) {
                    return true;
                }
            } else {
                counter = 0;
            }
        }

        for (int i = -4; i <= 4; i++) {
            if (ablePlaceChess(x, y + i) && pStatus[x][y + i] == player) {
                counter++;
                if (counter == 5) {
                    return true;
                }
            } else {
                counter = 0;
            }
        }

        for (int i = -4; i <= 4; i++) {
            if (ablePlaceChess(x + i, y + i) && pStatus[x + i][y + i] == player) {
                counter++;
                if (counter == 5) {
                    return true;
                }
            } else {
                counter = 0;
            }
        }

        for (int i = -4; i <= 4; i++) {
            if (ablePlaceChess(x + i, y - i) && pStatus[x + i][y - i] == player) {
                counter++;
                if (counter == 5) {
                    return true;
                }
            } else {
                counter = 0;
            }
        }
        return false;
    }

    private boolean ablePlaceChess(int x, int y) {
        return x >= 0 && x < gridCount && y >= 0 && y < gridCount;
    }

    public boolean isLegalPlace(int x, int y) {
        return ablePlaceChess(x, y) && pStatus[x][y] == 0;
    }

    public void reset() {
        pStatus = new int[this.gridCount][this.gridCount];
    }
}
