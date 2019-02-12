
package fanorona.Logic;

public class Rules
{
    
    /*
    Checks if the piece can move to the location the user wanted.
    Creates a mask of al the possible diractions the piece can move in.
    Returns true if the requested location is a possible location for the piece. 
    */
    public static boolean validMove(long from,long to)
    {
        // A mask of every bit on the board that can move in a diagonal.
        long diagonal = 0x0000055555555555L;
        long possible = 0;
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
            
}