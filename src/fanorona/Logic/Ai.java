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
        Board.depth = depth;
        int value;
        if(depth == 0 || !board.checkWin().equals("n"))
        {
            value = evaluate(board);//Board.endGame == false?evaluate(board)//:endGameEval(board);
            return value;
        }
        board.getOrderedMoves();
        int best = -infinity;
        Move move; 
        Board nextBoard;
        while (board.hasMoreMoves())
        {
            move = board.getNextMove();
            nextBoard = board.makeMove(move);
            value = -negaMax(nextBoard, depth-1,-beta,-alpha);
            value = Board.endGame == true?value - checkThreats(board):value;
            if(value > best)
                best = value;
            if(best > alpha)
                alpha = best;
            if(best >= beta)
                break;
        }
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
        if(!board.checkWin().equals("n"))
            return  infinity;//eval = infinity;
        else
            eval = countBits(board.getWhiteState()) - countBits(board.getBlackState());
        return board.turn == "pb"?eval:-eval;
    }
    
    /**
     * Returns an evaluation of the board for the negaMax function at the 
     * late stages of the game. 
     *
    public int endGameEval(Board board)
    {
        int eval;
        if(!board.checkWin().equals("n"))
            return  infinity;
        eval = countBits(board.getWhiteState()) - countBits(board.getBlackState());
        eval = eval - 8*checkThreats(board);
        return board.turn == "pb"?eval:-eval;
    }*/
    
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
    
    /**
     * Checks how many of the current player's pieces are at risk of being eaten 
     * in the next move.
     * Gets a board and returns the number of pieces in danger.
     */
    public int checkThreats(Board b)
    {
        long cur = b.turn.equals("pw")?b.getWhiteState():b.getBlackState();
        long op = b.turn.equals("pw")?b.getBlackState():b.getWhiteState();
        // For loop that goes over every piece that the current player has on 
        // the board.
        int count = 0;
        for(long i = 1;i <= cur;i *= 2)
        {
            long x = Rules.fullBoard;
            if((cur & i) != 0)
            {
                if(Rules.validMove(i/*selected*/,i >> 1) && b.isEmpty((i >> 1)))
                    if(Rules.capturingInMyDirection(i, i >> 1,op) != 0|| Rules.capturingInOppositeDirection(i, i >> 1,op) != 0)
                    {
                        count++;
                        continue;
                    }
                if(Rules.validMove(i,i << 1) && b.isEmpty((i << 1)))
                    if(Rules.capturingInMyDirection(i, i << 1,op) != 0|| Rules.capturingInOppositeDirection(i, i << 1,op) != 0)
                    {
                        count++;
                        continue;
                    }
                if(Rules.validMove(i,i >> 8) && b.isEmpty((i >> 8)))
                    if(Rules.capturingInMyDirection(i, i >> 8,op) != 0|| Rules.capturingInOppositeDirection(i, i >> 8,op) != 0)
                    {
                        count++;
                       continue;
                    }
                if(Rules.validMove(i,i << 8) && b.isEmpty((i << 8)))
                    if(Rules.capturingInMyDirection(i, i << 8,op) != 0|| Rules.capturingInOppositeDirection(i, i << 8,op) != 0)
                    {
                        count++;
                        continue;
                    }
                if(Rules.validMove(i,i >> 9) && b.isEmpty((i >> 9)))
                    if(Rules.capturingInMyDirection(i, i >> 9,op) != 0|| Rules.capturingInOppositeDirection(i, i >> 9,op) != 0)
                    {
                        count++;
                        continue;
                    }
                if(Rules.validMove(i,i << 9) && b.isEmpty((i << 9)))
                    if(Rules.capturingInMyDirection(i, i << 9,op) != 0|| Rules.capturingInOppositeDirection(i, i << 9,op) != 0)
                    {
                        count++;
                        continue;
                    }
                if(Rules.validMove(i,i >> 10) && b.isEmpty((i >> 10)))
                    if(Rules.capturingInMyDirection(i, i >> 10,op) != 0|| Rules.capturingInOppositeDirection(i, i >> 10,op) != 0)
                    {
                        count++;
                        continue;
                    }
                if(Rules.validMove(i,i << 10) && b.isEmpty((i << 10)))
                    if(Rules.capturingInMyDirection(i, i << 10,op) != 0|| Rules.capturingInOppositeDirection(i, i << 10,op) != 0)
                    {
                        count++;
                        continue;
                    }
            }  
        }
        return count;
    }

}