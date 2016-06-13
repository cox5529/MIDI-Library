package cox5529.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import cox5529.midi.track.MetaTrack;
import cox5529.midi.track.MusicTrack;

/**
 * Class used to store a MIDI file or song.
 * 
 * @author Brandon Cox
 * 		
 */
public class MIDIFile {
	
	private int resolution;
	private MetaTrack meta;
	private ArrayList<MusicTrack> tracks;
	
	/**
	 * Constructs a new MIDIFile.
	 */
	public MIDIFile() {
		meta = new MetaTrack();
		tracks = new ArrayList<MusicTrack>();
		resolution = 128;
	}
	
	/**
	 * Creates a new MusicTrack and adds it to this MIDIFile.
	 */
	public void addTrack() {
		tracks.add(new MusicTrack());
	}
	
	/**
	 * Gets the ArrayList of MusicTracks within this MIDIFile
	 * 
	 * @return the ArrayList of MusicTracks within this MIDIFile
	 */
	public ArrayList<MusicTrack> getTracks() {
		return tracks;
	}
	
	/**
	 * Gets the MetaTrack that corresponds to this file.
	 * 
	 * @return the MetaTrack that corresponds to this file
	 */
	public MetaTrack getMetaTrack() {
		return meta;
	}
	
	/**
	 * Converts this MIDIFile to a .mid file
	 * 
	 * @param debug true if status should be printed to the console.
	 * @param f the file to write the MIDITrack object to. Should end with ".mid".
	 * @throws IOException if the file is not found or there is an error
	 */
	public void write(File f, boolean debug) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(new byte[] { 0x4D, 0x54, 0x68, 0x64 }); // Literal "MThd"
		fos.write(new byte[] { 0x00, 0x00, 0x00, 0x06 });
		fos.write(new byte[] { 0x00, 0x01 });
		fos.write(ByteBuffer.allocate(4).putInt(tracks.size() + 1).array(), 2, 2);
		fos.write(ByteBuffer.allocate(4).putInt(resolution).array(), 2, 2);
		if(debug) {
			System.out.println("Wrote MIDI header.");
		}
		fos.write(meta.toOutputArray(debug));
		fos.write(new byte[] { (byte) 0x00, (byte) 0xFF, 0x2F, 0x00 });
		if(debug) {
			System.out.println("Wrote Meta track.");
		}
		for(int i = 0; i < tracks.size(); i++) {
			fos.write(tracks.get(i).toOutputArray(debug));
			fos.write(new byte[] { (byte) 0x00, (byte) 0xFF, 0x2F, 0x00 });
			if(debug) {
				System.out.println("Wrote track " + (1 + i) + ".");
			}
		}
		fos.close();
	}
}
