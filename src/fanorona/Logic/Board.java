package fanorona.Logic;

import fanorona.Gui.GamePanel;
import static fanorona.Logic.Rules.fullBoard;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.lang.Math;

public class Board
{

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int SIZE = ROWS * COLS;
    // The difference between the edges of the image and the begining of the board.
    public static final int DIF = 15;
    // The difference between each cell
    public static final int SPACE = 30;
    // The size of a cell.
    public static final int cellSize = 36;
    public String turn;
    private Player black, white;
    Image imageBlack = Toolkit.getDefaultToolkit().getImage("images/Black.png");
    Image imageWhite = Toolkit.getDefaultToolkit().getImage("images/White.png");
    Image imageSelected = Toolkit.getDefaultToolkit().getImage("images/Red.png");
    Image imageX = Toolkit.getDefaultToolkit().getImage("images/x.png");
    GamePanel panel;
    // Selected - the selected piece,prev - the previous spots the piece moved 
    // over in the last move(used only in moves with more than one capturing).
    // Possible - the possible moves a player has after an capturing move.
    private long selected = 0, prev = 0, possible = 0;
    private boolean anotherMove = false;
    private boolean choose = false;
    // Captureing in my direction, capturing in opposite direction.
    private long capture1, capture2;
    private int selectedRow, selectedCol;
    // The previous direction the player moved to in his multiple capturing move.
    private int prevDirection;
    // A queue containing all the possible moves from the current possition.
    private ArrayList<Move> moves;
    // The biggest integer value availble by java.
    private int infinity = Integer.MAX_VALUE;
    private Ai ai = new Ai();
    // The depth of search, depth = 5 for the main board.
    public int depth = 5;
    // If this is the time to start the Ai
    public boolean startAi = false;
    // If it is time to start the endGame strategy.
    public static boolean endGame = false;
    // If the ai is in a losing state when the endGame begins it will enter a 
    // defensive mode trting to achive a tie.
    public static boolean defensiveMode = false;
    // If there are 15 moves in a row without capturnig, the game ends in a tie.
    public int tie = 0;

    public Board(GamePanel panel)
    {
        black = new Player(/*0x2000L);/*);0x1001005L);*/0x000000000297ffffL);
        white = new Player(/*0x2001E3L);/*0x4008000020L);*/0x00001ffffd280000L);
        this.panel = panel;
        turn = "pw";
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
        depth--;
        this.tie = b.tie;
    }

    /**
     * Returns the boolean endGame, true if the endgame stage has began and
     * false if not.
     */
    public boolean getEndGame()
    {
        return endGame;
    }
    
    /**
     * Returns defensiveMode.
     */
    public boolean isDefensiveMode()
    {
        return defensiveMode;
    }
    
    /**
     * Returns the the black player.
     */
    public Player getBlack()
    {
        return black;
    }
    
