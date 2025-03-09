package ChessBot;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Engine {
    final static private int pawnValue = 100;
    final static private int knightValue = 320;
    final static private int bishopValue = 330;
    final static private int rookValue = 500;
    final static private int queenValue = 900;
    final static private int[] materialValues = new int[] {pawnValue, bishopValue, knightValue, rookValue, queenValue, 0};

    final static private Random random = new Random();

    private static ZobristTable transpositionTable;
    private static int transpositionCount = 0;

    final static private int[] whitePawnHeatMap = new int[] {
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 20,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    final static private int[] whiteKnightHeatMap = new int[] {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50, 
    };
    final static private int[] whiteBishopHeatMap = new int[] {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20,
    };
    final static private int[] whiteRookHeatMap = new int[] {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, 10, 10, 10, 10,  5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
    };
    final static private int[] whiteQueenHeatMap = new int[] {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,  0,  5,  5,  5,  5,  0, -5,
        0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };
    final static private int[] whiteKingHeatMap = new int[] {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20, 20,  0,  0,  0,  0, 20, 20,
        20, 30, 10,  0,  0, 10, 30, 20
    };
    static private int[][] whiteMaps = new int[][] {whitePawnHeatMap, whiteBishopHeatMap, whiteKnightHeatMap, whiteRookHeatMap, whiteQueenHeatMap, whiteKingHeatMap};

    static private int[] blackPawnHeatMap;
    static private int[] blackKnightHeatMap;
    static private int[] blackBishopHeatMap;
    static private int[] blackRookHeatMap;
    static private int[] blackQueenHeatMap;
    static private int[] blackKingHeatMap;

    static private int[][] blackMaps;
    static public Grid currentGrid;

    private static int[] reverseArray(int[] array) {
        int[] toReturn = new int[array.length];
        for (int i=0; i<array.length; i++) {
            toReturn[i] = array[63 - i];
        }
        return toReturn;
    }

    public static void initMaps() {
        blackPawnHeatMap = reverseArray(whitePawnHeatMap);
        blackBishopHeatMap = reverseArray(whiteBishopHeatMap);
        blackKnightHeatMap = reverseArray(whiteKnightHeatMap);
        blackRookHeatMap = reverseArray(whiteRookHeatMap);
        blackQueenHeatMap = reverseArray(whiteQueenHeatMap);
        blackKingHeatMap = reverseArray(whiteKingHeatMap);

        blackMaps = new int[][] {blackPawnHeatMap, blackBishopHeatMap, blackKnightHeatMap, blackRookHeatMap, blackQueenHeatMap, blackKingHeatMap};
    }

    public static void initTable(ZobristTable table) {
        transpositionTable = table;
    }

    private static int[] sumMaterial(int[] grid) {
        int material = 0;
        int whitePieceCount = 0;
        int blackPieceCount = 0;
        for (int i=0; i<64; i++) {
            material += getValueOf(grid[i]);
        }
        return new int[] {material, Math.min(whitePieceCount, blackPieceCount)};
    }

    /*
    private static int moveGenerationTest(int depth) {
        if (depth == 0) {
            return 1;
        }

        int total = 0;
        List<Move> moves = Moves.getAllLegalMoves(currentGrid, true, false);
        for (Move move : moves) {
            AntiMove antiMove = currentGrid.makeMove(move, false);
            total += moveGenerationTest(depth - 1);
            currentGrid.unmakeMove(move, antiMove, false);
        }
        return total;
    }
    */

    private static int search(int depth, int alpha, int beta) {
        long hash = transpositionTable.getHash(currentGrid);
        /*int ttEvaluation = transpositionTable.getEvaluation(hash, depth);
        if (!(ttEvaluation == ZobristTable.lookupFailed)) {
            transpositionCount++;
            return ttEvaluation;
        }*/

        if (depth == 0) {
            //return quiescenceSearch(alpha, beta);
            return evaluateBoard(currentGrid);
        }
        List<Move> moves = Moves.getAllLegalMoves(currentGrid, true, false);
        
        if (depth > 1) {
            moves = orderMoves(moves);
        }
        if (moves.isEmpty()) {
            if (currentGrid.getIsChecked(currentGrid.getIsWhiteTurn())) {
                return Integer.MIN_VALUE + 10000 + depth * 100;
            }
            return 0;
        }

        int evaluation;
        AntiMove reverseMove;
        for (Move move : moves) {
            reverseMove = currentGrid.makeMove(move, false);
            evaluation = -search(depth - 1, -beta, -alpha);
            if (evaluation >= beta) {
                transpositionTable.addEvaluation(transpositionTable.getHash(currentGrid), depth, evaluation);
            }
            currentGrid.unmakeMove(move, reverseMove, false);
        
            if (evaluation >= beta) {
                return beta;
            }
            if (evaluation > alpha) {
                alpha = evaluation;
            }
        }

        transpositionTable.addEvaluation(hash, depth, alpha);
        return alpha;
    }

    /*
    private static int quiescenceSearch(int alpha, int beta) {
        int eval = evaluateBoard(currentGrid);
        if (eval >= beta) {
            return beta;
        }
        if (eval > alpha) {
            alpha = eval;
        }

        List<Move> moves = Moves.getAllLegalMoves(currentGrid, true, true);

        for (Move move : moves) {
            AntiMove antiMove = currentGrid.makeMove(move, false);
            eval = -quiescenceSearch(-beta, -alpha);
            currentGrid.unmakeMove(move, antiMove, false);

            if (eval >= beta) {
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }
        return alpha;
    }*/

    private static int applyHeatMaps(int[] grid) {
        int totalScore = 0;
        int pieceValue;
        for (int i=0; i<64; i++) {
            pieceValue = grid[i];
            if (pieceValue > 0) {
                if ((pieceValue & 8) == 0) {
                    totalScore += whiteMaps[pieceValue - 1][i];
                } else {
                    totalScore -= blackMaps[pieceValue - 9][i];
                }
            }
        }
        return totalScore;
    }

    private static int evaluateBoard(Grid grid) {
        int totalScore = 0;

        final int[] materialStats = sumMaterial(grid.getGridValues());
        final int material = materialStats[0];
        totalScore += material;

        totalScore += applyHeatMaps(grid.getGridValues());
        return totalScore;
    }

    private static int getValueOf(int piece) {
        if (piece > 8) {
            return -materialValues[piece - 9];
        } else if (piece > 0) {
            return materialValues[piece - 1];
        }
        return 0;
    }

    private static List<Move> orderMoves(List<Move> moves) {
        final int[] grid = currentGrid.getGridValues();
        final boolean isTurnWhite = currentGrid.getIsWhiteTurn();
        final HashSet<Integer> attackedSquares = currentGrid.getAttackedSquares(isTurnWhite);

        for (int i=0; i<moves.size(); i++) {
            final Move move = moves.get(i);
            final int destinationSquare = move.endSquare;
            final int capturePiece = grid[destinationSquare];
            final int takingPiece = grid[moves.get(i).startSquare];

            int score = 10 * getValueOf(capturePiece) - getValueOf(takingPiece);

            if (attackedSquares.contains(destinationSquare)) {
                score -= getValueOf(capturePiece);
            }
            move.setSortValue(score);
        }

        Collections.sort(moves, new Comparator<Move>() {
            @Override 
            public int compare(Move move1, Move move2) {
                return move1.getSortValue() - move2.getSortValue();
            }
        });
        return moves;
    }

    public static void makeComputerBookMove(Grid grid, int depth) {
        List<String[]> openings = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(GUI.pathStart + "Assets/Openings.txt"));
            String line = br.readLine();
 
            while (!(line == null)) {
                openings.add(line.split(" "));
                line = br.readLine();
            }

            br.close();
        } catch (IOException e) {
            System.out.println("Error reading openings file");
        }

        final List<String> moves = grid.getMoves();
        final int halfMoves = grid.getHalfMoves();
        for (int i=0; i<halfMoves; i++) {
            final List<String[]> tempMoves = new ArrayList<>();
            for (String[] sequence : openings) {
                if (sequence[i].equals(moves.get(i))) {
                    tempMoves.add(sequence);
                }
            }
            openings = new ArrayList<>();
            openings.addAll(tempMoves);
        }

        if (openings.isEmpty()) {
            System.out.println("No book move found");
            makeComputerMove(grid, depth);
        } else {
            final String[] choseOpening = openings.get(random.nextInt(openings.size()));
            System.out.println(choseOpening[halfMoves]);
            final Move move = grid.codeToMove(choseOpening[halfMoves]);
            
            grid.makeMove(move, true);
        }
    }

    public static void makeComputerMove(Grid grid, int depth) {
        System.out.println("Depth: ");
        System.out.println(depth);
        final long startTime = System.nanoTime();

        currentGrid = grid;
        List<Move> moves = Moves.getAllLegalMoves(grid, true, false);
        int evaluation;
        Move bestMove = null;
        AntiMove reverseMove;
        int alpha = Integer.MIN_VALUE + 1000;
        int beta = Integer.MAX_VALUE - 1000;

        if (moves.isEmpty()) {
            if (currentGrid.getIsChecked(false)) {
                System.out.println("White wins by checkmate");
            } else {
                System.out.println("Stalemate");
            }
            return;
        }

        moves = orderMoves(moves);

        int i = 1;
        for (Move move : moves) {
            reverseMove = currentGrid.makeMove(move, false);  
            evaluation = -search(depth, -beta, -alpha);
            currentGrid.unmakeMove(move, reverseMove, false);

            if (evaluation > alpha) {
                alpha = evaluation;
                bestMove = move;
            }
            if (System.nanoTime() - startTime > 15000000000L) {
                System.out.println(i);
                System.out.println("/");
                System.out.println(moves.size());
                System.out.println("");
            }
            i++;
        }

        if (System.nanoTime() - startTime < 200) {
            makeComputerMove(grid, depth + 2);
            return;
        }

        transpositionCount = 0;
        grid.makeMove(bestMove, true);
        System.out.println("Evaluation: ");
        System.out.println(alpha / 100);
        System.out.println("");
    }
    
    public int getTranspositionCount() {
        return transpositionCount;
    }
}