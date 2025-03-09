package ChessBot;

import java.util.HashSet;

public class AntiMove {
    private int takenPiece;
    private int prevEnPassantTarget;
    private boolean prevWhiteKingside;
    private boolean prevWhiteQueenside;
    private boolean prevBlackKingside;
    private boolean prevBlackQueenside;
    private boolean prevWhiteCheck;
    private boolean prevBlackCheck;
    private HashSet<Integer> whiteAttackedSquares;
    private HashSet<Integer> blackAttackedSquares;

    public AntiMove(int taken, int enPassant, boolean whiteKingside, boolean whiteQueenside, boolean blackKingside, boolean blackQueenside, HashSet<Integer> whiteAttackSquares, HashSet<Integer> blackAttackSquares, boolean whiteCheck, boolean blackCheck) {
        takenPiece = taken;
        prevEnPassantTarget = enPassant;
        prevWhiteKingside = whiteKingside;
        prevWhiteQueenside = whiteQueenside;
        prevBlackKingside = blackKingside;
        prevBlackQueenside = blackQueenside;
        whiteAttackedSquares = whiteAttackSquares;
        blackAttackedSquares = blackAttackSquares;
        prevWhiteCheck = whiteCheck;
        prevBlackCheck = blackCheck;
    }
    public int getTakenPiece() {
        return takenPiece;
    }
    public int getPrevEnPassantTarget() {
        return prevEnPassantTarget;
    }
    public boolean getPrevWhiteKingside() {
        return prevWhiteKingside;
    }
    public boolean getPrevWhiteQueenside() {
        return prevWhiteQueenside;
    }
    public boolean getPrevBlackKingside() {
        return prevBlackKingside;
    }
    public boolean getPrevBlackQueenside() {
        return prevBlackQueenside;
    }
    public HashSet<Integer> getAttackedSquares(boolean checkForWhite) {
        if (checkForWhite) {
            return whiteAttackedSquares;
        }
        return blackAttackedSquares;
    }
    public boolean wasCheck(boolean checkForWhite) {
        if (checkForWhite) {
            return prevWhiteCheck;
        }
        return prevBlackCheck;
    }
}
