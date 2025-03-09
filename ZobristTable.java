package ChessBot;

import java.util.HashMap;

public class ZobristTable {
    private final HashMap<Long, int[]> table = new HashMap<>();
    private long[][] randomBoard;
    private long[] otherConditions;

    private long pseudoValue;

    private final long seed = 34254352346526L;
    private final long modulus = 45351355423536L;
    private final long multiplier = 89992557876453L;
    private final long increment = 14369294324712L;

    private final static int lookupFailed = -655653564;

    public ZobristTable(Grid board) {
        initConstants();
    }

    private long nextPseudoRandom() {
        return (multiplier * pseudoValue + increment) % modulus;
    }

    private void initConstants() {
        randomBoard = new long[64][16];
        pseudoValue = seed;
        for (int i=0; i<64; i++) {
            final long[] pieces = new long[16];
            for (int j=0; j<16; j++) {
                pieces[j] = nextPseudoRandom();
            }
            randomBoard[i] = pieces;
        }
        
        otherConditions = new long[13];
        for (int i=0; i<13; i++) {
            otherConditions[i] = nextPseudoRandom();
        }
    }

    /*private void initHash(Grid board) {
        hash = 0;
        int[] grid = board.getGridValues();
        for (int i=0; i<64; i++) {
            if (grid[i] > 0) {
                hash ^= randomBoard[i][grid[i]];
            }
        }

        boolean[] conditions = new boolean[] {
            board.canCastle(false, true),
            board.canCastle(false, false),
            board.canCastle(true, true),
            board.canCastle(true, false),
            board.getIsWhiteTurn()
        };

        for (int i=0; i<conditions.length; i++) {
            if (conditions[i]) {
                hash ^= otherConditions[i];
            }
        }

        int enPassantTarget = board.getEnPassantTarget();
        if (enPassantTarget > -1) {
            hash ^= otherConditions[6 + enPassantTarget % 8];
        }
    }

    private void updateHash(Move move, Grid board) {
        final int[] grid = board.getGridValues();
        final boolean isTurnWhite = board.getIsWhiteTurn();
        final int turnOffset = isTurnWhite ? 0 : 2;
        final int kingValue = isTurnWhite ? 6 : 14;
        final int rookValue = isTurnWhite ? 4 : 8;

        hash ^= randomBoard[move.startSquare][grid[move.startSquare]];
        if (grid[move.endSquare] > 0) {
            hash ^= randomBoard[move.endSquare][grid[move.endSquare]];
        }
        hash ^= randomBoard[move.endSquare][grid[move.startSquare]];

        if (grid[move.startSquare] == kingValue) {
            if (board.canCastle(isTurnWhite, true)) {
                hash ^= otherConditions[turnOffset];
            } if (board.canCastle(isTurnWhite, false)) {
                hash ^= otherConditions[turnOffset + 1];
            }
        }

        if (grid[move.startSquare] == rookValue) {
            if (move.startSquare == 0 && board.canCastle(false, false)) {
                hash ^= otherConditions[3];
            } else if (move.startSquare == 7 && board.canCastle(false, true)) {
                hash ^= otherConditions[2];
            } else if (move.startSquare == 56 && board.canCastle(true, false)) {
                hash ^= otherConditions[1];
            } else if (move.startSquare == 63 && board.canCastle(true, true)) {
                hash ^= otherConditions[0];
            }
        }

        hash ^= otherConditions[4];
    }*/

    public long getHash(Grid board) {
        long newHash = 0;
        final int[] grid = board.getGridValues();
        for (int i=0; i<64; i++) {
            if (grid[i] > 0) {
                newHash ^= randomBoard[i][grid[i]];
            }
        }

        if (board.getIsWhiteTurn()) {
            newHash ^= otherConditions[4];
        }

        if (board.canCastle(true, true)) {
            newHash ^= otherConditions[0];
        } if (board.canCastle(true, false)) {
            newHash ^= otherConditions[1];
        } if (board.canCastle(false, true)) {
            newHash ^= otherConditions[2];
        } if (board.canCastle(false, false)) {
            newHash ^= otherConditions[3];
        }

        final int enPassantTarget = board.getEnPassantTarget();
        if (enPassantTarget > -1) {
            newHash ^= otherConditions[5 + enPassantTarget % 8];
        }

        return newHash;
    }

    public int getEvaluation(long hash, int depth) {
        if (table.containsKey(hash)) {
            final int[] lookup = table.get(hash);
            if (lookup[1] <= depth) {
                final int sign = (depth - lookupFailed) % 2;
                return (sign == 0 ? 1 : -1) * lookup[0];
            }
        }
        return lookupFailed;   
    }

    public void addEvaluation(long hash, int depth, int evaluation){
        table.put(hash, new int[] {evaluation, depth});
    } 
}
