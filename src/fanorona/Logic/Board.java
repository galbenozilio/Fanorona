
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
    Image imageBlack, imageWhite,imageSelected, imageX;
    GamePanel panel;
    long selected = 0;
    boolean anotherMove = false;
    boolean choose = false;
    // Eating in my direction, eating in opposite direction.
    long eat1,eat2;
    int selectedRow,selectedCol;
    
    public Board(GamePanel panel)
    {
        black = new Player(0x000000000297ffffL);
        white = new Player(0x00001ffffd280000L);
        imageBlack = Toolkit.getDefaultToolkit().getImage("images/Black.png");
        imageWhite = Toolkit.getDefaultToolkit().getImage("images/White.png");
        imageSelected = Toolkit.getDefaultToolkit().getImage("images/Red.png");
        imageX = Toolkit.getDefaultToolkit().getImage("images/x.png");
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
            if(eat1 != 0 && eat2 != 0 && ((eat1 & mask) != 0 || (eat2 & mask) != 0))
            {
                gr.drawImage(imageX,i%COLS*(cellSize+SPACE)+DIF,i/COLS*(cellSize+SPACE)+DIF - (i/COLS*3),cellSize,cellSize,panel);
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
        if(choose)
        {
            if((mask & eat1) !=0)
                opPlayer.state ^= eat1;
            else if((mask & eat2) !=0)
                opPlayer.state ^= eat2;
            eat1 = 0;
            eat2 = 0;
            choose = false;
        }
        else if(selected != 0)
        {// If the player has chosen a piece and now wants to move it.
            if(isEmpty(mask) && Rules.validMove(selected,mask))
            {
                long x = selected;
                x = ~x;
                curPlayer.state &= x;
                curPlayer.state |= mask;
                eat1 = Rules.eatingInMyDirection(selected,mask,opPlayer.state);
                eat2 = Rules.eatingInOppositeDirection(selected,mask,opPlayer.state);
                selected = mask;
                selectedRow = row;
                selectedCol = col;
                if(anotherMove)
                {// If the player has eaten an enemy piece and now has another move.
                    if(eat1 != 0 && eat2 != 0)
                    {// The player needs to choose whice pieces to eat.
                        choose = true;
                    }
                    else if(eat1 == 0 && eat2 == 0)
                    {// If the move is not an eating move.
                        anotherMove = false;
                        selected = 0;
                        turn = turn.equals("pw")?"pb":"pw";
                    }
                    else
                    {
                        opPlayer.state ^= eat1;
                        opPlayer.state ^= eat2;
                        eat1 = 0;
                        eat2 = 0;                        
                    }
                }
                else
                {
                    //selected = mask;
                    //selectedRow = row;
                    //selectedCol = col;
                    anotherMove = (eat1 != 0 || eat2 != 0);
                    if(eat1 == 0 && eat2 == 0)
                    {// If the move is not an eating move
                        selected = 0;
                        turn = turn.equals("pw")?"pb":"pw";
                    }
                    else if(eat1 != 0 && eat2 != 0 )
                    {
                        choose = true;
                    }
                    else
                    {
                        opPlayer.state ^= eat1;
                        opPlayer.state ^= eat2;
                        eat1 = 0;
                        eat2 = 0;
                    }
                }
                // = opPlayer.state & eat;
            }
            else if(mask == selected && !anotherMove)
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