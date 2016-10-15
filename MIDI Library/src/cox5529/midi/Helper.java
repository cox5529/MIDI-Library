package cox5529.midi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Various helper methods.
 * 
 * @author Brandon Cox
 * 		
 */
public class Helper {
	
	/**
	 * Determines if a note is in a specified key.
	 * 
	 * @param note the note to test
	 * @param sharps the number of sharps in a key. If there are flats, number of flats * -1
	 * @param isMajor true if the key is major
	 * @return true if the note is in key
	 */
	public static boolean isInKey(byte note, int sharps, boolean isMajor) {
		while(note > 0x11)
			note -= 12;
		if(note < 6) // 6 = c1
			note += 12;
		byte base = 6;
		if(sharps == 0)
			if(isMajor)
				base = 6;
			else
				base = 15;
		else if(sharps == 1)
			if(isMajor)
				base = 11;
			else
				base = 10;
		else if(sharps == 2)
			if(isMajor)
				base = 8;
			else
				base = 17;
		else if(sharps == 3)
			if(isMajor)
				base = 15;
			else
				base = 12;
		else if(sharps == 4)
			if(isMajor)
				base = 10;
			else
				base = 7;
		else if(sharps == 5)
			if(isMajor)
				base = 17;
			else
				base = 14;
		else if(sharps == 6)
			if(isMajor)
				base = 12;
			else
				base = 9;
		else if(sharps == 7)
			if(isMajor)
				base = 7;
			else
				base = 16;
		else if(sharps == -1)
			if(isMajor)
				base = 11;
			else
				base = 8;
		else if(sharps == -2)
			if(isMajor)
				base = 16;
			else
				base = 13;
		else if(sharps == -3)
			if(isMajor)
				base = 9;
			else
				base = 6;
		else if(sharps == -4)
			if(isMajor)
				base = 14;
			else
				base = 11;
		else if(sharps == -5)
			if(isMajor)
				base = 7;
			else
				base = 16;
		else if(sharps == -6)
			if(isMajor)
				base = 12;
			else
				base = 9;
		else if(sharps == -7)
			if(isMajor)
				base = 17;
			else
				base = 12;
		byte[] key = new byte[12];
		if(isMajor) {
			key[0] = base;
			key[1] = (byte) (key[0] + 2);
			key[2] = (byte) (key[1] + 2);
			key[3] = (byte) (key[2] + 1);
			key[4] = (byte) (key[3] + 2);
			key[5] = (byte) (key[4] + 2);
			key[6] = (byte) (key[5] + 2);
			key[7] = (byte) (key[6] + 1);
		} else {
			key[0] = base;
			key[1] = (byte) (key[0] + 2);
			key[2] = (byte) (key[1] + 1);
			key[3] = (byte) (key[2] + 2);
			key[4] = (byte) (key[3] + 2);
			key[5] = (byte) (key[4] + 1);
			key[6] = (byte) (key[5] + 2);
			key[7] = (byte) (key[6] + 2);
		}
		for(int i = 0; i < key.length; i++) {
			if(key[i] == note)
				return true;
		}
		return false;
	}
	
	/**
	 * Determines the chord within the array of pitches given.
	 * 
	 * @param sharps the number of sharps in the key signature
	 * @param isMajor true if the key is major
	 * @param notes array of pitches
	 * @return ordered array of pitches that represents a chord
	 */
	public static byte[] getChord(int sharps, boolean isMajor, byte[] notes) {
		int b = getBase(sharps, isMajor);
		int low = 100;
		for(int i = 0; i < notes.length; i++) {
			while(notes[i] >= 12 + b) {
				notes[i] -= 12;
			}
			if(notes[i] < low)
				low = notes[i];
		}
		for(int i = 0; i < notes.length; i++) {
			for(int j = 0; j < notes.length; j++) {
				if(i != j && notes[i] == notes[j]) {
					Set<Byte> n = new HashSet<Byte>();
					for(int k = 0; k < notes.length; k++)
						n.add(notes[k]);
					Iterator<Byte> it = n.iterator();
					int index = 0;
					byte[] no = new byte[n.size()];
					while(it.hasNext()) {
						no[index] = it.next();
						index++;
					}
					notes = no;
				}
			}
		}
		Arrays.sort(notes);
		return notes;
	}
	
	private static byte getBase(int sharps, boolean isMajor) {
		byte base = 6;
		if(sharps == 0)
			if(isMajor)
				base = 6;
			else
				base = 15;
		else if(sharps == 1)
			if(isMajor)
				base = 11;
			else
				base = 10;
		else if(sharps == 2)
			if(isMajor)
				base = 8;
			else
				base = 17;
		else if(sharps == 3)
			if(isMajor)
				base = 15;
			else
				base = 12;
		else if(sharps == 4)
			if(isMajor)
				base = 10;
			else
				base = 7;
		else if(sharps == 5)
			if(isMajor)
				base = 17;
			else
				base = 14;
		else if(sharps == 6)
			if(isMajor)
				base = 12;
			else
				base = 9;
		else if(sharps == 7)
			if(isMajor)
				base = 7;
			else
				base = 16;
		else if(sharps == -1)
			if(isMajor)
				base = 11;
			else
				base = 8;
		else if(sharps == -2)
			if(isMajor)
				base = 16;
			else
				base = 13;
		else if(sharps == -3)
			if(isMajor)
				base = 9;
			else
				base = 6;
		else if(sharps == -4)
			if(isMajor)
				base = 14;
			else
				base = 11;
		else if(sharps == -5)
			if(isMajor)
				base = 7;
			else
				base = 16;
		else if(sharps == -6)
			if(isMajor)
				base = 12;
			else
				base = 9;
		else if(sharps == -7)
			if(isMajor)
				base = 17;
			else
				base = 12;
		base -= 6;
		return base;
	}
	
