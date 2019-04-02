package fanorona.Logic;
import java.lang.Long;

public class Ai
{
    private int infinity = Integer.MAX_VALUE;
    private int bla = Integer.MIN_VALUE;
    
    public int alphaBeta(Board board,int depth,int alpha,int beta)
    {
        Board.depth = depth;
        int value;
        if(depth == 0 || !board.checkWin().equals("n"))
        {
            value = evaluate(board);
            return value;
        }
        board.getOrderedMoves();
        int best = -infinity;//-MATE-1;
        Move move; 
        Board nextBoard;
        while (board.hasMoreMoves())
        {
            move = board.getNextMove();
            nextBoard = board.makeMove(move);
            value = -alphaBeta(nextBoard, depth-1,-beta,-alpha);
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
     * Returns an evaluation of the board for the alphaBeta function. 
     * the evaluation checks which player has more pieces on the board and 
     * returns the difference.  
     */
    public int evaluate(Board board)
    {
        int eval;
        if(!board.checkWin().equals("n"))
            eval = infinity;
        else
            eval = countBits(board.getWhiteState()) - countBits(board.getBlackState());
        return board.turn == "pw"?eval:-eval;
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

}