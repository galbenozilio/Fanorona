
package fanorona.Logic;
import java.util.HashMap;
import java.lang.Math;

public class Rules
{

    // A mask of every piece on the boarders of the board, when eating a ???
    // few pieces in a row, eating must stop at the end of the line.???????
    public static long wall = 0x000000180C060300L;
   
    /**
     * Checks if the piece can move to the location the user wanted.
     * Creates a mask of all the possible direction the piece can move in.
     * Returns true if the requested location is a possible location for the piece. 
     */
    public static boolean validMove(long from,long to)
    {
        // A mask of every bit on the board that can move in a diagonal.
        long diagonal = 0x0000055555555555L;
        long possible = 0x000000180c060300L;
        long mask = from;
        mask = mask & diagonal;
        possible |= from>>1;
        possible |= from<<1;
        possible |= from<<9;
        possible |= from>>9;
        if(mask != 0)
        {
            possible |= from<<10;
            possible |= from<<8;
            possible |= from>>10;
            possible |= from>>8;
        }
        return (to & possible) != 0;
    }
    
    /**
     * Checks if a move causes eating in the progress direction.
     * Gets the initial location,the location after the move and the state of 
     * the opponent's pieces.
     * Returns a long number containing the locations of the eaten pieces.
     */
    public static long eatingInMyDirection (long from,long to,long op)
    {
        int dir = (int) (from>to? from/to:-to/from);
        return eating(from,dir,op);
    }
    
    public static long eatingInOppositeDirection(long from,long to,long op)
    {
        int dir = (int) (to>from? to/from:-from/to);
        return eating(to,dir,op);
    }
    
    /**
     * Checks how many enemy pieces are eaten in a move.
     * Gets a direction and the opponents status and returns a mask of the 
     * locations of the eaten pieces.
     * Note : log[2](x) = log[10]x/log[10]2
     */
    public static long eating(long from,int dir,long op)
    {
        long mask = 0, temp;
        if(dir > 0)
        {// Shift right
            dir = (int)(java.lang.Math.log10(dir)/java.lang.Math.log10(2));
            temp = from >> dir;
            if((temp & wall) == 0 || dir != 1)
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
            if((temp & wall) == 0 || dir != 1)
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