package fanorona.Logic;

public class Move
{
    private long from;
    private long to;
    private long capture;
    
    public Move(long from,long to,long capture)
    {
        this.from = from;
        this.to = to;
        this.capture = capture;
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