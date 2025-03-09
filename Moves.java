package ChessBot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;



public class Moves {
    private final static int[] kingOffsets = new int[] {1, 9, 8, 7, -1, -9, -8, -7};
    private final static int[] knightOffsets = new int[] {-15, -6, 10, 17, 15, 6, -10, -17};
    private final static int[] whitePawnTakeOffsets = new int[] {-9, -7};
    private final static int[] blackPawnTakeOffsets = new int[] {7, 9};
    private final static int[] underPromotionPieces = new int[] {2, 3, 4, 5};

    private static boolean isSquareSameTeam(int pieceValue, boolean isTurnWhite) {
        return (isTurnWhite && pieceValue > 0 && pieceValue < 8) || (!isTurnWhite && pieceValue > 7);
    }

    private static boolean isSquareDifferentTeam(int pieceValue, boolean isTurnWhite) {
        return (isTurnWhite && pieceValue > 7) || (!isTurnWhite && pieceValue > 0 && pieceValue < 8);
    }

    private static boolean isOnBoard(int pos) {
        return pos >= 0 && pos < 64;
    }

    private static int rankDifference(int start, int end) {
        return Math.abs(start % 8 - end % 8);
    }

    private static List<Move> kingMoves(Grid grid, boolean isTurnWhite) {
        int[] gridValues = grid.getGridValues();
        boolean kingsideCastle = grid.canCastle(isTurnWhite, true);
        boolean queensideCastle = grid.canCastle(isTurnWhite, false);

        int kingPos = grid.getKingPos(isTurnWhite);
        final HashSet<Integer> attackedSquares = grid.getAttackedSquares(!isTurnWhite);
        
        final List<Move> legalMoves = new ArrayList<>();
        for (int offset : kingOffsets) {
            if (rankDifference(kingPos, kingPos + offset) < 2) {
                legalMoves.add(new Move(kingPos, kingPos + offset));
            }
        }

        if (kingsideCastle && (gridValues[kingPos + 1] + gridValues[kingPos + 2]) == 0) {
            if (!(attackedSquares.contains(kingPos + 1) || grid.getIsChecked(isTurnWhite))) {
                final Move castleLeftKing = new Move(kingPos, kingPos + 2);
                castleLeftKing.setCastle(true);
                legalMoves.add(castleLeftKing);
            }
        }
        if (queensideCastle && (gridValues[kingPos - 1] + gridValues[kingPos - 2] + gridValues[kingPos - 3]) == 0) {
            if (!(attackedSquares.contains(kingPos - 1) || grid.getIsChecked(isTurnWhite))) {
                final Move castleRightKing = new Move(kingPos, kingPos - 2);
                castleRightKing.setCastle(false);
                legalMoves.add(castleRightKing);
            }
        }

        return legalMoves;
    }

