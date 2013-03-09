import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;

/**The Level class now functions as a container for the distribution of levels produced from the genetic algorithm.
 * These levels are kept in the population, and statistics are recorded over time. When the Level is queried for it's layout with getMap
 * the result is a member from the population that most closely matches the player-profile calculations and historical trends.
 * 
 * The entire genetic algorithm is encapsulated  within this class, hiding behind the standard Level implementation. This means that
 * instead of an large number of Level objects for each population produced by the GA, a single Level object is composed of a
 * large population of 2D byte arrays.
 * @author nix
 *
 */

public class Level implements LevelInterface
{

	protected static final int HASH_SEED = 646;
	protected static final int HASH_OFFSET = 91079;
	
	public static ArrayList<byte[][]> population = new ArrayList<byte[][]>(0); //perhaps a heap
	
	protected static ArrayList<Float> pop_history = new ArrayList<Float>();
	protected static HashMap<Integer,Float> mem_history = new HashMap<Integer,Float>();
	protected static HashMap<Integer[], Float> section_history = new HashMap<Integer[], Float>();
	
	protected static HashMap<Histogram, Integer> H_POP = new HashMap<Histogram, Integer>(24); //population histogram
	protected static HashMap<Histogram, Integer> H_MEM = new HashMap<Histogram, Integer>(24); //member histogram
	protected static HashMap<Histogram, Integer> H_SEC = new HashMap<Histogram, Integer>(24); //section histogram
	
    protected static final byte BLOCK_EMPTY	= (byte) (0 + 1 * 16);
    protected static final byte BLOCK_POWERUP	= (byte) (4 + 2 + 1 * 16);
    protected static final byte BLOCK_COIN	= (byte) (4 + 1 + 1 * 16);
    protected static final byte GROUND		= (byte) (1 + 9 * 16);
    protected static final byte ROCK			= (byte) (9 + 0 * 16);
    protected static final byte COIN			= (byte) (2 + 2 * 16);

    protected static final byte LEFT_GRASS_EDGE = (byte) (0+9*16);
    protected static final byte RIGHT_GRASS_EDGE = (byte) (2+9*16);
    protected static final byte RIGHT_UP_GRASS_EDGE = (byte) (2+8*16);
    protected static final byte LEFT_UP_GRASS_EDGE = (byte) (0+8*16);
    protected static final byte LEFT_POCKET_GRASS = (byte) (3+9*16);
    protected static final byte RIGHT_POCKET_GRASS = (byte) (3+8*16);

    protected static final byte HILL_FILL = (byte) (5 + 9 * 16);
    protected static final byte HILL_LEFT = (byte) (4 + 9 * 16);
    protected static final byte HILL_RIGHT = (byte) (6 + 9 * 16);
    protected static final byte HILL_TOP = (byte) (5 + 8 * 16);
    protected static final byte HILL_TOP_LEFT = (byte) (4 + 8 * 16);
    protected static final byte HILL_TOP_RIGHT = (byte) (6 + 8 * 16);

    protected static final byte HILL_TOP_LEFT_IN = (byte) (4 + 11 * 16);
    protected static final byte HILL_TOP_RIGHT_IN = (byte) (6 + 11 * 16);

    protected static final byte TUBE_TOP_LEFT = (byte) (10 + 0 * 16);
    protected static final byte TUBE_TOP_RIGHT = (byte) (11 + 0 * 16);

    protected static final byte TUBE_SIDE_LEFT = (byte) (10 + 1 * 16);
    protected static final byte TUBE_SIDE_RIGHT = (byte) (11 + 1 * 16);
    
    //For the Rhythm class, replaced at level creation with actual elements
    //used like a struct, to define patterns to apply to levels during crossover in addition to the two parents
    protected static final byte OUTLINE = (byte) (13 + 2 * 16);
    
    //The level's width and height
    protected int width;
    protected int height;
    
    //This map of WIDTH * HEIGHT that contains the level's design
    private byte[][] map;

    //This is a map of WIDTH * HEIGHT that contains the placement and type enemies
    private SpriteTemplate[][] spriteTemplates;

    //These are the place of the end of the level
    protected int xExit;
    protected int yExit;
    
    public Level(){
    	
    }
    
    public Level(byte[][] new_map){
    	map = new_map;
    }

    public Level(int width, int height)
    {
        this.width = width;
        this.height = height;

        xExit = 10;
        yExit = 10;
        map = new byte[width][height];
        spriteTemplates = new SpriteTemplate[width][height];
    }

    public static void loadBehaviors(DataInputStream dis) throws IOException
    {
        dis.readFully(Level.TILE_BEHAVIORS);
    }