	/**
	 * Converts a time in ticks to its MIDI representation.
	 * 
	 * @param n the time in ticks to convert
	 * @return the time in its MIDI representation
	 */
	public static byte[] decimalToMIDITime(long n) {
		if(n == 0)
			return new byte[] { 0x00 };
		byte[] re = new byte[4];
		boolean val = false;
		if(n > 0x1FFFFF) {
			val = true;
			re[0] = (byte) (n / 0x200000 + 0x80);
			n %= 0x200000;
		}
		if(n > 0x3FFF || val) {
			val = true;
			re[1] = (byte) (n / 0x4000 + 0x80);
			n %= 0x4000;
		}
		if(n > 0x7F || val) {
			val = true;
			re[2] = (byte) (n / 0x80 + 0x80);
			n %= 0x80;
		}
		if(n > 0)
			re[3] = (byte) (n);
		int cut = 0;
		for(int i = 0; i < re.length; i++) {
			if(re[i] != 0x00) {
				cut = i;
				break;
			}
		}
		byte[] b = new byte[re.length - cut];
		for(int i = 0; i < b.length; i++) {
			b[i] = re[cut + i];
		}
		return b;
	}
	
	/**
	 * Converts MIDI time to decimal time.
	 * 
	 * @param in byte[] containing the time in MIDI time.
	 * @return the time in decimal time
	 */
	public static long midiTimeToDecimal(byte[] in) {
		long re = 0;
		int index = 0;
		for(int i = in.length - 1; i >= 0; i--) {
			if(i == in.length - 1)
				re += in[i];
			else {
				re -= (-1 * in[i] - 0x80) * Math.pow(128, index);
			}
			index++;
		}
		return re;
	}
	
	/**
	 * Converts a note in String form to its MIDI byte value.
	 * 
	 * @param note the note in string format. For example, "3C" or "3c" represents C in the 3rd octave. Ranges from -1C to 9G. Place # for sharps and b for flats after the pitch.
	 * @return the note's byte value
	 * @throws Exception if the note is out of range
	 */
	public static byte stringNoteToByteValue(String note) throws Exception {
		int octave = Integer.parseInt(note.substring(0, 1));
		if(note.charAt(1) == '0') {
			octave = 10;
			note = note.substring(1);
		}
		if(octave < 0 || octave > 10)
			throw new Exception("Octave " + octave + " out of range.");
		String pitch = note.toLowerCase().substring(1);
		byte p = (byte) (octave * 12);
		switch(pitch) {
			case "cb":
			case "b":
				p += 1;
			case "bb":
			case "a#":
				p += 1;
			case "a":
				p += 1;
			case "ab":
			case "g#":
				p += 1;
			case "g":
				p += 1;
			case "gb":
			case "f#":
				p += 1;
			case "f":
				p += 1;
			case "fb":
			case "e":
				p += 1;
			case "eb":
			case "d#":
				p += 1;
			case "d":
				p += 1;
			case "db":
			case "c#":
				p += 1;
				break;
		}
		if(p >= 0)
			return p;
		else
			throw new Exception("Note " + note + " is out of range. Must be within 0C and 10G.");
	}
	
	/**
	 * Convert a byte array integer (4 bytes) to its int value
	 * 
	 * @param b byte[]
	 * @return int
	 */
	public static int byteArrayToInt(byte... b) {
		if(b.length == 4)
			return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
		else if(b.length == 2)
			return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);
			
		return 0;
	}
	
	/**
	 * Transposes the given pitch down to the lowest octaves that contains all notes in the key.
	 * 
	 * @param sharps the number of sharps in the key, negative for flats
	 * @param isMajor true if the key is major
	 * @param pitch the pitch to transpose
	 * @return the transposed pitch
	 */
	public static byte getLowestOctave(int sharps, boolean isMajor, byte pitch) {
		byte base = getBase(sharps, isMajor);
		while(pitch >= 12 + base)
			pitch -= 12;
		return pitch;
	}
	
	/**
	 * Increases a given pitch to be greater than a pitch. Increments by octave.
	 * 
	 * @param pitch the pitch to raise.
	 * @param octave the value to bring the pitch above
	 * @return the increased pitch
	 */
	public static byte increaseToAverageOctave(byte pitch, byte octave) {
		while(pitch < octave)
			pitch += 12;
		return pitch;
	}
	
	/**
	 * Gets the index of an element in an array of bytes.
	 * 
	 * @param arr the array to search
	 * @param elem the byte to search for
	 * @return the index of the element, or -1 if the array does not contain the element
	 */
	public static int getIndex(byte[] arr, byte elem) {
		for(int i = 0; i < arr.length; i++) {
			if(arr[i] == elem)
				return i;
		}
		return -1;
	}
	
}
