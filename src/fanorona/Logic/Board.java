
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
    //public static final int ROWDIF = 5;
    // The difference between each cell
    public static final int SPACE = 30;
    public static int cellsize;
    public String turn;
    public Player black,white;
    Image imageBlack, imageWhite,imageSelected;
    GamePanel panel;
    
    public Board(GamePanel panel)
    {
        black = new Player(0x000000000297ffffL);
        white = new Player(0x00001ffffd280000L);
        imageBlack = Toolkit.getDefaultToolkit().getImage("images/Black.png");
        imageWhite = Toolkit.getDefaultToolkit().getImage("images/White.png");
        imageSelected = Toolkit.getDefaultToolkit().getImage("images/Red.png");
        this.panel = panel;
        cellsize = 36;
        turn = "pw";
    }

    public void paint(Graphics gr) 
    {
        long mask = 1;
        for (int i = 0; i < SIZE; i++, mask<<=1)
        {
            if((black.state & mask) != 0)
            {
                gr.drawImage(imageBlack,i%COLS*(cellsize+SPACE)+DIF,i/COLS*(cellsize+SPACE)+DIF - (i/COLS*3),cellsize,cellsize,panel);
            }
            if((white.state & mask) != 0)
            {
                gr.drawImage(imageWhite,i%COLS*(cellsize+SPACE)+DIF,i/COLS*(cellsize+SPACE)+DIF - (i/COLS*3),cellsize,cellsize,panel);
            }
        }
    }
    
    // Highlights a selected piece
    public void selected(Graphics gr,int x,int y)
    {
        gr.drawImage(imageSelected,y*(cellsize+SPACE)+DIF,x*(cellsize+SPACE)+DIF - (x*3),cellsize,cellsize,panel);
    }
    
    // returns true if the cell is empty
    public boolean isEmpty(long location)
    {
        if(((location & white.state) == 0) && ((location & black.state) == 0))
        {
            return true;
        }
        return false;
    }
    
}