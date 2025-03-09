package ChessBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Grid {
    private int[] grid;
    private boolean isTurnWhite;
    private boolean whiteIsChecked;
    private boolean blackIsChecked;
    private boolean whiteKingsideCastle, whiteQueensideCastle, blackKingsideCastle, blackQueensideCastle;
    private int halfMoves;
    private int enPassantTarget;
    private HashSet<Integer> whiteAttackedSquares;
    private HashSet<Integer> blackAttackedSquares;

    private int whiteKingPosition;
    private int blackKingPosition;

    private static HashMap<Character, Integer> fenPieces;
    public final static int[] fenPieceValues = new int[] {1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14};
    private final static char[] fenPieceKeys = new char[] {'P','B','N','R','Q','K','p','b','n','r','q','k'};
    private final static String rowLetters = "abcdefgh";
    private final static String pieceChars = "--BNRQK--BNRQK";
    private final List<String> movesDone = new ArrayList<>();

    public Grid() {}

    public void readFromFenString(String fen) {
        grid = new int[64];
        whiteIsChecked = false;
        blackIsChecked = false;

        List<String> info = new ArrayList<>(Arrays.asList(fen.split(" ")));
        String positions = info.get(0);

        int gridPoint = 0;
        for (int i=0; i<positions.length(); i++) {
            char chr = positions.charAt(i);
            if (Character.isDigit(chr)) {
                gridPoint += chr - '0';
            } else if (!(chr == '/')) {
                grid[gridPoint] = fenPieces.get(chr);
                gridPoint++;
            }
        }

        isTurnWhite = info.get(1).equals("w");
        enPassantTarget = convertPawnString(info.get(3));
        halfMoves = Integer.valueOf(info.get(4));

        String castles = info.get(2);
        whiteKingsideCastle = castles.contains("K");
        whiteQueensideCastle = castles.contains("Q");
        blackKingsideCastle = castles.contains("k");
        blackQueensideCastle = castles.contains("q");

        for (int i=0; i<64; i++) {
            if (grid[i] == 6) {
                whiteKingPosition = i;
            } else if (grid[i] == 14) {
                blackKingPosition = i;
            }
        }
    }

    public static void initConstants() {
        fenPieces = new HashMap<>();
        for (int i=0; i<fenPieceKeys.length; i++) {
            fenPieces.put(fenPieceKeys[i], fenPieceValues[i]);
        }
    }

    public void changeTurn() {
        isTurnWhite = isTurnWhite ? false : true;
    }

    public static int convertPawnString(String pawn) {
        if (pawn.equals("-")) {
            return -1;
        }
        System.out.println(pawn);
        int row = rowLetters.indexOf(pawn.charAt(0));
        return 64 - Integer.valueOf(pawn.charAt(1)) * 8 + row;
    }

    private static Move castleMove(Move move, boolean isTurnWhite) {
        if (move.isKingsideCastle()) {
            if (isTurnWhite) {
                return new Move(63, 61);
            }
            return new Move(7, 5);
        }
        if (isTurnWhite) {
            return new Move(56, 59);
        }
        return new Move(0, 3);
    }

    public void findAttackedSquares() {
        boolean originalTurn = isTurnWhite;

        isTurnWhite = true;
        whiteAttackedSquares = new HashSet<>();
        List<Move> nextMoves = Moves.getAllLegalMoves(this, false, false);
        for (Move m : nextMoves) {
            whiteAttackedSquares.add(m.endSquare);
        }
        blackIsChecked = whiteAttackedSquares.contains(blackKingPosition);

        isTurnWhite = false;
        blackAttackedSquares = new HashSet<>();
        nextMoves = Moves.getAllLegalMoves(this, false, false);
        for (Move m : nextMoves) {
            blackAttackedSquares.add(m.endSquare);
        }
        whiteIsChecked = blackAttackedSquares.contains(whiteKingPosition);

        isTurnWhite = originalTurn;
    }

    public AntiMove makeMove(Move move, boolean updateScreen) {
        if (updateScreen && halfMoves < 5) {
            addMove(move);
        }

        final int rookValue = isTurnWhite ? 4 : 12;
        final int enemyRookValue = 16 - rookValue;
        final int pawnOffset = isTurnWhite ? 8 : -8;

        final boolean prevWhiteKingside = whiteKingsideCastle;
        final boolean prevWhiteQueenside = whiteQueensideCastle;
        final boolean prevBlackKingside = blackKingsideCastle;
        final boolean prevBlackQueenside = blackQueensideCastle;

        final int prevEnPassantTarget = enPassantTarget;
        final HashSet<Integer> prevWhiteAttackedSquares = whiteAttackedSquares;
        final HashSet<Integer> prevBlackAttackedSquares = blackAttackedSquares;

        final boolean wasWhiteCheck = whiteIsChecked;
        final boolean wasBlackCheck = blackIsChecked;

        final int startValue = grid[move.startSquare];
        final int endValue = grid[move.endSquare];

        if (startValue == 6 || move.startSquare == 63) {
            whiteKingsideCastle = false;
        } if (startValue == 6 || move.startSquare == 56) {
            whiteQueensideCastle = false;
        } if (startValue == 14 || move.startSquare == 7) {
            blackKingsideCastle = false;
        } if (startValue == 14 || move.startSquare == 0) {
            blackQueensideCastle = false;
        }

        if (endValue == enemyRookValue) {
            if (move.endSquare == 63) {
                whiteKingsideCastle = false;
            } else if (move.endSquare == 56) {
                whiteQueensideCastle = false;
            } else if (move.endSquare == 7) {
                blackKingsideCastle = false;
            } else if (move.endSquare == 0) {
                blackQueensideCastle = false;
            }
        }

        if (move.isCastleGeneral()) {
            final Move castle = castleMove(move, isTurnWhite);
            grid[castle.endSquare] = rookValue;
            grid[castle.startSquare] = 0;
        }

        int takenPiece;
        if (move.getIsEnPassant()) {
            final int enPassantPos = move.endSquare + pawnOffset;
            takenPiece = grid[enPassantPos];
            grid[enPassantPos] = 0;
        } else {
            takenPiece = grid[move.endSquare];
        }

        if (move.getIsDoublePawn()) {
            enPassantTarget = move.endSquare + pawnOffset;
        } else {
            enPassantTarget = -1;
        }

        if (move.getPromotionValue() > 0) {
            grid[move.endSquare] = move.getPromotionValue();
        } else {
            grid[move.endSquare] = startValue;
        }
        grid[move.startSquare] = 0;

        if (startValue == 6) {
            whiteKingPosition = move.endSquare;
        } else if (startValue == 14) {
            blackKingPosition = move.endSquare;
        }

        findAttackedSquares();
        changeTurn();

        if (updateScreen) {
            GUI.update(this);
            halfMoves++;
        }
        return new AntiMove(takenPiece, prevEnPassantTarget, prevWhiteKingside, prevWhiteQueenside, prevBlackKingside, prevBlackQueenside, prevWhiteAttackedSquares, prevBlackAttackedSquares, wasWhiteCheck, wasBlackCheck);
    }

    public void unmakeMove(Move move, AntiMove antiMove, boolean updateScreen) {
        final int pawnOffset = isTurnWhite ? -8 : 8;
        final int pawnValue = isTurnWhite ? 9 : 1;

        final int takenPiece = antiMove.getTakenPiece();
        enPassantTarget = antiMove.getPrevEnPassantTarget();

        whiteKingsideCastle = antiMove.getPrevWhiteKingside();
        whiteQueensideCastle = antiMove.getPrevWhiteQueenside();
        blackKingsideCastle = antiMove.getPrevBlackKingside();
        blackQueensideCastle = antiMove.getPrevBlackQueenside();

        whiteAttackedSquares = antiMove.getAttackedSquares(true);
        blackAttackedSquares = antiMove.getAttackedSquares(false);
        whiteIsChecked = antiMove.wasCheck(true);
        blackIsChecked = antiMove.wasCheck(false);

        if (grid[move.endSquare] == 6) {
            whiteKingPosition = move.startSquare;
        } else if (grid[move.endSquare] == 14) {
            blackKingPosition = move.startSquare;
        }

        if (move.isCastleGeneral()) {
            final Move antiCastle = castleMove(move, !isTurnWhite);
            grid[antiCastle.startSquare] = grid[antiCastle.endSquare];
            grid[antiCastle.endSquare] = 0;
        }

        if (move.getIsEnPassant()) {
            grid[move.endSquare + pawnOffset] = takenPiece;
        }

        if (move.getPromotionValue() > 0) {
            grid[move.startSquare] = pawnValue;
        } else {
            grid[move.startSquare] = grid[move.endSquare];
        }

        if (!move.getIsEnPassant()) {
            grid[move.endSquare] = takenPiece;
        } else {
            grid[move.endSquare] = 0;
        }

        changeTurn();

        if (updateScreen) {
            GUI.update(this);
            halfMoves--;
        }
    }

    public boolean getIsWhiteTurn() {
        return isTurnWhite;
    }

    public void setIsTurnWhite(boolean newValue) {
        isTurnWhite = newValue;
    }

    public int[] getGridValues() {
        return grid;
    }

    public boolean getIsChecked(boolean checkForWhite) {
        if (checkForWhite) {
            return whiteIsChecked;
        }
        return blackIsChecked;
    }

    public int getEnPassantTarget() {
        return enPassantTarget;
    }

    public int getHalfMoves() {
        return halfMoves;
    }

    public HashSet<Integer> getAttackedSquares(boolean checkForWhite) {
        if (checkForWhite) {
            return whiteAttackedSquares;
        }
        return blackAttackedSquares;
    }

    public int getKingPos(boolean white) {
        if (white) {
            return whiteKingPosition;
        }
        return blackKingPosition;
    }

    public boolean canCastle(boolean white, boolean kingside) {
        if (white && kingside) {
            return whiteKingsideCastle;
        } else if (white) {
            return whiteQueensideCastle;
        } else if (!white && kingside) {
            return blackKingsideCastle;
        }
        return blackQueensideCastle;
    }

    private boolean isKnightMove(int start, int end) {
        final int x = start % 8 - end % 8;
        final int y = start / 8 - end / 8;
        return (int) Math.round(1000 * Math.sqrt(x*x + y*y)) == 2236;
    }

    public Move codeToMove(String code) {
        final int pawnOffset = isTurnWhite ? 8 : -8;
        final int pawnValue = isTurnWhite ? 1 : 9;
        final int bishopValue = isTurnWhite ? 2 : 10;
        final int knightValue = isTurnWhite ? 3 : 11;
        final int queenValue = isTurnWhite ? 5 : 13;
        final int kingValue = isTurnWhite ? 6 : 14;

        int startSquare = -1;
        int endSquare;
        if (Character.isLowerCase(code.charAt(0))){ 
            if (code.contains("x")) {
                endSquare = codeToPos(code.substring(2, 3));
            } else {
                endSquare = codeToPos(code);
            }

            startSquare = endSquare;
            while (!(grid[startSquare] == pawnValue)) {
                startSquare += pawnOffset;
            }
        } else {
            final int pieceValue = pieceChars.indexOf(code.charAt(0));
            endSquare = codeToPos(code.substring(code.length() - 2, code.length()));
            if (pieceValue == kingValue || pieceValue == queenValue) {
                for (int i=0; i<64; i++) {
                    if (grid[i] == pieceValue) {
                        startSquare = i;
                    }
                }
            } else if (pieceValue == bishopValue) {
                for (int i=0; i<64; i++) {
                    if (grid[i] == pieceValue && endSquare % 2 == i % 2) {
                        startSquare = i;
                    }
                }
            } else {
                final boolean hasRankGiven = code.length() > 3 && !code.contains("x");
                boolean distanceMatches, rowMatches;
                for (int i=0; i<64; i++) {
                    distanceMatches = !hasRankGiven && isKnightMove(i, endSquare);
                    rowMatches = hasRankGiven && (i % 8 == rowLetters.indexOf(code.charAt(1)) - 1);
                    if (grid[i] == knightValue && (distanceMatches || rowMatches)) {
                        startSquare = i;
                    }
                }
            }
        }
        return new Move(startSquare, endSquare);
    }

    public int codeToPos(String code) {
        final int rank = rowLetters.indexOf(code.charAt(0));
        final int row = Integer.parseInt("" + code.charAt(1));
        return rank + (8 - row) * 8; 
    }

    public String posToCode(int square) {
        final char column = rowLetters.charAt(square % 8);
        final char row = (char) ((8 - square / 8) + '0');
        return "" + column + row;
    }

    public void addMove(Move move) {
        final int pawnValue = isTurnWhite ? 1 : 9;

        String stringMove;
        if (grid[move.startSquare] == pawnValue) {
            final String code = posToCode(move.endSquare);
            if (move.endSquare % 8 == move.startSquare % 8) {
                stringMove = code;
            } else {
                stringMove = code.charAt(0) + "x" + code.charAt(1);
            }
        } else {
            final StringBuilder sb = new StringBuilder("");
            sb.append(pieceChars.charAt(grid[move.startSquare]));
            if (grid[move.endSquare] > 0) {
                sb.append("x");
            }
            sb.append(posToCode(move.endSquare));
            stringMove = sb.toString();
        }

        if (isTurnWhite ? blackIsChecked : whiteIsChecked) {
            stringMove += "+";
        }
        movesDone.add(stringMove);
    }

    public List<String> getMoves() {
        return movesDone;
    }
}
