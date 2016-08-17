/**
 * 
 */
package cox5529.generator.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Brandon Cox
 * 		
 */
public class Note {
	
	private HashMap<Byte, Integer> follow; // pitch that follows this note, frequency
	private byte[] precede;
	
	/**
	 * Constructs a note object
	 * 
	 * @param precede the notes that precede the note that will be generated
	 * @param next the note that follows this set
	 */
	public Note(byte[] precede, byte next) {
		this.follow = new HashMap<Byte, Integer>();
		follow.put(next, 1);
		this.precede = precede;
	}
	
	/**
	 * Adds a pitch to this Note's data
	 * 
	 * @param p the pitch to add
	 */
	public void addPitch(Byte p) {
		boolean cont = false;
		Iterator<Byte> it = follow.keySet().iterator();
		while(it.hasNext()) {
			Byte key = it.next();
			if(key == p) {
				cont = true;
				break;
			}
		}
		if(cont) {
			follow.replace(p, follow.get(p) + 1);
		} else {
			follow.put(p, 1);
		}
	}
	
	/**
	 * Returns the precede array for this object
	 * 
	 * @return the precede array for this object
	 */
	public byte[] getPrecede() {
		return precede;
	}
	
	/**
	 * Gets the pitch that will follow this note.
	 * 
	 * @return the pitch that will follow this note.
	 */
	public byte getFollowPitch() {
		Iterator<Entry<Byte, Integer>> it = follow.entrySet().iterator();
		int total = 0;
		while(it.hasNext()) {
			Map.Entry<Byte, Integer> pair = (Map.Entry<Byte, Integer>) it.next();
			total += pair.getValue();
		}
		it = follow.entrySet().iterator();
		double rand = Math.random();
		int counted = 0;
		while(it.hasNext()) {
			Map.Entry<Byte, Integer> pair = (Map.Entry<Byte, Integer>) it.next();
			counted += pair.getValue();
			if(rand < (counted + 0.0) / total) {
				return pair.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * Determines if this Note object is equal to another.
	 * 
	 * @param n the Note object to compare this object to
	 * @return true if they are the same
	 */
	public boolean equals(Note n) {
		byte[] nPrecede = n.getPrecede();
		for(int i = 0; i < nPrecede.length; i++) {
			if(nPrecede[i] != precede[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the String representation of this Note object.
	 * 
	 * @return the String representation of this Note object.
	 */
	public String toString() {
		String re = "Precede: ";
		for(int i = 0; i < precede.length; i++) {
			re += precede[i] + " ";
		}
		re += "\n";
		re += "Size: " + follow.size() + "\n";
		re += "Contents: ";
		Iterator<Entry<Byte, Integer>> it = follow.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Byte, Integer> pair = (Entry<Byte, Integer>) it.next();
			re += pair.getKey() + "x" + pair.getValue() + " ";
		}
		return re;
	}
}
