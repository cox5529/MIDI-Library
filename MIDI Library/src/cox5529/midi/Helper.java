package cox5529.midi;

/**
 * Various helper methods.
 * 
 * @author Brandon Cox
 * 		
 */
public class Helper {
	
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
}
