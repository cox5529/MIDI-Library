/**
 * 
 */
package cox5529.generator.storage;

import java.util.ArrayList;
import java.util.Arrays;

import cox5529.midi.Helper;

/**
 * Storage class for musical phrases.
 * 
 * @author Brandon Cox
 * 		
 */
public class Phrase {
	
	private ArrayList<Measure> measures;
	private ArrayList<byte[]> chords;
	
	/**
	 * Half cadence
	 */
	public static final int HALF_CADENCE = 0;
	
	/**
	 * Authentic cadence
	 */
	public static final int AUTHENTIC_CADENCE = 2;
	/**
	 * Plagal cadence
	 */
	public static final int PLAGAL_CADENCE = 3;
	
	/**
	 * Deceptive cadence
	 */
	public static final int DECEPTIVE_CADENCE = 4;
	
	/**
	 * Constructs a new Phrase object
	 * 
	 * @param measures the measures that make up this phrase
	 * @param chords the chords contained within this phrase in integer notation
	 */
	public Phrase(ArrayList<Measure> measures, ArrayList<byte[]> chords) {
		this.measures = measures;
		this.chords = chords;
	}
	
	/**
	 * Gets the cadence value of the given numeral chords
	 * 
	 * @param end the numeral representing the 2nd to last chord in a proposed phrase
	 * @param to the numeral representing the last chord in a proposed phrase
	 * @return the number representing the cadence value of the chords, or -1 if there is no cadence
	 */
	public static int getCadence(String end, String to) {
		if(to.toLowerCase().equals("V") || to.toLowerCase().equals("V7"))
			return HALF_CADENCE;
		else if((end.toLowerCase().equals("v") || end.toLowerCase().equals("v7") || end.toLowerCase().equals("viio") || end.toLowerCase().equals("viio7")) && (to.toLowerCase().equals("i") || to.toLowerCase().endsWith("i7")))
			return AUTHENTIC_CADENCE;
		else if((end.toLowerCase().equals("iv") || end.toLowerCase().equals("iv7")) && (to.toLowerCase().equals("i") || to.toLowerCase().endsWith("i7")))
			return PLAGAL_CADENCE;
		else if((end.toLowerCase().equals("v") || end.toLowerCase().equals("v7")) && !(to.toLowerCase().equals("i") || to.toLowerCase().endsWith("i7")))
			return DECEPTIVE_CADENCE;
		else
			return -1;
	}
	
	/**
	 * Gets the numeral representation of a chord
	 * 
	 * @param chord the chord to convert to numerals
	 * @param sharps the number of sharps in the key signature, negative if flats
	 * @param isMajor true if the key is major
	 * @return the numeral representation of the given chord
	 */
	public static String getNumeral(byte[] chord, int sharps, boolean isMajor) {
		byte base = Helper.getBase(sharps, isMajor);
		byte[] root = new byte[chord.length];
		for(int i = 0; i < root.length; i++) {
			root[i] = (byte) (chord[i] % 12);
		}
		Arrays.sort(root);
		byte min = root[0];
		for(int i = 0; i < root.length; i++) {
			root[i] = (byte) (root[i] - min);
		}
		String num = "";
		int b = chord[0] - base;
		if(isMajor) {
			if(b == 0)
				num = "i";
			else if(b == 2)
				num = "ii";
			else if(b == 4)
				num = "iii";
			else if(b == 5)
				num = "iv";
			else if(b == 7)
				num = "v";
			else if(b == 9)
				num = "vi";
			else if(b == 10)
				num = "vii";
			else
				num = "i";
		} else {
			if(b == 0)
				num = "i";
			else if(b == 2)
				num = "ii";
			else if(b == 3)
				num = "iii";
			else if(b == 5)
				num = "iv";
			else if(b == 7)
				num = "v";
			else if(b == 8)
				num = "vi";
			else if(b == 10)
				num = "vii";
			else
				num = "i";
		}
		if(chord.length == 2) {
			return "D";
		} else if(chord.length == 3) {
			if(root[1] == 4) {
				if(root[2] == 7)
					return num.toUpperCase(); // major 0-4-7
				else
					return num + "+"; // augmented 0-4-8
			} else {
				if(root[2] == 7)
					return num; // minor 0-3-7
				else
					return num + "o"; // diminished 0-3-6
			}
		} else {
			
			if(root[1] == 3) {
				if(root[2] == 7) {
					if(root[3] == 10)
						return num + "7"; // minor seventh 0-3-7-10
					else if(root[3] == 11)
						return num + "mM7"; // minor-major seventh 0-3-7-11
				} else if(root[2] == 6) {
					if(root[3] == 9)
						return num + "d7"; // diminished seventh 0-3-6-9
					else if(root[3] == 10)
						return num + "o7"; // half-diminished seventh 0-3-6-10
				}
			} else if(root[1] == 4) {
				if(root[2] == 7) {
					if(root[3] == 10 && num.equals("v"))
						return "V7D"; // dominant seventh 0-4-7-10
					else if(root[3] == 11)
						return num.toUpperCase() + "7"; // major seventh 0-4-7-11
				} else if(root[2] == 8) {
					if(root[3] == 11)
						return num.toUpperCase() + "+7"; // augmented major seventh 0-4-8-11
				}
			}
			return num;
		}
	}
}
