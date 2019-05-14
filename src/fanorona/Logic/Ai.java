package fanorona.Logic;

import java.lang.Long;


public class Ai
{
    private int infinity = Integer.MAX_VALUE;
    
    /**
     * Nega Max algorithm.
     * The algorithm is used to determine the best move. It checks what move is 
     * the best move move for a player in his turn and than checks for the best 
     * move his opponent can make in his next turn (until a certain depth is 
     * reached, in our case - 4).
     * The value of each move is determined by the evaluate function.
     * After entering the endgame, the number of soldiers who could by eaten in     
     * the next move is reduced from the move's value.
     */
    public int negaMax(Board board,int depth,int alpha,int beta)
    {
        board.depth = depth;
        int value;
        if(depth == 0 || !board.checkWin().equals("n"))
        {
            value = evaluate(board);
            //if(depth == 4)
              //  System.out.println("depth0 or check win value: " +value);
            return value;
        }
        board.getOrderedMoves();
        int best = -infinity;
        Move move; 
        // Copy of the current count of non capturing moves.
        int copyTie = board.tie;
        Board nextBoard;
        while (board.hasMoreMoves())
        {
            move = board.getNextMove();
            if(depth == 4)
                nextBoard = board.makeMove(move);
            else
                nextBoard = board.makeMove(move);
            value = -negaMax(nextBoard, depth-1,-beta,-alpha);
            //Delete 
            /*if(depth == 4)
            {
                System.out.println("from: "+move.getFrom());
                System.out.println("to: "+move.getTo());
                System.out.println("capture: "+move.getCapture() );
                System.out.println("value: "+value);
                
            }*/
            //value = Board.endGame == true?value - checkThreats(nextBoard):value;
            if(value > best)
                best = value;
            if(best > alpha)
                alpha = best;
            if(best >= beta)
                break;
            board.tie = copyTie;
        }
        //System.out.println("best: " +best);
        return best;
    }
    
    /**
     * Returns an evaluation of the board for the negaMax function. 
     * the evaluation checks which player has more pieces on the board and 
     * returns the difference.  
     */
    public int evaluate(Board board)
    {
        int eval;
        // Delete
        String x;
        if(!board.checkWin().equals("n"))
            if(board.checkWin().equals("t"))
                if(Board.defensiveMode == true)
                    eval = -infinity/10;
                else
                    eval = infinity/10;
            else
                return -infinity;
        else
        {
            eval = countBits(board.getWhiteState()) - countBits(board.getBlackState());
            eval = board.turn.equals("pb")?-eval:eval;
            if(Board.endGame == true)
            {
                //board.turn = board.turn.equals("pw") ? "pb" : "pw";
                if(Board.defensiveMode == true)
                    eval = board.turn.equals("pw") ? eval - checkThreats(board):eval;
                else
                    eval = board.turn.equals("pb") ? eval - checkThreats(board):eval;
                eval = eval + countEnemyDeadEnds(board) - countMyDeadEnds(board);
                /*if(board.turn == "pb")
                {
                    // change it - no need for 5*eval;
                    //eval *= eval > 0 ? 5 : 1;
                    eval = eval - corners(board.getBlackState()) - edges(board.getBlackState());
                    eval += corners(board.getWhiteState()) - edges(board.getWhiteState());
                }
                else
                {
                    eval = eval - corners(board.getWhiteState()) - edges(board.getWhiteState());
                    eval += corners(board.getBlackState()) + edges(board.getBlackState());
                }*/
                //board.turn = board.turn.equals("pw") ? "pb" : "pw";
                // If the board is in an endGame stage, try to avoid the corners. 
                ////if(Board.defensiveMode == true)
                  //  if(board.turn == "pw")
                    //    eval = countCorners(board.getBlackState())==0?eval:-infinity;//(int)(2*eval - 10*countCorners(board.getBlackState())); //+ countEdges(board.getWhiteState());
                    //else
                      //  x = "Add later";
                //else
                    //if(board.turn == "pb")
                  //      eval = countCorners(board.getWhiteState())==0?eval:-infinity;//(int)(2*eval - 10*countCorners(board.getWhiteState()));
            }
        }
        return eval;
    }
    
