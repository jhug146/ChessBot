package ChessBot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Grid grid;
    private static int startGridPoint = -2;
    private static final String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static final HashMap<String, Integer> pieces = new HashMap<String, Integer>() {{
        put("b", 2);
        put("k", 3);
        put("r", 4);
        put("q", 5);
    }};

    private static final int searchDepth = 4;

    public static void main(String[] args) {
        Grid.initConstants();
        Engine.initMaps();

        grid = new Grid();
        grid.readFromFenString(startFen);

        ZobristTable transpositionTable = new ZobristTable(grid);
        Engine.initTable(transpositionTable);

        new GUI(grid);

        grid.findAttackedSquares();
        if (Arrays.asList(startFen.split(" ")).get(1).equals("b")) {
            Engine.makeComputerMove(grid, searchDepth);
        }

        /*Engine.currentGrid = grid;
        for (int i=1; i<6; i++) {
            System.out.println(i);
            System.out.println(Engine.moveGenerationTest(i));
            System.out.println("");
        }*/
    }

    public static void handleClick(int x, int y) {
        if (startGridPoint == -1 || !grid.getIsWhiteTurn() || x > GUI.boardSize || x < 0 || y < 0 || y > GUI.boardSize) {
            return;
        }

        final int xGrid = x / GUI.squareSize;
        final int yGrid = y / GUI.squareSize;
        if (startGridPoint == -2) {
            if (grid.getGridValues()[xGrid * 8 + yGrid] > 0) {
                startGridPoint = xGrid * 8 + yGrid;
                return;
            }
        }

        Move move = new Move(startGridPoint, xGrid * 8 + yGrid);
        List<Move> moves = Moves.getAllLegalMoves(grid, true, false);

        startGridPoint = -1;

        if (moves.isEmpty()) {
            if (grid.getIsChecked(true)) {
                System.out.println("Black wins by checkmate");
            } else {
                System.out.println("Stalemate");
            }
            return;
        }

        Scanner scanner;
        for (Move potentialMove : moves) {
            if (potentialMove.startSquare == move.startSquare && potentialMove.endSquare == move.endSquare) {
                if (potentialMove.getPromotionValue() > 0) {
                    String chosenPiece = "-";
                    scanner = new Scanner(System.in);
                    while (!pieces.containsKey(chosenPiece)) {
                        System.out.println("Enter the piece you want to promote to (Bishop: b, Knight: k, Rook: r, Queen: q): ");
                        chosenPiece = scanner.nextLine();
                    }
                    scanner.close();
                    potentialMove.setPromotionValue(pieces.get(chosenPiece));
                }

                grid.makeMove(potentialMove, true);
                
                if (grid.getHalfMoves() < 4) {
                    Engine.makeComputerBookMove(grid, searchDepth);
                } else {
                    Engine.makeComputerMove(grid, searchDepth);
                } 
                break;
            }
        }
        startGridPoint = -2;
    }
}
