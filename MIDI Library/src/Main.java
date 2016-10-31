import java.io.File;
import java.io.IOException;

import cox5529.generator.SimpleCompositions;
import cox5529.midi.MIDIFile;

/**
 * Test code.
 * 
 * @author Brandon Cox
 * 		
 */
public class Main {
	
	/**
	 * Test code.
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) {
		String src = "pachelbel-s.mid";
		int duration = 10;
		int x = 2;
		int y = 4;
		int z = 1000;
		System.out.println(src);
		try {
			MIDIFile source = MIDIFile.read(new File(src), false);
			SimpleCompositions sc = new SimpleCompositions(duration, x, y, z, source);
			// sc.generateSong().write(new File("out.mid"), false);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
