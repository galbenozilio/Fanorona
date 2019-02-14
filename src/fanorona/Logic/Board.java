
package fanorona.Logic;

import fanorona.Gui.GamePanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;


public class Board 
{
    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int SIZE = ROWS * COLS;
    // The difference between the edges of the image and the begining of the board.
    public static final int DIF=15;
    // The difference between each cell
    public static final int SPACE = 30;
    public static int cellSize;
    public String turn;
    public Player black,white;
    Image imageBlack, imageWhite,imageSelected;
    GamePanel panel;
    long selected = 0;
    boolean anotherMove = false;
    int selectedRow,selectedCol;
    
    public Board(GamePanel panel)
    {
        black = new Player(0x000000000297ffffL);
        white = new Player(0x00001ffffd280000L);
        imageBlack = Toolkit.getDefaultToolkit().getImage("images/Black.png");
        imageWhite = Toolkit.getDefaultToolkit().getImage("images/White.png");
        imageSelected = Toolkit.getDefaultToolkit().getImage("images/Red.png");
        this.panel = panel;
        cellSize = 36;
        turn = "pw";
    }

    public void paint(Graphics gr) 
    {
        long mask = 1;
        for (int i = 0; i < SIZE; i++, mask<<=1)
        {
            if((black.state & mask) != 0)
            {
                gr.drawImage(imageBlack,i%COLS*(cellSize+SPACE)+DIF,i/COLS*(cellSize+SPACE)+DIF - (i/COLS*3),cellSize,cellSize,panel);
            }
            if((white.state & mask) != 0)
            {
                gr.drawImage(imageWhite,i%COLS*(cellSize+SPACE)+DIF,i/COLS*(cellSize+SPACE)+DIF - (i/COLS*3),cellSize,cellSize,panel);
            }
        }
        // Paints a piece in red if it is selected.
        if(selected != 0)
            gr.drawImage(imageSelected,selectedCol*(cellSize+SPACE)+DIF,selectedRow*(cellSize+SPACE)+DIF - (selectedRow*3),cellSize,cellSize,panel);
    }
    
    
    // returns true if the cell is empty
    public boolean isEmpty(long location)
    {
        return (location & white.state) == 0 && (location & black.state) == 0;
    }

    public void Click(int row, int col)
    {
        //A boolean to determine if the player gets another move or not.
        long mask = 1;
        mask<<=(row*9 + col);
        Player curPlayer = turn.equals("pw")? white:black;
        Player opPlayer = turn.equals("pw")? black:white;
        if(selected != 0)
        {// If the player has chosen a piece and now wants to move it.
            if(isEmpty(mask) && Rules.validMove(selected,mask))
            {
                long x = selected;
                x = ~x;
                curPlayer.state &= x;
                curPlayer.state |= mask;
                long eat = Rules.eatingInMyDirection(selected,mask,opPlayer.state);
                if(anotherMove)
                {
                    opPlayer.state ^= eat;
                    if(eat == 0)
                    {
                        anotherMove = false;
                        selected = 0;
                        turn = turn.equals("pw")?"pb":"pw";
                    }
                    else
                    {
                        selected = mask;
                        selectedRow = row;
                        selectedCol = col;
                    }
                }
                else
                {
                    selected = mask;
                    selectedRow = row;
                    selectedCol = col;
                    opPlayer.state ^= eat;
                    if(eat == 0)
                    {
                        selected = 0;
                        turn = turn.equals("pw")?"pb":"pw";
                    }
                    anotherMove = (eat!=0);
                }
                // = opPlayer.state & eat;
            }
            else if(mask == selected)
                selected = 0;
        }
        else if((mask & curPlayer.state) != 0)
        {
            selected = mask;
            selectedRow = row;
            selectedCol = col;
        }
    }
    
}