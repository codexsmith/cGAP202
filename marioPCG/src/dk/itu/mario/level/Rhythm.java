import java.util.ArrayList;
import java.util.Random;

public class Rhythm extends Level{
	
	public static final int num_rhythm = 11;
	protected static ArrayList<byte[][]> rhythm_store = new ArrayList<byte[][]>(num_rhythm); 
	
	private Rhythm(){
		
	}
	
	public int rhythmCount(){
		return rhythm_store.size();
	}
	
	private static void create(int rhythmCount){
		byte[][] rhyme = new byte[population.get(0).length][population.get(0)[0].length];
		int rowC = 0;
		//SINGLE ________ normal floor
		for(byte[] row : rhyme){
			if (rowC < 3){
				for(byte col : row){
					col = OUTLINE;
				}//col
			}
			rowC++;
		}//row
		rhythm_store.add(rhyme);
		rhyme = new byte[population.get(0).length][population.get(0)[0].length];
		
		//DOUBLE ======= two floors
		for(byte[] row : rhyme){
			if (rowC < 5 || rowC > 50 && rowC < 55 ){
				for(byte col : row){
					col = OUTLINE;
				}//col
			}
			rowC++;
		}//row
		rhythm_store.add(rhyme);
		rhyme = new byte[population.get(0).length][population.get(0)[0].length];
		
		//WIDE RANGE _______ single floor
			for(byte[] row : rhyme){
				if (rowC < 15){
					for(byte col : row){
						col = OUTLINE;
					}//col
				}
				rowC++;
			}//row
		rhythm_store.add(rhyme);
		rhyme = new byte[population.get(0).length][population.get(0)[0].length];
		
		for(int type = 3; type < rhythm_store.size(); type++){
			//RANDOM creates a random layout of placeholders	
			//the larger the type number, the more sparse the random level will be
			Random randA = new Random();
			Random randB = new Random();
			for(byte[] row : rhyme){
				if (randA.nextInt(population.get(0).length*type-1) <= population.get(0).length){
					for(byte col : row){
						if(randB.nextInt(population.get(0)[0].length*type-2) <= population.get(0)[0].length){
							col = OUTLINE;
						}
					}//col
				}
			}//row
			rhythm_store.add(rhyme);
			rhyme = new byte[population.get(0).length][population.get(0)[0].length];
		}
	}
	
	/**Standard interaction with the Rhythm class occurs through this method. it will return the rhythm byte map equivalent with
	 * the type desired. 
	 * 
	 * @param type - DEFAULT values. will expand if needed see 3+
	 * 	0 - normal mario level, with a single floor
	 *  1 - double mario level, 2 'floors' one low and one high
	 *  2 - wide range floor, triple the depth of a normal level to allow more variability
	 *  3 - 10 : random - the higher the number, the more sparse the level, will expand to create as sparse a level 
	 */
	public static byte[][] rhythm(int type){
		if (type -1 >rhythm_store.size() || rhythm_store.isEmpty()){
			create(Math.max(type, num_rhythm));
			return rhythm_store.get(type);
		}
		else{
			return rhythm_store.get(type);
		}
	}
}
