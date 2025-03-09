package ChessBot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.awt.*;
import java.awt.image.BufferedImage;


public class PiecesCanvas extends Canvas {
    private Grid grid;
    private final int boardSize = 800;
    private final int squareSize = boardSize / 8;

    private final String[] imagesPaths = {"white-pawn.png", "white-bishop.png", "white-knight.png", "white-rook.png", "white-queen.png", "white-king.png", "black-pawn.png", "black-bishop.png", "black-knight.png", "black-rook.png", "black-queen.png", "black-king.png"};
    
    private HashMap<Integer, String> pieceImages;
    private List<Image> pieceImageObjects;

    private final static Color color1 = new Color(119, 149, 86);
    private final static Color color2 = new Color(235, 236, 208);

    public PiecesCanvas(Grid myGrid) {
        grid = myGrid;
        initConstants();
    }

    private void initConstants() {
        pieceImages = new HashMap<>();
        for (int i=0; i<imagesPaths.length; i++) {
            pieceImages.put(Grid.fenPieceValues[i], "Images/" + imagesPaths[i]);
        }
    }

    public void repaint(List<Rectangle> rects) {
        int i = 0;
        final Graphics graphics = this.getGraphics();
        for (Rectangle rect : rects) {
            graphics.setColor((((i / 8) % 2) ^ (i % 2)) == 1 ? color1 : color2);
            graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
            i++;
        }

        int piece;
        BufferedImage image;
        pieceImageObjects = new ArrayList<>();
        for (int j=0; j<8; j++) {
            for (int k=0; k<8; k++) {
                piece = grid.getGridValues()[j * 8 + k];
                if (!(piece == 0)) {
                    image = GUI.generateImage(pieceImages.get(piece));
                    pieceImageObjects.add(image);
                    getGraphics().drawImage(image, k * squareSize, j * squareSize, null);
                }
            }
        } 
    }
}