    public static void saveBehaviors(DataOutputStream dos) throws IOException
    {
        dos.write(Level.TILE_BEHAVIORS);
    }

    /**
     *Clone the level data so that we can load it when Mario die
     */
    public Level clone() throws CloneNotSupportedException {

        Level clone=new Level(width, height);

        clone.map = new byte[width][height];
    	clone.spriteTemplates = new SpriteTemplate[width][height];
    	clone.xExit = xExit;
    	clone.yExit = yExit;

    	for (int i = 0; i < map.length; i++)
    		for (int j = 0; j < map[i].length; j++) {
    			clone.map[i][j]= map[i][j];
    			clone.spriteTemplates[i][j] = spriteTemplates[i][j];
    	}

        return clone;

      }

    public void tick(){    }

    public byte getBlockCapped(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    public byte getBlock(int x, int y)
    {
        if (x < 0) x = 0;
        if (y < 0) return 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        return map[x][y];
    }

    public void setBlock(int x, int y, byte b)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        map[x][y] = b;
    }

    public boolean isBlocking(int x, int y, float xa, float ya)
    {
        byte block = getBlock(x, y);

        boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
        blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
        blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

        return blocking;
    }

    public SpriteTemplate getSpriteTemplate(int x, int y)
    {
        if (x < 0) return null;
        if (y < 0) return null;
        if (x >= width) return null;
        if (y >= height) return null;
        return spriteTemplates[x][y];
    }

