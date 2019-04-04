package fanorona.Logic;

public class Move
{
    private long from;
    private long to;
    private long capture;
    // If the move is a multi capture move, this variable has the next move 
    //private Move extraCapture;
    
    public Move(long from,long to,long capture)//,Move extraCapture)
    {
        this.from = from;
        this.to = to;
        this.capture = capture;
        //this.extraCapture = new Move(extraCapture);
    }
    
    public Move(Move m)
    {
        this.from = m.from;
        this.to = m.to;
        this.capture = m.capture;
        //this.extraCapture = new Move(m.extraCapture);
    }

    public long getFrom()
    {
        return from;
    }

    public void setFrom(long from)
    {
        this.from = from;
    }

    public long getTo()
    {
        return to;
    }

    public void setTo(long to)
    {
        this.to = to;
    }

    public long getCapture()
    {
        return capture;
    }

    public void setCapture(long capture)
    {
        this.capture = capture;
    }
    
    
}