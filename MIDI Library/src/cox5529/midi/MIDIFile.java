package cox5529.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

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
	
	private MIDIFile(ArrayList<MusicTrack> tracks, int resolution) {
		this.tracks = tracks;
		this.resolution = resolution;
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
		fos.write(ByteBuffer.allocate(4).putInt(tracks.size() + (meta == null ? 0: 1)).array(), 2, 2);
		fos.write(ByteBuffer.allocate(4).putInt(resolution).array(), 2, 2);
		if(debug) {
			System.out.println("Wrote MIDI header.");
		}
		if(meta != null) {
			fos.write(meta.toOutputArray(debug));
			if(debug) {
				System.out.println("Wrote Meta track.");
			}
		}
		for(int i = 0; i < tracks.size(); i++) {
			fos.write(tracks.get(i).toOutputArray(debug));
			if(debug) {
				System.out.println("Wrote track " + (1 + i) + ".");
			}
		}
		fos.close();
	}
	
	/**
	 * Reads a MIDIFile.
	 * 
	 * @param f the file to read from in the .mid format
	 * @param debug true if status should be printed to the console.
	 * @return a MIDIFile object created from the given .mid file
	 * @throws IOException if an I/O error occurs reading from the file
	 */
	public static MIDIFile read(File f, boolean debug) throws IOException {
		byte[] file = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
		if(debug)
			System.out.println("Read file into byte array.");
		int trackCount = file[10] * 256 + file[11];
		int resolution = Byte.toUnsignedInt(file[12]) * 256 + Byte.toUnsignedInt(file[13]);
		System.out.println(resolution);
		int index = 14;
		if(debug)
			System.out.println("Read MIDI header.");
		ArrayList<MusicTrack> tracks = new ArrayList<MusicTrack>();
		for(int i = 0; i < trackCount; i++) {
			int length = Helper.byteArrayToInt(file[index + 4], file[index + 5], file[index + 6], file[index + 7]);
			tracks.add(MusicTrack.byteArrayToTrack(Arrays.copyOfRange(file, index, index + length + 8), debug));
			if(debug)
				System.out.println("Read track.");
			index += length + 8;
		}
		MIDIFile out = new MIDIFile(tracks, resolution);
		return out;
	}
}
