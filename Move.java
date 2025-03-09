package ChessBot;

public class Move {
    int startSquare;
    int endSquare;

    private boolean isCastleKingside = false;
    private boolean isCastleQueenside = false;
    private boolean isEnPassant = false;
    private boolean isDoublePawn = false;
    private int promoteTo = 0;

    private int sortValue; 

    public Move(int start, int end) {
        startSquare = start;
        endSquare = end;
    }

    public void setCastle(boolean kingside) {
        if (kingside) {
            isCastleKingside = true;
        } else {
            isCastleQueenside = true;
        }
    }

    public boolean isKingsideCastle() {
        return isCastleKingside;
    }

    public boolean isCastleGeneral() {
        return isCastleKingside || isCastleQueenside;
    }

    public void setIsEnPassant() {
        isEnPassant = true;
    }

    public void setPromotionValue(int piece) {
        promoteTo = piece;
    }

    public boolean getIsEnPassant() {
        return isEnPassant;
    }

    public void setIsDoublePawn() {
        isDoublePawn = true;
    }

    public boolean getIsDoublePawn() {
        return isDoublePawn;
    }

    public int getPromotionValue() {
        return promoteTo;
    }

    public int getSortValue() {
        return sortValue;
    }

    public void setSortValue(int value) {
        sortValue = value;
    }
}