    private static List<Move> pawnMoves(int[] grid, boolean whitesTurn, int enPassantTarget) {
        final int pawnValue = whitesTurn ? 1 : 9;
        final int singleMoveOffset = whitesTurn ? -8 : 8;
        final int doubleMoveOffset = whitesTurn ? -16 : 16;
        final int pawnStartRow = whitesTurn ? 6 : 1;
        final int colourChange = whitesTurn ? 0 : 8;

        final int[] offsets = whitesTurn ? whitePawnTakeOffsets : blackPawnTakeOffsets;

        int targetValue, newPos;
        final List<Move> validMoves = new ArrayList<>();
        for (int i=0; i<64; i++) {
            if (grid[i] == pawnValue) {
                newPos = i + singleMoveOffset;
                if (grid[newPos] == 0) {
                    validMoves.add(new Move(i, i + singleMoveOffset));
                }

                if (i / 8 == pawnStartRow) {
                    newPos = i + doubleMoveOffset;
                    if (grid[newPos] + grid[i + singleMoveOffset] == 0) {
                        final Move doublePawn = new Move(i, newPos);
                        doublePawn.setIsDoublePawn();
                        validMoves.add(doublePawn);
                    }
                }

                for (int offset : offsets) {
                    if (i % 8 == 0 && (offset == 7 || offset == -9) || i % 8 == 7 && (offset == -7 || offset == 9)) {
                        continue;
                    }
                    targetValue = grid[i + offset];
                    if (isSquareDifferentTeam(targetValue, whitesTurn)) {
                        validMoves.add(new Move(i, i + offset));
                    }
                    if (i + offset == enPassantTarget && grid[enPassantTarget] == 0) {
                        final Move enPassant = new Move(i, enPassantTarget);
                        enPassant.setIsEnPassant();
                        validMoves.add(enPassant);
                    }
                }
            }
        }

        final List<Move> legalMoves = new ArrayList<>();
        for (Move move : validMoves) {
            if (whitesTurn && move.endSquare < 8 || !whitesTurn && move.endSquare > 55) {
                for (int piece : underPromotionPieces) {
                    final Move promotionMove = new Move(move.startSquare, move.endSquare);
                    promotionMove.setPromotionValue(piece + colourChange);
                    legalMoves.add(promotionMove);
                }
            } else {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    private static List<Move> knightMoves(int[] grid, boolean whitesTurn) {
        final int knightValue = whitesTurn ? 3 : 11;
        final List<Integer> pieces = new ArrayList<>();
        for (int i=0; i<64; i++) {
            if (grid[i] == knightValue) {
                pieces.add(i);
            }
        }

        int newPos;
        final List<Move> legalMoves = new ArrayList<>();
        for (int knight : pieces) {
            for (int offset : knightOffsets) {
                newPos = knight + offset;
                if (rankDifference(knight, newPos) < 3) {
                    legalMoves.add(new Move(knight, newPos));
                }
            }
        }
        return legalMoves;
    }

    private static List<Move> rookMoves(int[] grid, boolean whitesTurn) {
        final int queenValue = whitesTurn ? 5 : 13;
        final int rookValue = whitesTurn ? 4 : 12;

        final List<Move> legalMoves = new ArrayList<>();
        final List<Integer> pieces = new ArrayList<>();

        for (int i=0; i<64; i++) {
            if (grid[i] == queenValue || grid[i] == rookValue) {
                pieces.add(i);
            }
        }
        for (int piece : pieces) {
            for (int i=piece+8; i<64; i+=8) {
                if (isSquareSameTeam(grid[i], whitesTurn)) {
                    break;
                } else if (isSquareDifferentTeam(grid[i], whitesTurn)) {
                    legalMoves.add(new Move(piece, i));
                    break;
                }
                legalMoves.add(new Move(piece, i));
            }
            for (int i=piece-8; i>=0; i-=8) {
                if (isSquareSameTeam(grid[i], whitesTurn)) {
                    break;
                } else if (isSquareDifferentTeam(grid[i], whitesTurn)) {
                    legalMoves.add(new Move(piece, i));
                    break;
                }
                legalMoves.add(new Move(piece, i));
            }
            for (int i=piece+1; i%8>0; i++) {
                if (isSquareSameTeam(grid[i], whitesTurn)) {
                    break;
                } else if (isSquareDifferentTeam(grid[i], whitesTurn)) {
                    legalMoves.add(new Move(piece, i));
                    break;
                }
                legalMoves.add(new Move(piece, i));
            }
            for (int i=piece-1; i%8<7 && i>=(8*(piece/8)); i--) {
                if (isSquareSameTeam(grid[i], whitesTurn)) {
                    break;
                } else if (isSquareDifferentTeam(grid[i], whitesTurn)) {
                    legalMoves.add(new Move(piece, i));
                    break;
                }
                legalMoves.add(new Move(piece, i));
            }
        }
        return legalMoves;
    }

    private static List<Move> bishopMoves(int[] grid, boolean whitesTurn) {
        final int bishopValue = whitesTurn ? 2 : 10;
        final int queenValue = whitesTurn ? 5 : 13;

        final List<Move> legalMoves = new ArrayList<>();
        for (int i=0; i<64; i++) {
            if (grid[i] == bishopValue || grid[i] == queenValue) {
                int startLeft = i;
                while (!(startLeft % 8 == 0) && startLeft > 7) {
                    startLeft -= 9;
                    if (isSquareSameTeam(grid[startLeft], whitesTurn)) {
                        break;
                    } else if (isSquareDifferentTeam(grid[startLeft], whitesTurn)) {
                        legalMoves.add(new Move(i, startLeft));
                        break;
                    }
                    legalMoves.add(new Move(i, startLeft));
                }
                startLeft = i;
                while (!(startLeft % 8 == 7) && startLeft < 56) {
                    startLeft += 9;
                    if (isSquareSameTeam(grid[startLeft], whitesTurn)) {
                        break;
                    } else if (isSquareDifferentTeam(grid[startLeft], whitesTurn)) {
                        legalMoves.add(new Move(i, startLeft));
                        break;
                    }
                    legalMoves.add(new Move(i, startLeft));
                }

                int startRight = i;
                while (!(startRight % 8 == 7) && startRight > 7) {
                    startRight -= 7;
                    if (isSquareSameTeam(grid[startRight], whitesTurn)) {
                        break;
                    } else if (isSquareDifferentTeam(grid[startRight], whitesTurn)) {
                        legalMoves.add(new Move(i, startRight));
                        break;
                    }
                    legalMoves.add(new Move(i, startRight));
                }
                startRight = i;
                while (!(startRight % 8 == 0) && startRight < 56) {
                    startRight += 7;
                    if (isSquareSameTeam(grid[startRight], whitesTurn)) {
                        break;
                    } else if (isSquareDifferentTeam(grid[startRight], whitesTurn)) {
                        legalMoves.add(new Move(i, startRight));
                        break;
                    }
                    legalMoves.add(new Move(i, startRight));
                }
            }
        }
        return legalMoves;
    }

    public static List<Move> getAllLegalMoves(Grid grid, boolean lookForCheck, boolean onlyCaptures) {
        List<Move> allMoves = new ArrayList<>();

        boolean isTurnWhite = grid.getIsWhiteTurn();
        int[] gridValues = grid.getGridValues();

        allMoves.addAll(kingMoves(grid, isTurnWhite));
        allMoves.addAll(pawnMoves(gridValues, isTurnWhite, grid.getEnPassantTarget()));
        allMoves.addAll(rookMoves(gridValues, isTurnWhite));
        allMoves.addAll(knightMoves(gridValues, isTurnWhite));
        allMoves.addAll(bishopMoves(gridValues, isTurnWhite));

        List<Move> validMoves = new ArrayList<>();
        for (Move move : allMoves) {
            if (isOnBoard(move.endSquare) && !(move.startSquare == move.endSquare) && !isSquareSameTeam(gridValues[move.endSquare], isTurnWhite)) {
                validMoves.add(move);
            }
        }

        if (onlyCaptures) {
            allMoves = new ArrayList<>();
            for (Move move : validMoves) {
                if (gridValues[move.endSquare] > 0) {
                    allMoves.add(move);
                }
            }
            validMoves = new ArrayList<>();
            for (Move move : allMoves) {
                validMoves.add(move);
            }
        }

        if (!lookForCheck) {
            return validMoves;
        }

        final List<Move> legalMoves = new ArrayList<>();
        for (Move move : validMoves) {
            AntiMove antiMove = grid.makeMove(move, false);
            if (!grid.getIsChecked(isTurnWhite)) {
                legalMoves.add(move);
            }
            grid.unmakeMove(move, antiMove, false);
        }

        return legalMoves;
    }
}
