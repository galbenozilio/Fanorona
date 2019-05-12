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
                    eval = infinity/10;
                else
                    eval = -infinity/10;
            else
                return -infinity;
        else
        {
            eval = countBits(board.getWhiteState()) - countBits(board.getBlackState());
            if(Board.endGame == true)
            {
                //board.turn = board.turn.equals("pw") ? "pb" : "pw";
                eval = Board.endGame == true?eval - checkThreats(board):eval;
                if(board.turn == "pw")
                    eval = eval - 10*countCorners(board.getBlackState());//(int)(2*eval)
                else
                    eval = eval - 10*countCorners(board.getWhiteState());
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
        return board.turn.equals("pb")?-eval:eval;
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
    
    public int countCorners(long state)
    {
        int counter = 0;
        // For loop that goes over every piece that the player has on 
        // the board.
        for (long i = 1; i < state; i *= 2)
        {
            if ((state & i) != 0)
                counter += isCorner(i);
        }
        return counter;
    }
    
    public int isCorner(long piece)
    {
        if((piece & Rules.up) != 0)
            if((piece & Rules.left) != 0)
                return 1;
            if((piece & Rules.left) != 0)
                return 1;
        if((piece & Rules.down) != 0)
            if((piece & Rules.left) != 0)
                return 1;
            if((piece & Rules.left) != 0)
                return 1;
        return 0;
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
        // For loop that goes over every piece that the current player has on 
        // the board.
        long danger = 0;
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