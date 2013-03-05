public class Histogram extends Level{
	

	
    protected static final Histogram BLOCK_EMPTY_H	= new Histogram(BLOCK_EMPTY);
    protected static final Histogram BLOCK_POWERUP_H	= new Histogram(BLOCK_POWERUP);
    protected static final Histogram BLOCK_COIN_H	= new Histogram(BLOCK_COIN);
    protected static final Histogram GROUND_H		= new Histogram(GROUND);
    protected static final Histogram ROCK_H			= new Histogram(ROCK);
    protected static final Histogram COIN_H			= new Histogram(COIN);

    protected static final Histogram LEFT_GRASS_EDGE_H = new Histogram(LEFT_GRASS_EDGE);
    protected static final Histogram RIGHT_GRASS_EDGE_H = new Histogram(RIGHT_GRASS_EDGE);
    protected static final Histogram RIGHT_UP_GRASS_EDGE_H = new Histogram(RIGHT_UP_GRASS_EDGE);
    protected static final Histogram LEFT_UP_GRASS_EDGE_H = new Histogram(LEFT_UP_GRASS_EDGE);
    protected static final Histogram LEFT_POCKET_GRASS_H = new Histogram(LEFT_POCKET_GRASS);
    protected static final Histogram RIGHT_POCKET_GRASS_H = new Histogram(RIGHT_POCKET_GRASS);

    protected static final Histogram HILL_FILL_H = new Histogram(HILL_FILL);
    protected static final Histogram HILL_LEFT_H = new Histogram(HILL_LEFT);
    protected static final Histogram HILL_RIGHT_H = new Histogram(HILL_RIGHT);
    protected static final Histogram HILL_TOP_H = new Histogram(HILL_TOP);
    protected static final Histogram HILL_TOP_LEFT_H = new Histogram(HILL_TOP_LEFT);
    protected static final Histogram HILL_TOP_RIGHT_H = new Histogram(HILL_TOP_RIGHT);

    protected static final Histogram HILL_TOP_LEFT_IN_H = new Histogram(HILL_TOP_LEFT_IN);
    protected static final Histogram HILL_TOP_RIGHT_IN_H = new Histogram(HILL_TOP_RIGHT_IN);

    protected static final Histogram TUBE_TOP_LEFT_H = new Histogram(TUBE_TOP_LEFT);
    protected static final Histogram TUBE_TOP_RIGHT_H = new Histogram(TUBE_TOP_RIGHT);

    protected static final Histogram TUBE_SIDE_LEFT_H = new Histogram(TUBE_SIDE_LEFT);
    protected static final Histogram TUBE_SIDE_RIGHT_H = new Histogram(TUBE_SIDE_RIGHT);
	
	public final byte bt;
	
	public Histogram(byte by){
		bt = by;
	}
	
	public boolean equals(Object o){
		if (o == null)
			return false;
		if (o == this)
			return true;
		
		if (o instanceof Histogram){
			Histogram h = (Histogram) o;
			if (this.bt == h.bt){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		int hash = HASH_SEED;
		
		hash = hash * HASH_OFFSET * (int)bt;
		hash = hash * (HASH_OFFSET + (bt % 2 == 0 ? hash : (int) bt)); //note order of operations
		hash = (hash * HASH_OFFSET) + (bt % 2 == 0 ? (int)bt : hash);  //and turnary outcomes
		
		return hash;
	}
	
	//save space by using the same static, final bytes and histograms
	public static Histogram lookup(byte b){
		if (BLOCK_EMPTY	== b)
				return BLOCK_EMPTY_H;
		if (BLOCK_POWERUP == b)
			return BLOCK_POWERUP_H;
		if (BLOCK_COIN == b)
			return BLOCK_COIN_H;
		if (GROUND == b)
			return GROUND_H;
		if (ROCK == b)
			return ROCK_H;
		if (COIN == b)
			return COIN_H;
		if (LEFT_GRASS_EDGE == b)
			return LEFT_GRASS_EDGE_H;
		if (RIGHT_GRASS_EDGE ==b)
			return RIGHT_GRASS_EDGE_H;
		if (RIGHT_UP_GRASS_EDGE == b)
			return RIGHT_UP_GRASS_EDGE_H;
		if (LEFT_UP_GRASS_EDGE==b)
			return LEFT_UP_GRASS_EDGE_H;
		if (LEFT_POCKET_GRASS ==b)
			return LEFT_POCKET_GRASS_H;
		if (RIGHT_POCKET_GRASS==b)
			return RIGHT_POCKET_GRASS_H;
		if (HILL_FILL == b)
			return HILL_FILL_H;
		if (HILL_LEFT ==b)
			return HILL_LEFT_H;
		if (HILL_RIGHT ==b)
			return HILL_RIGHT_H;
		if (HILL_TOP ==b)
			return HILL_TOP_H;
		if (HILL_TOP_LEFT ==b)
			return HILL_TOP_LEFT_H;
		if (HILL_TOP_RIGHT ==b)
			return HILL_TOP_RIGHT_H;
		if(HILL_TOP_LEFT_IN ==b)
			return HILL_TOP_LEFT_IN_H;
		if (HILL_TOP_RIGHT_IN ==b)
			return HILL_TOP_RIGHT_IN_H;
		if (TUBE_TOP_LEFT ==b)
			return TUBE_TOP_LEFT_H;
		if (TUBE_SIDE_RIGHT ==b)
			return TUBE_SIDE_RIGHT_H;
		return new Histogram(b);
	}
}
