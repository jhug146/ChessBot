package ChessBot;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;


public class GUI {
    private static Frame window;
    private static PiecesCanvas pieces;
    private static Grid grid;
    private static int titleHeight;

    final static int boardSize = 800;
    final static int squareSize = boardSize / 8;

    final static String pathStart = System.getProperty("user.dir") + "/ChessBot/";
    private final static String iconImage = "Images/black-pawn.png";
    private final static Color transparent = new Color(255, 255, 255, 255);

    public GUI(Grid startGrid) {
        grid = startGrid;

        window = new Frame();
        window.setTitle("JamesBot");
        window.setVisible(true);
        window.setLayout(null);
        titleHeight = window.getInsets().top;
        window.setSize(boardSize, boardSize + titleHeight + 8);
        
        pieces = new PiecesCanvas(grid);
        pieces.setBackground(transparent);
        pieces.setBounds(0, titleHeight, boardSize, boardSize + 8);
        window.add(pieces);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        window.setIconImage(generateImage(iconImage));

        update(grid);

        pieces.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                Main.handleClick(e.getY(), e.getX());
            }
        });
    }

    public static BufferedImage generateImage(String fileName) {
        try {
            final byte[] rawData = Files.readAllBytes(Paths.get(pathStart + fileName));
            return ImageIO.read(new ByteArrayInputStream(rawData));
        } catch (IOException exception) {
            System.out.println(exception.toString());
            return null;
        }
    }

    public static void update(Grid grid) {
        final List<Rectangle> toDraw = new ArrayList<>();
        for (int j=0; j<8; j++) {
            for (int i=0; i<8; i++) {
                toDraw.add(new Rectangle(i * squareSize, j * squareSize, squareSize, squareSize));
            }
        }
        pieces.repaint(toDraw);
    }
}
