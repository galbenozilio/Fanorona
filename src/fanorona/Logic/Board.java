
package fanorona.Logic;

import fanorona.Gui.GamePanel;
import static fanorona.Logic.Rules.fullBoard;
import static fanorona.Logic.Rules.validMove;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JOptionPane;


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
    private String turn;
    private Player black,white;
    Image imageBlack, imageWhite,imageSelected, imageX;
    GamePanel panel;
    // Selected - the selected piece,prev - the previous spots the piece moved 
    // over in the last move(used only in moves with more than one eating).
    // Possible - the possible moves a player has after an eating move.
    long selected = 0,prev = 0,possible = 0;
    private boolean anotherMove = false;
    private boolean choose = false;
    // Eating in my direction, eating in opposite direction.
    private long eat1,eat2;
    private int selectedRow,selectedCol;
    private Rules gameRules;
    // The previous direction the player moved to in his multiple eating move.
    private int prevDirection;
    
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
        this.gameRules = new Rules();
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
    
    
    /**
     * Checks if a move is valid.
     * Uses the validMove function to check if the piece can move in the 
     * chosen direction, and to check if the spot is empty.
     */
    public boolean isEmpty(long to)
    {
        return ((to & white.state) == 0 && (to & black.state) == 0);
    }

    public void Click(int row, int col)
    {
        //A boolean to determine if the player gets another move or not.
        long mask = 1;
        mask<<=(row*9 + col);
        Player curPlayer = turn.equals("pw")? white:black;
        Player opPlayer = turn.equals("pw")? black:white;
        if(!anotherMove)
        {
            possible = Rules.fullBoard;
            this.prevDirection = 0;
        }
        if(choose)
        {// If the player needs to choose which pieces to eat. 
            if((mask & eat1) !=0 || ((mask & eat2) !=0))
            {
                if((mask & eat1) !=0)
                    opPlayer.state ^= eat1;
                else if((mask & eat2) !=0)
                    opPlayer.state ^= eat2;
                eat1 = 0;
                eat2 = 0;
                choose = false;
            }
        }
        else if(selected != 0 && (mask & ~prev) != 0 && (mask & possible) != 0)
        {// If the player has chosen a piece and now wants to move it.
            if(isEmpty(mask) && gameRules.validMove(selected, mask))
            {
                long x = selected;
                x = ~x;
                curPlayer.state &= x;
                curPlayer.state |= mask;
                eat1 = gameRules.eatingInMyDirection(selected,mask,opPlayer.state);
                eat2 = gameRules.eatingInOppositeDirection(selected,mask,opPlayer.state);
                prev |= selected;
                this.prevDirection = (int) (selected>mask? selected/mask:-mask/selected);
                selected = mask;
                selectedRow = row;
                selectedCol = col;
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
            else if(mask == selected && !anotherMove)
                selected = 0;
        }
        else if((mask & curPlayer.state) != 0)
        {// Choosing a piece to move with
            selected = mask;
            selectedRow = row;
            selectedCol = col;
        }
        
    }
    
    public void getOneMoreMove()
    {
        if(!choose)
        {
            if(anotherMove)
            {
                if(checkPossibilities(selected) == 0)
                {
                    anotherMove = false;
                    selected = 0;
                    selectedRow = 0;
                    selectedCol = 0;
                    prev = 0;
                    turn = turn.equals("pw")?"pb":"pw";
                }
            }
            else
                prev = 0;
        }
    }
    
    /**
     * Checks if the selected piece can do an eating move.
     * This method is used after one or more eatings, because a player gets an 
     * extra move after eating only if he has another eating move he can make 
     * using the same piece.
     * First we check if the move is valid - if the space is empty and the piece
     * can move there, then we check if the player can perform an eating move in 
     * that direction. If he can, this move is added to the possible moves mask.
     */
    public long checkPossibilities(long from)
    {
        Player opPlayer = turn.equals("pw")? black:white;
        long mask = 0;
        if(gameRules.validMove(selected,from >> 1) && isEmpty(from >> 1))
            if(gameRules.eatingInMyDirection(from, from >> 1,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from >> 1,opPlayer.state) != 0)
                 mask |= from >> 1;
        if(gameRules.validMove(from,from << 1) && isEmpty(from << 1))
            if(gameRules.eatingInMyDirection(from, from << 1,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from << 1,opPlayer.state) != 0)
                 mask |= from << 1;
        if(gameRules.validMove(from,from >> 8) && isEmpty(from >> 8))
            if(gameRules.eatingInMyDirection(from, from >> 8,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from >> 8,opPlayer.state) != 0)
                 mask |= from >> 8;
        if(gameRules.validMove(from,from << 8) && isEmpty(from << 8))
            if(gameRules.eatingInMyDirection(from, from << 8,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from << 8,opPlayer.state) != 0)
                 mask |= from << 8;
        if(gameRules.validMove(from,from >> 9) && isEmpty(from >> 9))
            if(gameRules.eatingInMyDirection(from, from >> 9,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from >> 9,opPlayer.state) != 0)
                 mask |= from >> 9;
        if(gameRules.validMove(from,from << 9) && isEmpty(from << 9))
            if(gameRules.eatingInMyDirection(from, from << 9,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from << 9,opPlayer.state) != 0)
                 mask |= from << 9;
        if(gameRules.validMove(from,from >> 10) && isEmpty(from >> 10))
            if(gameRules.eatingInMyDirection(from, from >> 10,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from >> 10,opPlayer.state) != 0)
                 mask |= from >> 10;
        if(gameRules.validMove(from,from << 10) && isEmpty(from << 10))
            if(gameRules.eatingInMyDirection(from, from << 10,opPlayer.state) != 0|| gameRules.eatingInOppositeDirection(from, from << 10,opPlayer.state) != 0)
                 mask |= from << 10;
        //So we don't get numbers that are outside of the board.
        mask &= fullBoard;
        // The player can't go to a place he has already been to in the current move.
        mask &= ~prev;
         // The previous direction the player moved in.
        int dir = prevDirection;
        // Checking where would the player go if he continues in the same 
        // he moved in his last move(during a multiple eating move).
        if(dir > 0)
        {// Shift right
            dir = (int)(java.lang.Math.log10(dir)/java.lang.Math.log10(2));
            from = from >> dir;
        }
        else
        {// Shift left
            dir = (int)(java.lang.Math.log10(-dir)/java.lang.Math.log10(2));
            from = from << dir;
        }
        // The player can't go in the same direction he just moved in.
        mask &= ~from;
        possible = mask;
        return mask;
    }

    public void checkWin()
    {
        if(black.state == 0)
            JOptionPane.showMessageDialog(panel, "White won!");
        if(white.state == 0)
            JOptionPane.showMessageDialog(panel, "Black won!");
    }
    
}