    /**
     * Returns the the white player.
     */
    public Player getWhite()
    {
        return white;
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
     * A constructor used for multiple captures moves the Ai makes. Gets the
     * players state,the turn, the previous direction the player moved in and
     * the previous locations he visited.
     *
     * public Board(long white,long black,String turn,int prev,int prevDir) {
     * this.black = new Player(black); this.white = new Player(white); this.turn
     * = turn; this.prev = prev; this.prevDirection = prevDir; this.Rules = new
     * Rules(); depth--; }
     */
    public void paint(Graphics gr)
    {
        long mask = 1;
        for (int i = 0; i < SIZE; i++, mask <<= 1)
        {
            if ((black.state & mask) != 0)
                gr.drawImage(imageBlack, i % COLS * (cellSize + SPACE) + DIF, i / COLS * (cellSize + SPACE) + DIF - (i / COLS * 3), cellSize, cellSize, panel);
            if ((white.state & mask) != 0)
                gr.drawImage(imageWhite, i % COLS * (cellSize + SPACE) + DIF, i / COLS * (cellSize + SPACE) + DIF - (i / COLS * 3), cellSize, cellSize, panel);
            if (capture1 != 0 && capture2 != 0 && ((capture1 & mask) != 0 || (capture2 & mask) != 0))
                gr.drawImage(imageX, i % COLS * (cellSize + SPACE) + DIF, i / COLS * (cellSize + SPACE) + DIF - (i / COLS * 3), cellSize, cellSize, panel);
        }
        // Paints a piece in red if it is selected.
        if (selected != 0)
            gr.drawImage(imageSelected, selectedCol * (cellSize + SPACE) + DIF, selectedRow * (cellSize + SPACE) + DIF - (selectedRow * 3), cellSize, cellSize, panel);
    }

    /**
     * Checks if a spot on the board is empty.
     */
    public boolean isEmpty(long to)
    {
        return ((to & white.state) == 0 && (to & black.state) == 0);
    }

    /**
     * Checks if a spot on the board is empty. Gets a location, and the state of
     * the board
     */
    public boolean isEmpty(long to, long board)
    {
        return (to & board) == 0;
    }

    public void Click(int row, int col)
    {
        if (turn.equals("pw"))
        {
            long mask = 1;
            mask <<= (row * 9 + col);
            Player curPlayer = turn.equals("pw") ? white : black;
            Player opPlayer = turn.equals("pw") ? black : white;
            if (!anotherMove)
            {// If the move is not a multi capture mive
                possible = Rules.fullBoard;
                this.prevDirection = 0;
            }
            if (choose)
            {// If the player needs to choose which pieces to eat. 
                if ((mask & capture1) != 0 || ((mask & capture2) != 0))
                {
                    if ((mask & capture1) != 0)
                        opPlayer.state ^= capture1;
                    else if ((mask & capture2) != 0)
                        opPlayer.state ^= capture2;
                    capture1 = 0;
                    capture2 = 0;
                    choose = false;
                    tie = 0;
                }
            } else if (selected != 0 && (mask & ~prev) != 0 && (mask & possible) != 0)
            {// If the player has chosen a piece and now wants to move it.
                if (isEmpty(mask) && Rules.validMove(selected, mask))
                {// If the move is legal.
                    long x = selected;
                    x = ~x;
                    curPlayer.state &= x;
                    curPlayer.state |= mask;
                    capture1 = Rules.capturingInMyDirection(selected, mask, opPlayer.state);
                    capture2 = Rules.capturingInOppositeDirection(selected, mask, opPlayer.state);
                    prev |= selected;
                    this.prevDirection = (int) (selected > mask ? selected / mask : -mask / selected);
                    selected = mask;
                    selectedRow = row;
                    selectedCol = col;
                    anotherMove = (capture1 != 0 || capture2 != 0);
                    if (capture1 == 0 && capture2 == 0)
                    {// If the move is not a capturing move
                        if (endGame == false && ai.countBits(white.state) + ai.countBits(black.state) <= 14)
                            startEndGame();
                        selected = 0;
                        tie++;
                        endTurn();
                    } else if (capture1 != 0 && capture2 != 0)
                    {// If the player needs to choose which pieces to eat.
                        choose = true;
                        tie = 0;
                    } else
                    {// If the move is an eating move.
                        opPlayer.state ^= capture1;
                        opPlayer.state ^= capture2;
                        capture1 = 0;
                        capture2 = 0;
                        tie = 0;
                    }
                } else if (mask == selected && !anotherMove)
                    selected = 0;
            } else if ((mask & curPlayer.state) != 0 && !anotherMove)
            {// Choosing a piece to move with
                selected = mask;
                selectedRow = row;
                selectedCol = col;
            }
        }

    }

    /**
     * Ends a turn by switching the turn. Checks if the current board is the
     * displayed board, if it is this method changes the startAi variable to be
     * true so the Ai will start working.
     */
    public void endTurn()
    {
        turn = turn.equals("pw") ? "pb" : "pw";
        if (turn.equals("pb") && depth == 5)
            startAi = true;
    }

    /**
     * Makes the Ai perform a move. Returns a new board after the chosen move.
     * The method first checks if the endGame has started, if so it decides if
     * it should use a defensive strategy. If the endGame hasn't started yet it
     * checks if the endGame should begin.
     */
    public Move startAi()
    {
        long cur = turn.equals("pw") ? white.state : black.state;
        long op = turn.equals("pb") ? white.state : black.state;
        if (endGame == true)
            // Checks if the ai is losing (at the endgame) and if so starts 
            // defensive mode. 
            defensiveMode = ai.countBits(cur) - ai.countBits(op) >= 0 ? false : true;
        if (endGame == false && ai.countBits(white.state) + ai.countBits(black.state) <= 7)
            // Checks if the ai should enter the endGame.
            startEndGame();
        //Board newBoard = new Board(this);
        getOrderedMoves();
        int best = -infinity;
        Move bestMove = null;
        if (moves.size() != 0)
        {
            int size = this.moves.size();
            // Holds the best possible move.
            bestMove = this.moves.get(0);
            for (int i = 0; i < size; i++)
            {// A loop that goes over every possible move.
                Move m = this.getNextMove();
                // Copy of the current count of non capturing moves
                int copyTie = this.tie;
                Board b = makeMove(m);
                if(b.checkWin().equals("n"))
                {
                    int eval = -this.ai.negaMax(b, 4, -infinity, infinity);
                    System.out.println("from: "+m.getFrom());
                    System.out.println("to: "+m.getTo());
                    System.out.println("capture: "+m.getCapture() );
                    //if(tie >=20)
                      //  eval += m.getCapture() != 0 ? 5 : 0;
                    /*if (defensiveMode == true)
                    {
                        long newCur = turn.equals("pw") ? b.getWhiteState() : b.getBlackState();
                        eval -= (ai.checkThreats(b));
                    }*/
                    System.out.println("value: "+eval);
                    if (eval >= best)
                    {
                        best = eval;
                        bestMove = m;
                    }
                }
                else
                {
                    bestMove = m;
                    break;
                }
                b.tie = copyTie;
            }
            if (endGame == false && bestMove.getCapture() == 0 && ai.countBits(white.state) + ai.countBits(black.state) <= 14)
                startEndGame();
            /*if (bestMove.getCapture() == 0)
                tie++;
            else
                tie = 0;*/
            //newBoard = makeMove(bestMove);
            //newBoard.depth = 5;
            //newBoard.startAi = false;
        }
        //newBoard.turn = "pw";
        //return newBoard;
        return bestMove;
    }

    /**
     * Checks if the player should get another move after performing a capturing
     * move. The method checks if there is another capturing the player can do
     * with the current piece.
     */
    public void getOneMoreMove()
    {
        if (!choose)
            if (anotherMove)
            {
                if (checkCaptureingPossibilities(selected) == 0)
                {
                    anotherMove = false;
                    selected = 0;
                    selectedRow = 0;
                    selectedCol = 0;
                    prev = 0;
                    endTurn();
                }
            } else
                prev = 0;
    }

    /**
     * Checks if the selected piece can do a capturing move. This method is used
     * after one or more captures, because a player gets an extra move after
     * capturing only if he has another capturing move he can make using the
     * same piece. First we check if the move is valid - if the space is empty
     * and the piece can move there, then we check if the player can perform an
     * capturing move in that direction. If he can, this move is added to the
     * possible moves mask.
     */
    public long checkCaptureingPossibilities(long from)
    {
        Player opPlayer = turn.equals("pw") ? black : white;
        long mask = 0;
        if (Rules.validMove(from/*selected*/, from >> 1) && isEmpty(from >> 1))
            if (Rules.capturingInMyDirection(from, from >> 1, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from >> 1, opPlayer.state) != 0)
                mask |= from >> 1;
        if (Rules.validMove(from, from << 1) && isEmpty(from << 1))
            if (Rules.capturingInMyDirection(from, from << 1, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from << 1, opPlayer.state) != 0)
                mask |= from << 1;
        if (Rules.validMove(from, from >> 8) && isEmpty(from >> 8))
            if (Rules.capturingInMyDirection(from, from >> 8, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from >> 8, opPlayer.state) != 0)
                mask |= from >> 8;
        if (Rules.validMove(from, from << 8) && isEmpty(from << 8))
            if (Rules.capturingInMyDirection(from, from << 8, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from << 8, opPlayer.state) != 0)
                mask |= from << 8;
        if (Rules.validMove(from, from >> 9) && isEmpty(from >> 9))
            if (Rules.capturingInMyDirection(from, from >> 9, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from >> 9, opPlayer.state) != 0)
                mask |= from >> 9;
        if (Rules.validMove(from, from << 9) && isEmpty(from << 9))
            if (Rules.capturingInMyDirection(from, from << 9, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from << 9, opPlayer.state) != 0)
                mask |= from << 9;
        if (Rules.validMove(from, from >> 10) && isEmpty(from >> 10))
            if (Rules.capturingInMyDirection(from, from >> 10, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from >> 10, opPlayer.state) != 0)
                mask |= from >> 10;
        if (Rules.validMove(from, from << 10) && isEmpty(from << 10))
            if (Rules.capturingInMyDirection(from, from << 10, opPlayer.state) != 0 || Rules.capturingInOppositeDirection(from, from << 10, opPlayer.state) != 0)
                mask |= from << 10;
        //So we don't get numbers that are outside of the board.
        mask &= fullBoard;
        // The player can't go to a place he has already been to in the current move.
        mask &= ~prev;
        // The previous direction the player moved in.
        int dir = prevDirection;
        // Checking where would the player go if he continues in the same 
        // he moved in his last move(during a multiple capturing move).
        if (dir > 0)
        {// Shift right
            dir = (int) (java.lang.Math.log10(dir) / java.lang.Math.log10(2));
            from = from >> dir;
        } else
        {// Shift left
            dir = (int) (java.lang.Math.log10(-dir) / java.lang.Math.log10(2));
            from = from << dir;
        }
        // The player can't go in the same direction he just moved in.
        mask &= ~from;
        possible = mask;
        return mask;
    }

    /**
     * Checks if there is a winner. Returns 'pw' if white won,'pb' if black won
     * and 'n' if the game isn't over.
     */
    public String checkWin()
    {
        if (black.state == 0)
        {
            if (depth == 5)
                JOptionPane.showMessageDialog(panel, "White won!");
            return "pw";
        }
        if (white.state == 0)
        {
            if (depth == 5)
                JOptionPane.showMessageDialog(panel, "black won!");
            return "pb";
        }
        if (tie >= 25)
        {
            if (depth == 5)
                JOptionPane.showMessageDialog(panel, "It's a tie!");
            return "t";
        }
        panel.repaint();
        return "n";
    }

    

    /**
     * Gets a move and returns a new Board after the move was done.
     */
    public Board makeMove(Move m)
    {
        Board b = new Board(this);
        long from, to;
        Player cur, op;
        from = m.getFrom();
        to = m.getTo();
        cur = this.turn.equals("pw") ? b.white : b.black;
        op = this.turn.equals("pw") ? b.black : b.white;
        long x = from;
        x = ~x;
        cur.state &= x;
        cur.state |= to;
        op.state ^= m.getCapture();
        while (m.getExtraCapture() != null)
        {
            //selected = to;
            m = m.getExtraCapture();
            x = m.getFrom();
            x = ~x;
            cur.state &= x;
            cur.state |= m.getTo();
            op.state ^= m.getCapture();
            //selected = 0;
            //panel.repaint();
        }
        if(m.getCapture() == 0)
            b.tie = this.tie + 1;
        else
            b.tie = 0;
        b.endTurn(); 
        return b;
    }

    public void getOrderedMoves()
    {
        this.moves = new ArrayList<Move>();
        if (this.turn.equals("pw"))
        {
            // For loop that goes over every piece that the white player has on 
            // the board.
            for (long i = (long) Math.pow(2, SIZE); i > 0; i /= 2)
            {
                if ((this.white.state & i) != 0)
                    checkPossibilities(i);
            }
        } else
            // For loop that goes over every piece that the black player has on 
            // the board.
            for (long i = 1; i <= this.black.state; i *= 2)
            {
                if ((this.black.state & i) != 0)
                    checkPossibilities(i);
            }
    }

    /**
     * Checks what moves the selected piece can do. This method is used to help
     * create a list of all the possible moves a player can perform.For every 
     * direction the piece can move in, first we check if the move is valid - 
     * if the space is empty and the piece can move there. 
     * If the move is valid, it is added to the possible moves list. If the 
     * move is a multiple captures move, all the multiple capturing options are 
     * added to the list. 
     */
    public void checkPossibilities(long from)
    {
        Player opPlayer = turn.equals("pw") ? black : white;
        Player curPlayer = turn.equals("pw") ? white : black;
        // A variable used to check if the move is an capturing move.
        long capture1,capture2;
        Board copy;
        // ArryList that contains all the possible multiple eating moves for 
        // every move.
        ArrayList<Move> a = new ArrayList<Move>();
        if (Rules.validMove(from, from >> 1) && isEmpty(from >> 1))
        {
            capture1 = Rules.capturingInMyDirection(from, from >> 1, opPlayer.state);
            capture2 = Rules.capturingInOppositeDirection(from, from >> 1, opPlayer.state);
            if (capture1 != 0)
            {// If the move is an eating move, the algorithm than checks if
             // the player can make multiple captures in this move.
                int dir = 1;//from > from >> 1 ? from / from >> 1 : -(from >> 1 / from)); // -from / from >> 1);
                long x = from;
                x = ~x;
                long cur = curPlayer.state, op = opPlayer.state;
                cur &= x;
                cur |= from >> 1;
                op ^= capture1;
                a = getExtraCaptures(from >> 1, from | from >> 1, dir, cur, op);
                if (a.size() != 0)
                    for (int i = 0; i < a.size(); i++)
                    {//For loop that adds every multiple capture1 option.
                        this.moves.add(new Move(from, from >> 1, capture1, a.get(i)));
                    }
                else 
                    this.moves.add(new Move(from, from >> 1, capture1, null));
            } else if(capture2 == 0)
                this.moves.add(new Move(from, from >> 1, capture1, null));
            if (capture2 != 0)
            {// If the move is an eating move, the algorithm than checks if
             // the player can make multiple captures in this move.
                int dir = 1;//-from / from >> 1);
                long x = from;
                x = ~x;
                long cur = curPlayer.state, op = opPlayer.state;
                cur &= x;
                cur |= from >> 1;
                op ^= capture2;
                a = getExtraCaptures(from >> 1, from | from >> 1, dir, cur, op);
                if (a.size() != 0)
                    for (int i = 0; i < a.size(); i++)
                    {//For loop that adds every multiple capture option.
                        this.moves.add(new Move(from, from >> 1, capture2, a.get(i)));
                    }
                else
                    this.moves.add(new Move(from, from >> 1, capture2, null));
            }
        }
        if (Rules.validMove(from, from << 1) && isEmpty(from << 1))
        {
            capture1 = Rules.capturingInMyDirection(from, from << 1, opPlayer.state);
            capture2 = Rules.capturingInOppositeDirection(from, from << 1, opPlayer.state);
            if (capture1 != 0)
            {// If the move is an eating move, the algorithm than checks if
             // the player can make multiple captures in this move.
                int dir = -1;
                long x = from;
                x = ~x;
                long cur = curPlayer.state, op = opPlayer.state;
                cur &= x;
                cur |= from << 1;
                op ^= capture1;
                a = getExtraCaptures(from << 1, from | from << 1, dir, cur, op);
                if (a.size() != 0)
                    for (int i = 0; i < a.size(); i++)
                    {//For loop that adds every multiple capture option.
                        this.moves.add(new Move(from, from << 1, capture1, a.get(i)));
                    }
                else
                    this.moves.add(new Move(from, from << 1, capture1, null));
            } else if(capture2 == 0)
                this.moves.add(new Move(from, from << 1, capture1, null));
            if (capture2 != 0)
            {// If the move is an eating move, the algorithm than checks if
             // the player can make multiple captures in this move.
                int dir = -1;
                long x = from;
                x = ~x;
                long cur = curPlayer.state, op = opPlayer.state;
                cur &= x;
                cur |= from << 1;
                op ^= capture2;
                a = getExtraCaptures(from << 1, from | from << 1, dir, cur, op);
                if (a.size() != 0)
                    for (int i = 0; i < a.size(); i++)
                    {//For loop that adds every multiple capture option.
                        this.moves.add(new Move(from, from << 1, capture2, a.get(i)));
                    }
                else
                    this.moves.add(new Move(from, from << 1, capture2, null));
            }
        }
        for (int i = 8; i <= 10; i++)
        {
            if (Rules.validMove(from, from >> i) && isEmpty(from >> i))
            {
                capture1 = Rules.capturingInMyDirection(from, from >> i, opPlayer.state);
                capture2 = Rules.capturingInOppositeDirection(from, from >> i, opPlayer.state);
                if (capture1 != 0)
                {// If the move is an eating move, the algorithm than checks if
                 // the player can make multiple captures in this move.
                    int dir = i;
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state, op = opPlayer.state;
                    cur &= x;
                    cur |= from >> i;
                    op ^= capture1;
                    a = getExtraCaptures(from >> i, from | from >> i, dir, cur, op);
                    if (a.size() != 0)
                        for (int j = 0; j < a.size(); j++)
                        {//For loop that adds every multiple capture option.
                            this.moves.add(new Move(from, from >> i, capture1, a.get(j)));
                        }
                    else
                        this.moves.add(new Move(from, from >> i, capture1, null));
                } else if(capture2 == 0)
                    this.moves.add(new Move(from, from >> i, capture1, null));
                if (capture2 != 0)
                {// If the move is an eating move, the algorithm than checks if
                 // the player can make multiple captures in this move.
                    int dir = i;
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state, op = opPlayer.state;
                    cur &= x;
                    cur |= from >> i;
                    op ^= capture2;
                    a = getExtraCaptures(from >> i, from | from >> i, dir, cur, op);
                    if (a.size() != 0)
                        for (int j = 0; j < a.size(); j++)
                        {//For loop that adds every multiple capture option.
                            this.moves.add(new Move(from, from >> i, capture2, a.get(j)));
                        }
                    else
                        this.moves.add(new Move(from, from >> i, capture2, null));
                }
            }
            if (Rules.validMove(from, from << i) && isEmpty(from << i))
            {
                capture1 = Rules.capturingInMyDirection(from, from << i, opPlayer.state);
                capture2 = Rules.capturingInOppositeDirection(from, from << i, opPlayer.state);
                if (capture1 != 0)
                {
                    int dir = -i;
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state, op = opPlayer.state;
                    cur &= x;
                    cur |= from << i;
                    op ^= capture1;
                    a = getExtraCaptures(from << i, from | from << i, dir, cur, op);
                    if (a.size() != 0)
                        for (int j = 0; j < a.size(); j++)
                        {//For loop that adds every multiple capture option.
                            this.moves.add(new Move(from, from << i, capture1, a.get(j)));
                        }
                    else
                        this.moves.add(new Move(from, from << i, capture1, null));
                } else if(capture2 == 0)
                    this.moves.add(new Move(from, from << i, capture1, null));
                if (capture2 != 0)
                {
                    int dir = -i;
                    long x = from;
                    x = ~x;
                    long cur = curPlayer.state, op = opPlayer.state;
                    cur &= x;
                    cur |= from << i;
                    op ^= capture2;
                    a = getExtraCaptures(from << i, from | from << i, dir, cur, op);
                    if (a.size() != 0)
                        for (int j = 0; j < a.size(); j++)
                        {//For loop that adds every multiple capture option.
                            this.moves.add(new Move(from, from << i, capture2, a.get(j)));
                        }
                    else
                        this.moves.add(new Move(from, from << i, capture2, null));
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

    /**
     * Returns an array list of all the possible capturing moves a piece can do
     * after making a capture move. Gets a starting location and returns a long 
     * number that holds all the possible steps that cause a capturing. 
     * For every moving direction, the algorithm checks if the piece can move 
     * in that direction, if so, it checks if moving in that direction causes
     * capturing of enemy pieces, and the direction isn't the same direction 
     * this piece just moved in, and if the piece doesn't return to a location
     * it already visited in this move. if so the move is added to the list of 
     * possible moves.
     * prev - the previous locations the player visited (can't be revisited 
     * in the current move). 
     * prevDir - the previous direction the player moved in - can't repeat that 
     * direction in the current move). cur - current player state.
     * op - opponent player's state.
     */
    public ArrayList<Move> getExtraCaptures(long from, long prev, int prevDir, long cur, long op)
    {//לבדוק האם עשיתי פה את הבדיקה של חזרה על כיוון
        ArrayList<Move> a = new ArrayList<Move>();
        // forbidden - the move the player can't make.
        long c1, c2, forbidden;
        if (prevDir > 0)
        {// Shift right
            //prevDir = (int) (java.lang.Math.log10(prevDir) / java.lang.Math.log10(2));
            forbidden = from >> prevDir;
        } else
        {// Shift left
            //prevDir = (int) (java.lang.Math.log10(-prevDir) / java.lang.Math.log10(2));
            forbidden = from << prevDir;
        }
        if (Rules.validMove(from, from >> 1) && isEmpty(from >> 1, cur | op)
                && (prev & from >> 1) == 0 && (from >> 1) != forbidden)
        {
            c1 = Rules.capturingInMyDirection(from, from >> 1, op);
            c2 = Rules.capturingInOppositeDirection(from, from >> 1, op);
            if (c1 != 0)
                a.add(new Move(from, from >> 1, c1, null));
            if (c2 != 0)
                a.add(new Move(from, from >> 1, c2, null));
        }
        if (Rules.validMove(from, from << 1) && isEmpty(from << 1, cur | op)
                && (prev & from << 1) == 0 && (from << 1) != forbidden)
        {
            c1 = Rules.capturingInMyDirection(from, from << 1, op);
            c2 = Rules.capturingInOppositeDirection(from, from << 1, op);
            if (c1 != 0)
                a.add(new Move(from, from << 1, c1, null));
            if (c2 != 0)
                a.add(new Move(from, from << 1, c2, null));
        }
        for (int i = 8; i <= 10; i++)
        {
            if (Rules.validMove(from, from >> i) && isEmpty(from >> i, cur | op)
                    && (prev & from >> i) == 0 && (from >> i) != forbidden)
            {
                c1 = Rules.capturingInMyDirection(from, from >> i, op);
                c2 = Rules.capturingInOppositeDirection(from, from >> i, op);
                if (c1 != 0)
                    a.add(new Move(from, from >> i, c1, null));
                if (c2 != 0)
                    a.add(new Move(from, from >> i, c2, null));
            }
            if (Rules.validMove(from, from << i) && isEmpty(from << i, cur | op)
                    && (prev & from << i) == 0 && (from << i) != forbidden)
            {
                c1 = Rules.capturingInMyDirection(from, from << i, op);
                c2 = Rules.capturingInOppositeDirection(from, from << i, op);
                if (c1 != 0)
                    a.add(new Move(from, from << i, c1, null));
                if (c2 != 0)
                    a.add(new Move(from, from << i, c2, null));
            }
        }
        for (int i = 0; i < a.size(); i++)
        {// add extra capturings to every move that has extra capturing options.
            Move m = a.get(i);
            int dir;
            if(m.getFrom() > m.getTo())
            {
                dir = (int)(m.getFrom() / m.getTo());
                dir = (int)(java.lang.Math.log10(dir)/java.lang.Math.log10(2));
            }
            else
            {
                dir = (int)(-m.getTo() / m.getFrom());
                dir = (int)(java.lang.Math.log10(-dir)/java.lang.Math.log10(2));
            }
            //int dir = (int) (m.getFrom() > m.getTo() ? m.getFrom() / m.getTo() : -m.getTo() / m.getFrom());
            long x = m.getFrom();
            x = ~x;
            long copyCur = cur, copyOp = op;
            copyCur &= x;
            copyCur |= m.getTo();
            if (m.getCapture() != 0)
                copyOp ^= m.getCapture();
            ArrayList<Move> b = getExtraCaptures(m.getTo(), prev | m.getTo(), dir, copyCur, copyOp);
            for (int j = 0; j < b.size(); j++)
            {
                if (j == 0)
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

    /**
     * A method that makes the Ai enter the endGame state. Checks if the Ai is
     * losing at the current moment and if it is losing it enters in to a
     * defensive mode.
     */
    public void startEndGame()
    {
        endGame = true;
        long cur = turn.equals("pw") ? white.state : black.state;
        long op = turn.equals("pw") ? black.state : white.state;
        if (ai.countBits(cur) - ai.countBits(op) < 0)
            defensiveMode = true;
    }
}