    public static int countBits(long number)
    { 
        int count = 0;
        while(number>0)
        { 
            ++count; 
            number &= number-1; 
        } 
        return count;
    }
    
    public int corners(long state)
    {
        // For loop that goes over every piece that the player has on 
        // the board.
        for (long i = 1; i < state; i *= 2)
        {
            if ((state & i) != 0 && isCorner(i) == true)
                return 1;
        }
        return 0;
    }
    
    /**
     * Checks how many pieces the player has on the edges of the board.
     * Goes over every piece the player has and checks if it's on the edges.
     */
    public int edges(long state)
    {
        // For loop that goes over every piece that the player has on 
        // the board.
        for (long i = 1; i < state; i *= 2)
        {
            if ((state & i) != 0 && isEdge(i) == true)
                return 1;
        }
        return 0;
    }
    
    /**
     * Checks if a piece is in one of the corners of the board.
     * Returns 1 for yes and 0 for no.
     */
    public boolean isCorner(long piece)
    {
        if((piece & Rules.up) != 0)
        {
            if((piece & Rules.left) != 0)
                return true;
            if((piece & Rules.left) != 0)
                return true;
        }
        if((piece & Rules.down) != 0)
        {
            if((piece & Rules.left) != 0)
                return true;
            if((piece & Rules.left) != 0)
                return true;
        }
        return false;
    }
    
    public boolean isEdge(long piece)
    {
        if((piece & Rules.up) != 0)
            if((piece & Rules.left) == 0 && (piece & Rules.right) == 0)
                return true;
        if((piece & Rules.down) != 0)
            if((piece & Rules.left) == 0 && (piece & Rules.right) == 0)
                return true;
        if((piece & Rules.left) != 0)
            if((piece & Rules.up) == 0 && (piece & Rules.down) == 0)
                return true;
        if((piece & Rules.right) != 0)
            if((piece & Rules.up) == 0 && (piece & Rules.down) == 0)
                return true;
        return false;
    }
    
    /**
     * Checks how many of the enemy pieces have no way of escaping from being
     * captured.
     * Goes over every piece the enemy has on the board that has an opponent
     * piece in it's "hot spots".
     */
    public int countEnemyDeadEnds(Board b)
    {
       long cur = b.turn.equals("pw") ? b.getBlackState() : b.getWhiteState();
       long op = b.turn.equals("pb") ? b.getBlackState() : b.getWhiteState();
       int counter = 0;
        // For loop that goes over every piece that the current player has on 
        // the board.
        for(long i = 1;i <= op;i *= 2)
        {
            if((op & i) != 0 && checkHotSpots(i, cur) == true)
                counter++;
        }
        return counter;
    }
    
    /**
     * Checks how many of the current player's pieces have no way of escaping 
     * from being captured.
     * Goes over every piece the current player has on the board that has an 
     * opponent piece in it's "hot spots".
     */
    public int countMyDeadEnds(Board b)
    {
        long cur = b.turn.equals("pw") ? b.getBlackState() : b.getWhiteState();
        long op = b.turn.equals("pb") ? b.getBlackState() : b.getWhiteState();
        int counter = 0;
         // For loop that goes over every piece that the current player has on 
         // the board.
         for(long i = 1;i <= cur;i *= 2)
         {
             if((cur & i) != 0 && checkHotSpots(i, op) == true)
                 counter++;
         }
         return counter;
    }
    
