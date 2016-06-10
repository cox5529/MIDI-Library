import java.io.File;
import java.io.IOException;

import cox5529.midi.MIDIFile;
import cox5529.midi.track.MusicTrack;

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
		
		MIDIFile file = new MIDIFile();
		file.addTrack();
		MusicTrack t = file.getTracks().get(0);
		t.addNote(128, 128, 0, (byte) 0x3C, (byte) 127);
		t.changeInstrument(256, 0, (byte) 74);
		t.addNote(256, 256, 0, (byte) 0x3C, (byte) 127);
		try {
			file.write(new File("test.mid"), true);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}
