package fanorona.Logic;

public class Move
{
    private long from;
    private long to;
    private long eat;
    
    public Move(long from,long to,long eat)
    {
        this.from = from;
        this.to = to;
        this.eat = eat;
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

    public long getEat()
    {
        return eat;
    }

    public void setEat(long eat)
    {
        this.eat = eat;
    }
    
    
}