    /**
     * Checks if a piece has enemy pieces in its "HotSpots"
     */
    public boolean checkHotSpots(long piece, long op)
    {
        if((piece & Rules.up) != 0)
        {
            if((piece & Rules.diagonal) == 0)
                if(((op & piece << 17) != 0) || ((op & piece << 19) != 0) || ((op & piece << 9) != 0) || (((op & piece << 8) !=0) && (op & piece << 10) != 0))
                    return true;
            else
                if((op & piece << 8) != 0 && (op & piece << 10) != 0)
                    return true;
            if((piece & Rules.left) != 0)
                if(((op & piece << 10) != 0) || (((op & piece << 11) != 0) && ((op & piece << 19) != 0)))
                    return true;
            if((piece & Rules.right) != 0)
                if(((op & piece << 8) != 0) || (((op & piece << 7) != 0) && ((op & piece << 17) != 0)))
                    return true;
        }
        if((piece & Rules.down) != 0)
        {
            if((piece & Rules.diagonal) == 0)
                if(((op & piece >> 17) != 0) || ((op & piece >> 19) != 0) || ((op & piece >> 9) != 0) || (((op & piece >> 8) !=0) && (op & piece >> 10) != 0))
                    return true;
            else
                if((op & piece >> 8) != 0 && (op & piece >> 10) != 0)
                    return true;
            if((piece & Rules.left) != 0)
                if(((op & piece >> 8) != 0) || (((op & piece >> 7) != 0) && ((op & piece >> 17) != 0)))
                    return true;
            if((piece & Rules.right) != 0)
                 if(((op & piece >> 10) != 0) || (((op & piece >> 11) != 0) && ((op & piece >> 19) != 0)))
                    return true;
        }
        if(((piece & Rules.left) != 0) && ((piece & Rules.up) == 0) && ((piece & Rules.down) == 0))
        {
            if((piece & Rules.diagonal) == 0)
                if(((op & piece << 11) != 0) || ((op & piece >> 7) != 0) || ((op & piece << 1) != 0) || (((op & piece << 10) !=0) && (op & piece >> 8) != 0))
                    return true;
            else
                if((op & piece >> 8) != 0 && (op & piece << 10) != 0)
                    return true;
        }
        if(((piece & Rules.right) != 0) && ((piece & Rules.up) == 0) && ((piece & Rules.down) == 0))
        {
            if((piece & Rules.diagonal) == 0)
                if(((op & piece >> 11) != 0) || ((op & piece << 7) != 0) || ((op & piece >> 1) != 0) || (((op & piece >> 10) !=0) && (op & piece << 8) != 0))
                    return true;
            else
                if((op & piece << 8) != 0 && (op & piece >> 10) != 0)
                    return true;
        }
        return false;                    
    }
    
    /**
     * Checks how many of the current player's pieces are at risk of being eaten 
     * in the next move.
     * Creates a long number containing all the pieces that the opponent can eat
     * and counts them.
     * Gets a board and returns the number of pieces in danger.
     */
    public int checkThreats(Board b)
    { 
        long cur = b.turn.equals("pw")?b.getWhiteState():b.getBlackState();
        long op = b.turn.equals("pw")?b.getBlackState():b.getWhiteState();
        long danger = 0;
        // For loop that goes over every piece that the current player has on 
        // the board.
        for(long i = 1;i <= cur;i *= 2)
        {
            long x = Rules.fullBoard;
            if((cur & i) != 0)
            {
                if(Rules.validMove(i,i >> 1) && b.isEmpty((i >> 1)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i >> 1,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i >> 1, op);
                }
                if(Rules.validMove(i,i << 1) && b.isEmpty((i << 1)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i << 1,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i << 1, op);
                }
                if(Rules.validMove(i,i >> 8) && b.isEmpty((i >> 8)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i >> 8,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i >> 8, op);
                }
                if(Rules.validMove(i,i << 8) && b.isEmpty((i << 8)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i << 8,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i << 8, op);
                }
                if(Rules.validMove(i,i >> 9) && b.isEmpty((i >> 9)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i >> 9,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i >> 9, op);
                }
                if(Rules.validMove(i,i << 9) && b.isEmpty((i << 9)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i << 9,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i << 9, op);
                }
                if(Rules.validMove(i,i >> 10) && b.isEmpty((i >> 10)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i >> 10,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i >> 10, op);
                }
                if(Rules.validMove(i,i << 10) && b.isEmpty((i << 10)))
                {
                    danger ^= Rules.capturingInMyDirection(i, i << 10,op);
                    danger ^= Rules.capturingInOppositeDirection(i, i << 10, op);
                }
            }  
        }
        return countBits(danger);
    }

}