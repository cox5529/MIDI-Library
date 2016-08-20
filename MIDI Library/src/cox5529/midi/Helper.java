package cox5529.midi;

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
	 * Generates an array of bytes that will represent the pitches of a specified chord.
	 * 
	 * @param sharps the number of sharps in the key, or number of flats * -1
	 * @param isMajor true if major, false if minor
	 * @param low the base note of the chord
	 * @return the array of bytes representing the chord or an empty array if no chord will meet the requirements of the key.
	 */
	public static byte[] getChord(int sharps, boolean isMajor, byte low) {
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
		int b = base;
		while(b <= low - 12)
			b += 12;
		int offset = b - base;
		byte[] key = new byte[7];
		if(isMajor) {
			key[0] = base;
			key[1] = (byte) (key[0] + 2);
			key[2] = (byte) (key[1] + 2);
			key[3] = (byte) (key[2] + 1);
			key[4] = (byte) (key[3] + 2);
			key[5] = (byte) (key[4] + 2);
			key[6] = (byte) (key[5] + 2);
		} else {
			key[0] = base;
			key[1] = (byte) (key[0] + 2);
			key[2] = (byte) (key[1] + 1);
			key[3] = (byte) (key[2] + 2);
			key[4] = (byte) (key[3] + 2);
			key[5] = (byte) (key[4] + 1);
			key[6] = (byte) (key[5] + 2);
		}
		int deg = 0;
		for(int i = 0; i < key.length; i++) {
			key[i] += offset;
			if(key[i] == low) {
				deg = i + 1;
				break;
			}
		}
		if((deg == 1 || deg == 4 || deg == 5) && !isMajor)
			if(deg == 1 || deg == 5)
				deg++;
			else if(deg == 4)
				deg--;
		if((deg == 2 || deg == 3 || deg == 6) && isMajor) {
			if(deg == 2 || deg == 6)
				deg--;
			else if(deg == 3)
				deg++;
		}
		if((deg == 1 || deg == 4 || deg == 5) && isMajor) {
			return new byte[] { low, (byte) (low + 4), (byte) (low + 7) };
		} else if((deg == 2 || deg == 3 || deg == 6) && !isMajor) {
			return new byte[] { low, (byte) (low + 3), (byte) (low + 7) };
		} else if(deg == 7)
			return new byte[] { low, (byte) (low + 3), (byte) (low + 6) };
		else {
			return new byte[] {};
		}
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
	
}