    public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate)
    {
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        spriteTemplates[x][y] = spriteTemplate;
    }

    public SpriteTemplate[][] getSpriteTemplate(){
    	return this.spriteTemplates;
    }

    public void resetSpriteTemplate(){
    	for (int i = 0; i < spriteTemplates.length; i++) {
			for (int j = 0; j < spriteTemplates[i].length; j++) {

				SpriteTemplate st = spriteTemplates[i][j];
				if(st != null)
					st.isDead = false;
			}
		}
    }


    public void print(byte[][] array){
    	for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.print(array[i][j]);
			}
			System.out.println();
		}
    }
    /**
     * 
     */
    
	public byte[][] getMap() {
		int close =0;
		//get player's desired entropy and find the 'closest' to that
		//over or under is determined by the history of population entropies
		return population.get(close);
	}
	public void setMap(byte[][] new_map){
		map = new_map;
	}
	public SpriteTemplate[][] getSpriteTemplates() {
		return spriteTemplates;
	}
	public int getxExit() {
		// TODO Auto-generated method stub
		return xExit;
	}
	public int getyExit() {
		// TODO Auto-generated method stub
		return yExit;
	}
	public int getWidth() {
		// TODO Auto-generated method stub
		return width;
	}
	public int getHeight() {
		// TODO Auto-generated method stub
		return height;
	}

	public String getName() {
		return "";
	}

	
	public static byte[][] getMember(int mem) {
		return population.get(mem);
	}
	
	/**Count's total occurrences  of a variable in a pop and calculates a standard shannon entropy
	 * IS NOT necessarily equivalent to successive applications of shannonMember due to precise entropy formula
	 */
	public static float shannonPop(ArrayList<byte[][]> pop){
		float entropy = 0;
		
		if (!H_POP.isEmpty()){
			H_POP.clear();
		}
		
		for (byte[][] member : pop){
			for (byte[] row : member){
				for (byte column : row){
					pop_tally(column);
				}
			}
		}
		for (Entry<Histogram, Integer> e : H_POP.entrySet()){
			//Entropy = sum over attributes, prob of attrA * log prob attrA
			
			entropy = (float) (entropy + e.getValue()/H_POP.size() * Math.log( e.getValue()/H_POP.size()));
		}
		pop_history.add(entropy);
		return entropy;
		
	}
	/**differs slightly from standard shannon entropy by way of only calculating a minimum of feature probability 
	 * if the member is lacking features (specific elements), then probability of missing features is distributed across
	 * the remaining elements. This will cause levels with a minimalist feature set (eliminating undesired features),
	 * while also meeting a players desired difficulty. This is a competitive system, designed to be chaotic in local space,
	 * but probabilistic in definition.
	 */
	public float shannonMember(byte[][] member){
		float entropy = 0;
		
		if (!H_MEM.isEmpty()){
			H_MEM.clear();
		}
		
		for (byte[] row : member){
			for (byte column : row){
				mem_tally(column);
			}
		}
		
		for (Entry<Histogram, Integer> e : H_MEM.entrySet()){
			//Entropy = sum over attributes, prob of attrA * log prob attrA
			//including the math.max because H_POP.size could be 0
			entropy = (float) (entropy + e.getValue()/Math.min(H_MEM.size(), Math.max(H_POP.size(), 24)) * Math.log( e.getValue()/Math.min(H_MEM.size(), Math.max(H_POP.size(),24))));
		}
		
		return entropy;
	}
	
	
	public float shannonSection( int beginRow, int endRow, int beginCol, int endCol){
		return shannonPop(section(beginCol,endCol, beginRow, endRow));	
	}
	
	/** _tally increases the count of the parameter byte in the desired histogram
	 *  must MANUALLY reset histograms,if needed
	 */
	public static void pop_tally(byte b) {
		if(H_POP.containsKey(Histogram.lookup(b))){
			H_POP.put(Histogram.lookup(b), (Integer)H_POP.get(Histogram.lookup(b))+1);
		}
		else{
			H_POP.put(Histogram.lookup(b), new Integer(1));
		}
	}
	public static void mem_tally(byte b) {
		if(H_MEM.containsKey(Histogram.lookup(b))){
			H_MEM.put(Histogram.lookup(b),(Integer)H_MEM.get(Histogram.lookup(b))+1);
		}
		else{
			H_MEM.put(Histogram.lookup(b), new Integer(1));
		}
	}
	public static void sec_tally(byte b) {
		if(H_SEC.containsKey(Histogram.lookup(b))){
			H_SEC.put(Histogram.lookup(b),(Integer)H_SEC.get(Histogram.lookup(b))+1);
		}
		else{
			H_SEC.put(Histogram.lookup(b), new Integer(1));
		}
		
	}

	/**grab a cross section of of members of a population. Starts with a zero index.  
	 * DOES NOT copy the end element. 
	 */
	public static ArrayList<byte[][]> section(int beginRow, int endRow, int beginCol, int endCol) { //
		ArrayList<byte[][]> cross_sections = new ArrayList<byte[][]>();
		int rowI, colI;
		
		for (byte[][] member : population){//for each member of the population
			byte[][] member_copy = new byte[member.length][endCol-beginCol];//create a copy holder
			rowI = 0;
			for (byte[] row : member){
				colI = 0;
				if(rowI >= beginRow && rowI < endRow){//if a row is valid
					for(byte col : row){
						if(colI >= beginCol && colI < endCol){//if a col is valid copy it over
							member_copy[rowI][colI-beginCol] = col;
						}
						colI++;
					}
				}
				rowI++;
			}
			cross_sections.add(member_copy);
		}
		return cross_sections;
	}
	
	/** The intuition between the 
	 *  
	 * @param scheme - distiguishes between various selection schemes. 
	 * 	scheme >= 1 : step (stripe) applied equal to the scheme number 
	 *  scheme == -1: random
	 */
	public static void selection(int scheme){
		byte[][] child;
		
		if (scheme == -1){									
			Random randA = new Random((long)(HASH_OFFSET * pop_history.get(pop_history.size())));
			Random randB = new Random((long)(HASH_OFFSET * randA.nextLong()));
			Random randR = new Random((long)(HASH_OFFSET * randB.nextLong()));
			
			child = crossover(population.get(randA.nextInt(population.size())),population.get(randB.nextInt(population.size())),Rhythm.rhythm(randR.nextInt(Rhythm.num_rhythm)));
			
			population.add(child);
			
			}
		else{
			
		}
	}
	
	/**
	 * 
	 * @param A - parent A
	 * @param B - parent B
	 * @param R - rhythm byte pattern
	 * @return byte[][] child OR null on error
	 */
	public static byte[][] crossover(byte[][] A, byte[][] B, byte[][] R){
			
		//if the dimensions of A,B, and R are not equal
		if(A.length != B.length || B.length != R.length || A[0].length != B[0].length || B[0].length != R[0].length){
			return null;
		}
		
		byte[][] child = new byte[A.length][A[0].length];
		
		//column and row counters
		int rowA = 0, colA = 0, rowB = 0, colB = 0, rowR = 0, colR = 0, rowC = 0, colC = 0;
		
		
		for(byte[] row : R){
			for (byte col : row){
				if (col == OUTLINE){
					
				}//col == outline
				colR++;
			}//col
		}//row
		
		
		return child;
	}
	
	public static Integer hashMem(byte[][] member){
		int hash = HASH_SEED;
		for (byte[] row : member){
			for (byte col : row){
				hash = hash * HASH_OFFSET * (int)col;
				hash = hash * (HASH_OFFSET + (col % 2 == 0 ? hash : (int) col)); //note order of operations
				hash = (hash * HASH_OFFSET) + (col % 2 == 0 ? (int)col : hash);  //and turnary outcomes
			}
			hash = hash/row.length;
		}
		return (Integer)hash;
	}
}
