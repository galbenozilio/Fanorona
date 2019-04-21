
package fanorona.Logic;

import fanorona.Gui.GamePanel;
import static fanorona.Logic.Rules.fullBoard;
import static fanorona.Logic.Rules.validMove;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.lang.Math;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Board 
{
    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int SIZE = ROWS * COLS;
    // The difference between the edges of the image and the begining of the board.
    public static final int DIF=15;
    // The difference between each cell
    public static final int SPACE = 30;
    public static int cellSize = 36;
    public String turn;
    private Player black,white;
    Image imageBlack = Toolkit.getDefaultToolkit().getImage("images/Black.png");
    Image imageWhite = Toolkit.getDefaultToolkit().getImage("images/White.png");
    Image imageSelected = Toolkit.getDefaultToolkit().getImage("images/Red.png");
    Image imageX = Toolkit.getDefaultToolkit().getImage("images/x.png");
    GamePanel panel;
    // Selected - the selected piece,prev - the previous spots the piece moved 
    // over in the last move(used only in moves with more than one capturing).
    // Possible - the possible moves a player has after an capturing move.
    long selected = 0,prev = 0,possible = 0;
    private boolean anotherMove = false;
    private boolean choose = false;
    // Captureing in my direction, capturing in opposite direction.
    private long capture1,capture2;
    private int selectedRow,selectedCol;
    private Rules gameRules;
    // The previous direction the player moved to in his multiple capturing move.
    private int prevDirection;
    // A queue containing all the possible moves from the current possition.
    private ArrayList<Move> moves;
    private int infinity = Integer.MAX_VALUE;
    private Ai ai = new Ai();
    // The depth of search, depth = 5 for the main board.
    public static int depth = 5;
    // If this is the time to start the Ai
    public boolean startAi = false;
    // If it is time to start the endGame strategy.
    public static boolean endGame = false;
    // If the ai is in a losing state when the endGame begins it will enter a 
    // defensive mode trting to achive a tie.
    private static boolean defensiveMode = false;
    // If there are 15 moves in a row without capturnig, the game ends in a tie.
    public static int tie = 0;

    public boolean isEndGame()
    {
        return endGame;
    }
    
    public Board(GamePanel panel)
    {
        black = new Player(0x000000000297ffffL);//0x0000000052BFFFFL);//);//1);*/1024);
        white = new Player(0x00001ffffd280000L);//0x00001FFFFA940000L);//);//0x8101004L);*/0x12008002000L);
        this.panel = panel;
        turn = "pw";
        this.gameRules = new Rules();
    }
    
    /**
     * Copy constructor. 
     */
    public Board(Board b)
    {
        this.black = new Player(b.black);
        this.white = new Player(b.white);
        this.panel = b.panel;
        this.turn = b.turn;
        this.gameRules = new Rules();
        depth--;
    }
    
    /**
     * A constructor used for multiple captures moves the Ai makes.
     * Gets the players state,the turn, the previous direction the player moved 
     * in and the previous locations he visited.
     */
    public Board(long white,long black,String turn,int prev,int prevDir)
    {
        this.black = new Player(black);
        this.white = new Player(white);
        this.turn = turn;
        this.prev = prev;
        this.prevDirection = prevDir;
        this.gameRules = new Rules();
        depth--;
    }

    public void paint(Graphics gr) 
    {
        //System.out.println("started painting");
        //System.out.println("white: "+this.white.state);
        //System.out.println("black: "+this.black.state);
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
            if(capture1 != 0 && capture2 != 0 && ((capture1 & mask) != 0 || (capture2 & mask) != 0))
            {
                gr.drawImage(imageX,i%COLS*(cellSize+SPACE)+DIF,i/COLS*(cellSize+SPACE)+DIF - (i/COLS*3),cellSize,cellSize,panel);
            }
        }
        // Paints a piece in red if it is selected.
        if(selected != 0)
            gr.drawImage(imageSelected,selectedCol*(cellSize+SPACE)+DIF,selectedRow*(cellSize+SPACE)+DIF - (selectedRow*3),cellSize,cellSize,panel);
    }
    
    
    /**
     * Checks if a spot on the board is empty.
     */
    public boolean isEmpty(long to)
    {
        return ((to & white.state) == 0 && (to & black.state) == 0);
    }
    
    /**
     * Checks if a spot on the board is empty.
     * Gets a location, and the state of the board
     */
    public boolean isEmpty(long to,long board)
    {
        return (to & board) == 0;
    }
    
    public void Click(int row, int col)
    {
        if(turn.equals("pw"))
        {
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
                if((mask & capture1) !=0 || ((mask & capture2) !=0))
                {
                    if((mask & capture1) !=0)
                        opPlayer.state ^= capture1;
                    else if((mask & capture2) !=0)
                        opPlayer.state ^= capture2;
                    capture1 = 0;
                    capture2 = 0;
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
                    capture1 = gameRules.capturingInMyDirection(selected,mask,opPlayer.state);
                    capture2 = gameRules.capturingInOppositeDirection(selected,mask,opPlayer.state);
                    prev |= selected;
                    this.prevDirection = (int) (selected>mask? selected/mask:-mask/selected);
                    selected = mask;
                    selectedRow = row;
                    selectedCol = col;
                    anotherMove = (capture1 != 0 || capture2 != 0);
                    if(capture1 == 0 && capture2 == 0)
                    {// If the move is not an capturing move
                        if(endGame = false && ai.countBits(white.state) + ai.countBits(black.state) <= 14)
                            startEndGame();
                        selected = 0;
                        tie++;
                        endTurn();
                    }
                    else if(capture1 != 0 && capture2 != 0)
                    {
                        choose = true;
                        tie = 0;
                    }
                    else
                    {
                        opPlayer.state ^= capture1;
                        opPlayer.state ^= capture2;
                        capture1 = 0;
                        capture2 = 0;
                        tie = 0;
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
        
    }
    
    public void endTurn()
    {
        turn = turn.equals("pw")?"pb":"pw";
        if(turn.equals("pb") && depth == 5)
            startAi = true;
    }
    
    public Board startAi()
    {
        if(endGame = false && ai.countBits(white.state) + ai.countBits(black.state) <= 7)
            startEndGame();
        Board newBoard = new Board(this);
        getOrderedMoves();
        int best = -infinity;
        if(moves.size() != 0)
        {
            int size = this.moves.size();
            // Holds the best possible move.
            Move bestMove = this.moves.get(0);
            //System.out.println("starting the Ai");
            //System.out.println("number of possible moves: "+this.moves.size());
            for(int i = 0;i < size;i++)
            {
                Move m = this.getNextMove();
                Board b = makeMove(m);
                int eval = this.ai.alphaBeta(b,4, -infinity, infinity );
                if(defensiveMode = true)
                {
                    long cur = turn.equals("pw")? white.state:black.state;
                    long newCur = turn.equals("pw")? b.getWhiteState():b.getBlackState();
                    eval -= 10*(ai.checkThreats(b));
                }
                //System.out.println("finished frist eval");
                if(eval >= best)
                {
                    best = eval;
                    bestMove = m;
                }
            }
            printMove(bestMove);
            if(endGame = false && bestMove.getCapture() == 0 && ai.countBits(white.state) + ai.countBits(black.state) <= 14)
                startEndGame();
            if(bestMove.getCapture() == 0)
                tie++;
            else
                tie = 0;
            newBoard = makeMove(bestMove);
            newBoard.depth = 5;  
            newBoard.startAi = false;
        }
        newBoard.turn = "pw";
        return newBoard;
    }
    
    public void printMove(Move m)
    {
        System.out.println("from: " + (java.lang.Math.log10(m.getFrom())/java.lang.Math.log10(2)+1));
        System.out.println("to: " + (java.lang.Math.log10(m.getTo())/java.lang.Math.log10(2)+1));
        System.out.println("capture: " + m.getCapture());
        if(m.getExtraCapture() != null)
            printMove(m.getExtraCapture());
        
    }
    
    public void getOneMoreMove()
    {
        if(!choose)
        {
            if(anotherMove)
            {
                if(checkCaptureingPossibilities(selected) == 0)
                {
                    anotherMove = false;
                    selected = 0;
                    selectedRow = 0;
                    selectedCol = 0;
                    prev = 0;
                    endTurn();
                }
            }
            else
                prev = 0;
        }
    }
    
    /**
     * Checks if the selected piece can do a capturing move.
     * This method is used after one or more captures, because a player gets an 
     * extra move after capturing only if he has another capturing move he can make 
     * using the same piece.
     * First we check if the move is valid - if the space is empty and the piece
     * can move there, then we check if the player can perform an capturing move in 
     * that direction. If he can, this move is added to the possible moves mask.
     */
    public long checkCaptureingPossibilities(long from)
    {
        Player opPlayer = turn.equals("pw")? black:white;
        long mask = 0;
        if(gameRules.validMove(from/*selected*/,from >> 1) && isEmpty(from >> 1))
            if(gameRules.capturingInMyDirection(from, from >> 1,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from >> 1,opPlayer.state) != 0)
                 mask |= from >> 1;
        if(gameRules.validMove(from,from << 1) && isEmpty(from << 1))
            if(gameRules.capturingInMyDirection(from, from << 1,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from << 1,opPlayer.state) != 0)
                 mask |= from << 1;
        if(gameRules.validMove(from,from >> 8) && isEmpty(from >> 8))
            if(gameRules.capturingInMyDirection(from, from >> 8,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from >> 8,opPlayer.state) != 0)
                 mask |= from >> 8;
        if(gameRules.validMove(from,from << 8) && isEmpty(from << 8))
            if(gameRules.capturingInMyDirection(from, from << 8,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from << 8,opPlayer.state) != 0)
                 mask |= from << 8;
        if(gameRules.validMove(from,from >> 9) && isEmpty(from >> 9))
            if(gameRules.capturingInMyDirection(from, from >> 9,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from >> 9,opPlayer.state) != 0)
                 mask |= from >> 9;
        if(gameRules.validMove(from,from << 9) && isEmpty(from << 9))
            if(gameRules.capturingInMyDirection(from, from << 9,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from << 9,opPlayer.state) != 0)
                 mask |= from << 9;
        if(gameRules.validMove(from,from >> 10) && isEmpty(from >> 10))
            if(gameRules.capturingInMyDirection(from, from >> 10,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from >> 10,opPlayer.state) != 0)
                 mask |= from >> 10;
        if(gameRules.validMove(from,from << 10) && isEmpty(from << 10))
            if(gameRules.capturingInMyDirection(from, from << 10,opPlayer.state) != 0|| gameRules.capturingInOppositeDirection(from, from << 10,opPlayer.state) != 0)
                 mask |= from << 10;
        //So we don't get numbers that are outside of the board.
        mask &= fullBoard;
        // The player can't go to a place he has already been to in the current move.
        mask &= ~prev;
         // The previous direction the player moved in.
        int dir = prevDirection;
        // Checking where would the player go if he continues in the same 
        // he moved in his last move(during a multiple capturing move).
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

    /**
     * Checks if there is a winner.
     * Returns 'pw' if white won,'pb' if black won and 'n' if the game isn't over.
     */
    public String checkWin()
    {
        if(black.state == 0)
        {
            if(depth == 5)
                JOptionPane.showMessageDialog(panel, "White won!");
            return "pw";
        }
        if(white.state == 0)
        {
            if(depth == 5)
                JOptionPane.showMessageDialog(panel, "Black won!");
            return "pb";
        }
        if(tie >= 15)
        {
            if(depth == 5)
                JOptionPane.showMessageDialog(panel, "It's a tie!");
            return "t";
        }
        return "n";
    }
    
    /**
     * Returns the state of the black player. 
     */
    public long getBlackState()
    {
        return this.black.state;
    }
    
    /**
     * Returns the state of the white player. 
     */
    public long getWhiteState()
    {
        return this.white.state;
    }
    
    /**
     * Gets a move and returns a new Board after the move was done.
     */
    public Board makeMove(Move m)
    {
        Board b = new Board(this);
        long from,to;
        // cur = current player,op = opponent player.
        Player cur,op;
        from = m.getFrom();
        to = m.getTo();
        cur = this.turn.equals("pw")?b.white:b.black;
        op = this.turn.equals("pw")?b.black:b.white;
        long x = from;
        x = ~x;
        cur.state &= x;
        cur.state |= to;
        if(m.getCapture() != 0)
        {
            op.state ^= m.getCapture();
            while(m.getExtraCapture() != null)
            {
                m = m.getExtraCapture();
                x = m.getFrom();
                x = ~x;
                cur.state &= x;
                cur.state |= m.getTo();
                op.state ^= m.getCapture();
            }
        }
        b.turn = b.turn.equals("pw")?"pb":"pw";
        return b;
    }
    
    public void getOrderedMoves()
    {
        this.moves = new ArrayList<Move>();
        if(this.turn.equals("pw"))
        {
            // For loop that goes over every piece that the white player has on 
            // the board.
            // Is there a way to make this loop more efficient?
            for(long i = (long)Math.pow(2, 45);i > 0;i /= 2)
                if((this.white.state & i) != 0)
                    checkPossibilities(i);
        }
        else
        {
            // For loop that goes over every piece that the black player has on 
            // the board.
            for(long i = 1;i <= this.black.state;i *= 2)
                if((this.black.state & i) != 0)
                    checkPossibilities(i);
        }
    }
    
    /**
     * Checks what moves the selected piece can do.
     * This method is used to help create a list of all the possible moves a 
     * player can perform.
     * First we check if the move is valid - if the space is empty and the piece
     * can move there. If the move is valid, it is added to the possible moves
     * list.
     */
    public void checkPossibilities(long from)
    {
        Player opPlayer = turn.equals("pw")? black:white;
        Player curPlayer = turn.equals("pw")? white:black;
        // A variable used to check if the move is an capturing move.
        long capture;
        Board copy;
        // ArryList that contains all the possible multiple eating moves for 
        // every move.
        ArrayList<Move> a = new ArrayList<Move>();
        if(gameRules.validMove(from,from >> 1) && isEmpty(from >> 1))
        {
            capture= gameRules.capturingInMyDirection(from, from >> 1,opPlayer.state);
            if(capture != 0)
            {
                int dir = (int) (from>from>>1? from/from>>1:-from/from>>1);
                long x = from;
                x = ~x;
                long cur = curPlayer.state,op = opPlayer.state;
                cur &= x;
                cur |= from >> 1;
                op ^= capture;
                a = getExtraCaptures(from>>1,from|from>>1,dir,cur,op);
                if(a.size() != 0)
                {
                    for(int i = 0;i < a.size();i++)
                        this.moves.add(new Move(from,from>>1,capture,a.get(i)));
                }
                else
                    this.moves.add(new Move(from,from>>1,capture,null));
            }
            else
                this.moves.add(new Move(from,from>>1,capture,null));
            capture= gameRules.capturingInOppositeDirection(from, from >> 1,opPlayer.state);
            if(capture != 0)
            {
                int dir = (int) (from>from>>1? from/from>>1:-from/from>>1);
                long x = from;
                x = ~x;
                long cur = curPlayer.state,op = opPlayer.state;
                cur &= x;
                cur |= from >> 1;
                op ^= capture;
                a = getExtraCaptures(from>>1,from|from>>1,dir,cur,op);
                if(a.size() != 0)
                {
                    for(int i = 0;i < a.size();i++)
                        this.moves.add(new Move(from,from>>1,capture,a.get(i)));
                }
                else
                    this.moves.add(new Move(from,from>>1,capture,null));
            }
        }
        if(gameRules.validMove(from,from << 1) && isEmpty(from << 1))
        {
            capture= gameRules.capturingInMyDirection(from, from << 1,opPlayer.state);
            if(capture != 0)
            {
                int dir = (int) (from>from<<1? from/from<<1:-from/from<<1);
                long x = from;
                x = ~x;
                long cur = curPlayer.state,op = opPlayer.state;
                cur &= x;
                cur |= from << 1;
                op ^= capture;
                a = getExtraCaptures(from<<1,from|from<<1,dir,cur,op);
                if(a.size() != 0)
                {
                    for(int i = 0;i < a.size();i++)
                        this.moves.add(new Move(from,from<<1,capture,a.get(i)));
                }
                else
                    this.moves.add(new Move(from,from<<1,capture,null));
            }
            else
                this.moves.add(new Move(from,from<<1,capture,null));
            capture= gameRules.capturingInOppositeDirection(from, from << 1,opPlayer.state);
            if(capture != 0)
            {
                int dir = (int) (from>from<<1? from/from<<1:-from/from<<1);
                long x = from;
                x = ~x;
                long cur = curPlayer.state,op = opPlayer.state;
                cur &= x;
                cur |= from << 1;
                op ^= capture;
                a = getExtraCaptures(from<<1,from|from<<1,dir,cur,op);
                if(a.size() != 0)
                {
                    for(int i = 0;i < a.size();i++)
                        this.moves.add(new Move(from,from<<1,capture,a.get(i)));
                }
                else
                    this.moves.add(new Move(from,from<<1,capture,null));
            }
        }
        for(int i = 8;i <= 10;i++)
        {
            if(gameRules.validMove(from,from >> i) && isEmpty(from >> i))
            {
                capture= gameRules.capturingInMyDirection(from, from >> i,opPlayer.state);
                if(capture != 0)
                {
                    int dir = (int) (from>from>>i? from/from>>i:-from/from>>i);
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state,op = opPlayer.state;
                    cur &= x;
                    cur |= from >> i;
                    op ^= capture;
                    a = getExtraCaptures(from>>i,from|from>>i,dir,cur,op);
                    if(a.size() != 0)
                    {
                        for(int j = 0;j < a.size();j++)
                            this.moves.add(new Move(from,from>>i,capture,a.get(j)));
                    }
                    else
                        this.moves.add(new Move(from,from>>i,capture,null));
                }
                else
                    this.moves.add(new Move(from,from>>i,capture,null));
                capture= gameRules.capturingInOppositeDirection(from, from >> i,opPlayer.state);
                if(capture != 0)
                {
                    int dir = (int) (from>from>>i? from/from>>i:-from/from>>i);
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state,op = opPlayer.state;
                    cur &= x;
                    cur |= from >> i;
                    op ^= capture;
                    a = getExtraCaptures(from>>i,from|from>>i,dir,cur,op);
                    if(a.size() != 0)
                    {
                        for(int j = 0;j < a.size();j++)
                            this.moves.add(new Move(from,from>>i,capture,a.get(j)));
                    }
                    else
                        this.moves.add(new Move(from,from>>i,capture,null));
                }
            }
            if(gameRules.validMove(from,from << i) && isEmpty(from << i))
            {
                capture= gameRules.capturingInMyDirection(from, from << i,opPlayer.state);
                if(capture != 0)
                {
                    int dir = (int) (from>from<<i? from/from<<i:-from/from<<i);
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state,op = opPlayer.state;
                    cur &= x;
                    cur |= from << i;
                    op ^= capture;
                    a = getExtraCaptures(from<<i,from|from<<i,dir,cur,op);
                    if(a.size() != 0)
                    {
                        for(int j = 0;j < a.size();j++)
                            this.moves.add(new Move(from,from<<i,capture,a.get(j)));
                    }
                    else
                        this.moves.add(new Move(from,from<<i,capture,null));
                }
                else
                    this.moves.add(new Move(from,from<<i,capture,null));
                capture= gameRules.capturingInOppositeDirection(from, from << i,opPlayer.state);
                if(capture != 0)
                {
                    int dir = (int) (from>from<<i? from/from<<i:-from/from<<i);
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state,op = opPlayer.state;
                    cur &= x;
                    cur |= from << i;
                    op ^= capture;
                    a = getExtraCaptures(from<<i,from|from<<i,dir,cur,op);
                    if(a.size() != 0)
                    {
                        for(int j = 0;j < a.size();j++)
                            this.moves.add(new Move(from,from<<i,capture,a.get(j)));
                    }
                    else
                        this.moves.add(new Move(from,from<<i,capture,null));
                }
            }
        }
    }
    
    /**
     * Returns true if the board has more possible moves and false if not. 
     */
    public boolean hasMoreMoves()
    {
        return !(this.moves.isEmpty());
    }
    
    /**
     * Returns the first move from the moves list and removes it from the list.
     */
    public Move getNextMove()
    {
        return this.moves.remove(0);
    }
    
    public void printMe(long w,long b)
    {
        String white = Long.toBinaryString(w);
        String black = Long.toBinaryString(b);
        int counter = 0;
        for(int i=0;i<5;i++)
        {
            for(int j=0;j<=8;j++)
            {
                char wh='0',bl='0';
                if(j+i*9<white.length())
                    wh = white.charAt(white.length()-1-(j+i*9));
                else
                    wh = '0';
                if(j+i*9<black.length())
                    bl = black.charAt(black.length()-1-(j+i*9));
                else
                    bl = '0';
                if(wh!='0')
                 System.out.print("|w");
                else if (bl!='0')
                        System.out.print("|b");
                else
                    System.out.print("| ");
            }
            System.out.println("|");
        }
        System.out.println("");
    }
    
    /**
     * Returns an array list of all the possible capturing moves a piece can do 
     * after making a capture move.
     * Gets a starting location and a long number that holds all the possible
     * steps that cause a capturing.
     * prev - the previous locations the player visited (can't be revisited in 
     * the current move).
     * prevDir - the previous direction the player moved in - can't repeat that 
     * direction in the current move).
     * cur - current player state,op - opponent player state.
     */
    public ArrayList<Move> getExtraCaptures(long from,long prev,int prevDir,long cur,long op)
    {
        ArrayList<Move> a = new ArrayList<Move>();
        // forbidden - the move the player can't make.
        long c1,c2,forbidden;
        if(prevDir > 0)
        {// Shift right
            prevDir = (int)(java.lang.Math.log10(prevDir)/java.lang.Math.log10(2));
            forbidden = from >> prevDir;
        }
        else
        {// Shift left
            prevDir = (int)(java.lang.Math.log10(-prevDir)/java.lang.Math.log10(2));
            forbidden = from << prevDir;
        }
        if(gameRules.validMove(from,from >> 1) && isEmpty(from >> 1,cur|op) 
                && (prev & from >> 1) == 0 && (from >> 1) != forbidden)
        {
            c1 = gameRules.capturingInMyDirection(from, from >> 1,op);
            c2 = gameRules.capturingInOppositeDirection(from, from >> 1,op);
            if(c1 != 0)
                a.add(new Move(from,from>>1,c1,null));
            if(c2 != 0)
                a.add(new Move(from,from>>1,c2,null));
        }
        if(gameRules.validMove(from,from << 1) && isEmpty(from << 1,cur|op)
                && (prev & from << 1) == 0 && (from << 1) != forbidden)
        {
            c1 = gameRules.capturingInMyDirection(from, from << 1,op);
            c2 = gameRules.capturingInOppositeDirection(from, from << 1,op);
            if(c1 != 0)
                a.add(new Move(from,from<<1,c1,null));
            if(c2 != 0)
                a.add(new Move(from,from<<1,c2,null));
        }
        for(int i = 8;i <= 10;i++)
        {
            if(gameRules.validMove(from,from >> i) && isEmpty(from >> i,cur|op)
                    && (prev & from >> i) == 0 && (from >> i) != forbidden)
            {
                c1 = gameRules.capturingInMyDirection(from, from >> i,op);
                c2 = gameRules.capturingInOppositeDirection(from, from >> i,op);
                if(c1 != 0)
                    a.add(new Move(from,from>>i,c1,null));
                if(c2 != 0)
                    a.add(new Move(from,from>>i,c2,null));
            } 
            if(gameRules.validMove(from,from << i) && isEmpty(from << i,cur|op)
                    && (prev & from << i) == 0 && (from << i) != forbidden)
            {
                c1 = gameRules.capturingInMyDirection(from, from << i,op);
                c2 = gameRules.capturingInOppositeDirection(from, from << i,op);
                if(c1 != 0)
                    a.add(new Move(from,from<<i,c1,null));
                if(c2 != 0)
                    a.add(new Move(from,from<<i,c2,null));
            }
        }
        for(int i = 0;i < a.size();i++)
        {
            Move m = a.get(i);
            int dir = (int) (m.getFrom()>m.getTo()? m.getFrom()/m.getTo():-m.getTo()/m.getFrom()); 
            long x = m.getFrom();
            x = ~x;
            long copyCur = cur,copyOp = op;
            copyCur &= x;
            copyCur |= m.getTo();
            if(m.getCapture() != 0)
                copyOp ^= m.getCapture();
            ArrayList<Move> b = getExtraCaptures(m.getTo(),prev|m.getTo(),dir,copyCur,copyOp);
            for(int j = 0;j < b.size();j++)
            {
                if(j == 0)
                    m.setExtraCapture(b.get(j));
                else
                {
                    Move m2 = new Move(m);
                    m2.setExtraCapture(b.get(j));
                }
            }
        }
        return a;
    }
    
    public void startEndGame()
    {
        endGame = true;
        long cur = turn.equals("pw")? white.state:black.state;
        long op = turn.equals("pw")? black.state:white.state;
        if(ai.countBits(cur) - ai.countBits(op) < 0)
            defensiveMode = true;
    }
}