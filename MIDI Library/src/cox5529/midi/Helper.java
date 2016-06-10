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
	
}
