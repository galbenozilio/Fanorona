
package fanorona.Logic;
import java.lang.Math;

public class Rules
{

    // A mask of every piece on the boarders of the board, when capturing a 
    // few pieces in a row, capturing must stop at the end of the line.
    public static final long wall = 0x000000180C060300L;
    // A mask of a full board.
    public static final long fullBoard = 0x00001FFFFFFFFFFFL;
    // The previous direction the player moved to in his multiple capturing move.
    //private int prevDirection = 0;
    // A mask of every piece on the board that can't move left(left edge)
    public static final long left = 0x1008040201L;
    // A mask of every piece on the board that can't move right(right edge).
    public static final long right = 0x8100804020100L;
    // A mask of every piece on the board that can't move up(upeer edge).
    public static final long up = 0x1FFL;
    // A mask of every piece on the board that can't move down(lower edge).
    public static final long down = 0x1FF000000000L;
    
    /**
     * Checks if the piece can move to the location the user wanted.
     * Creates a mask of all the possible directions the piece can move in.
     * Gets a location of the piece the player wishes to move and the location 
     * to which he wishes to move the piece.
     * Returns true if the requested location is a possible location for the
     * piece. 
     */
        public static boolean validMove(long from,long to)
    {
        // A mask of every bit on the board that can move in a diagonal.
        long diagonal = 0x155555555555L;
        long possible = 0;//0x000000180c060300L;
        // A mask of every piece on the board that can't move left(edges)
        // and can't move to the right in diagonal.
        long left = 0x1008040201L;
        // A mask of every piece on the board that can't move right(edges).
        long right = 0x8100804020100L;
        // A mask of every piece on the board that can't move up(edges).
        long up = 0x1FFL;
        // A mask of every piece on the board that can't move down(edges).
        long down = 0x1FF000000000L;
        long mask = from;
        if((from & left) == 0)
            possible |= from>>1;
        if((from & right) == 0)
            possible |= from<<1;
        if((from & down) == 0)
            possible |= from<<9;
        if((from & up) == 0)
            possible |= from>>9;
        mask = mask & diagonal;
        if(mask != 0)
        {
            if((from & down) == 0)
            {
                if((from & right) == 0)
                    possible |= from<<10;
                if((from & left) == 0)
                    possible |= from<<8;
            }
            if((from & up) == 0)
            {
                if((from & left) == 0)
                    possible |= from>>10;
                if((from & right) == 0)
                    possible |= from>>8;
            }
        }
        return (to & possible) != 0;
    }
    
    /**
     * Checks if a move causes capturing in the progress direction.
     * Gets the initial location,the location after the move and the state of 
     * the opponent's pieces.
     * Returns a long number containing the locations of the captured pieces.
     */
    public static long capturingInMyDirection (long from,long to,long op)
    {
        int dir = (int) (from>to? from/to:-to/from);
        return capturing(from,dir,op);
    }
    
    /**
     * Checks if a move causes capturing in the opposite direction.
     * Gets the initial location,the location after the move and the state of 
     * the opponent's pieces.
     * Returns a long number containing the locations of the captured pieces.
     */
    public static long capturingInOppositeDirection(long from,long to,long op)
    {
        int dir = (int) (to>from? to/from:-from/to);
        return capturing(to,dir,op);
    }
    
    /**
     * Checks how many enemy pieces are captured in a move.
     * Gets a direction and the opponents status and returns a mask of the 
     * locations of the captured pieces.
     * Note : log[2](x) = log[10]x/log[10]2
     */
    public static long capturing(long from,int dir,long op)
    {
        long mask = 0, temp;
        if(dir > 0)
        {// Shift right
            dir = (int)(java.lang.Math.log10(dir)/java.lang.Math.log10(2));
            temp = from >> dir;
            if((temp & wall) == 0 || dir == 9)
            {
                temp >>= dir;
                while((temp & op) != 0)
                {
                    mask |= temp;
                    if((temp & wall) != 0 && dir != 9) break;
                    temp >>= dir;
                }
            }
        }
        else
        {// Shift left
            dir = (int)(java.lang.Math.log10(-dir)/java.lang.Math.log10(2));
            temp = from << dir;
            if((temp & wall) == 0 || dir == 9)
            {
                temp <<= dir;
                while((temp & op) != 0)
                {
                    mask |= temp;
                    if((temp & wall) != 0 && dir != 9) break;
                    temp <<= dir;
                }
            }
        }
        return mask;
    }
    
}