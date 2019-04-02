package fanorona.Logic;



public class Player 
{
    // white or black
    public long state; 
    public Player(long state)
    {
        this.state = state;
    }
    
    /**
     * Copy constructor.
     */
    public Player(Player p)
    {
        this.state = p.state;
    }
    
}