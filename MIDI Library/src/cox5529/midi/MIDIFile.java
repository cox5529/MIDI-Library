package cox5529.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import cox5529.generator.storage.Measure;
import cox5529.midi.event.MIDIEvent;
import cox5529.midi.event.Tempo;
import cox5529.midi.event.TimeSignature;
import cox5529.midi.track.MusicTrack;

/**
 * Class used to store a MIDI file or song.
 * 
 * @author Brandon Cox
 * 
 */
public class MIDIFile {
	
	private int resolution;
	private ArrayList<MusicTrack> tracks;
	
	/**
	 * Constructs a new MIDIFile.
	 */
	public MIDIFile() {
		tracks = new ArrayList<MusicTrack>();
		resolution = 96;
	}
	
	private MIDIFile(ArrayList<MusicTrack> tracks, int resolution) {
		this.tracks = tracks;
		this.resolution = resolution;
	}
	
	/**
	 * Gets the resolution of this MIDIFile.
	 * 
	 * @return the resolution of this MIDIFile.
	 */
	public int getResolution() {
		return this.resolution;
	}
	
	/**
	 * Changes the resolution of this MIDI file.
	 * 
	 * @param res the new resolution of this MIDI file
	 */
	public void setResolution(int res) {
		this.resolution = res;
	}
	
	/**
	 * Creates a new MusicTrack and adds it to this MIDIFile.
	 * 
	 * @return the MusicTrack created
	 */
	public MusicTrack addTrack() {
		tracks.add(new MusicTrack());
		return tracks.get(tracks.size() - 1);
	}
	
	/**
	 * Adds a track to this MIDIFile.
	 * 
	 * @param track the track to add to this MIDIFile
	 */
	public void addTrack(MusicTrack track) {
		tracks.add(track);
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
	 * Gets a random segment of this MIDIFile.
	 * 
	 * @param duration the duration of the segment
	 * @return a random segment of this MIDIFile
	 */
	public MIDIFile getRandomSegment(int duration) {
		int tempo = 120;
		ArrayList<Measure> measures = new ArrayList<Measure>();
		MIDIFile output = new MIDIFile();
		ArrayList<MusicTrack> tracks = getTracks();
		int res = getResolution();
		ArrayList<MIDIEvent> events = tracks.get(0).getEvents();
		ArrayList<MIDIEvent> cur = new ArrayList<MIDIEvent>();
		output.setResolution(res);
		long measureStart = 0;
		long maxDur = res * 4 - 1;
		long curStart = -1;
		byte[] instruments = new byte[tracks.size()];
		long[] volTotal = new long[tracks.size()];
		long[] noteCount = new long[tracks.size()];
		for(int j = 0; j < events.size(); j++) {
			MIDIEvent event = events.get(j);
			if(event.getStatus() == (byte) 0x90 && event.getData()[1] != 0) {
				if(event.getTimeStamp() > measureStart + res * 4) {
					ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
					for(int k = 0; k < cur.size(); k++) {
						toAdd.add(cur.get(k));
					}
					measures.add(new Measure(toAdd, false));
					measureStart += maxDur + 1;
					j--;
					continue;
				}
				volTotal[0] += event.getData()[1];
				noteCount[0]++;
				// Duration stuff
				curStart = event.getTimeStamp();
				event.setTimeStamp(curStart - measureStart);
				cur.add(event);
			} else if(curStart != -1 && (event.getStatus() == (byte) 0x90 && event.getData()[1] == 0) || event.getStatus() == (byte) 0x80) {
				long noteDur = event.getTimeStamp() - curStart;
				if(noteDur == 455 || noteDur == 479)
					noteDur = 479;
				else if(noteDur == 227 || noteDur == 239)
					noteDur = 239;
				else if(noteDur == 113 || noteDur == 119)
					noteDur = 119;
				long dur = curStart + noteDur - measureStart;
				event.setTimeStamp(curStart + noteDur - measureStart);
				curStart = -1;
				cur.add(event);
				if(dur >= maxDur || maxDur - dur < 1.0 / 32 * res) {
					boolean tie = false;
					if(dur > maxDur) {
						cur.remove(cur.size() - 1);
						cur.add(new MIDIEvent(maxDur, (byte) 0x90, new byte[] { events.get(j - 1).getData()[0], (byte) 0x00 }));
						tie = true;
					}
					ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
					for(int k = 0; k < cur.size(); k++) {
						toAdd.add(cur.get(k));
					}
					measures.add(new Measure(toAdd, tie));
					cur.clear();
					if(tie) {
						cur.add(new MIDIEvent(0, (byte) 0x90, events.get(j - 1).getData()));
						cur.add(new MIDIEvent(dur - maxDur, (byte) 0x90, event.getData()));
					}
					measureStart += maxDur + 1;
				}
			} else if(event.getStatus() == (byte) 0xFF && event.getData()[0] == 0x51) {
				byte[] data = event.getData();
				tempo = (int) (0.00006 * new BigInteger(new byte[] { data[2], data[3], data[4] }).intValue());
			} else if(event.getStatus() == (byte) 0xC0) {
				instruments[0] = event.getData()[0];
			}
		}
		ArrayList<MIDIEvent> toAdd1 = new ArrayList<MIDIEvent>();
		for(int k = 0; k < cur.size(); k++) {
			toAdd1.add(cur.get(k));
		}
		measures.add(new Measure(toAdd1, false));
		
		for(int i = 1; i < tracks.size(); i++) {
			cur.clear();
			measureStart = 0;
			ArrayList<MIDIEvent> supportEvents = tracks.get(i).getEvents();
			curStart = -1;
			int mCount = 0;
			for(int j = 0; j < supportEvents.size(); j++) {
				MIDIEvent event = supportEvents.get(j);
				int status = Byte.toUnsignedInt(event.getStatus());
				if(status / 16 == 0x9 && event.getData()[1] != 0) {
					if(event.getTimeStamp() > measureStart + res * 4 - 1) {
						toAdd1.clear();
						for(int k = 0; k < cur.size(); k++) {
							toAdd1.add(cur.get(k));
						}
						measures.get(mCount).addSupport(toAdd1);
						mCount++;
						measureStart += maxDur + 1;
						j--;
						continue;
					}
					volTotal[i] += event.getData()[1];
					noteCount[i]++;
					curStart = event.getTimeStamp();
					event.setTimeStamp(curStart - measureStart);
					cur.add(event);
				} else if(curStart != -1 && (status / 16 == 0x9 && event.getData()[1] == 0) || event.getStatus() == (byte) 0x80) {
					long noteDur = event.getTimeStamp() - curStart;
					if(noteDur == 455 || noteDur == 479)
						noteDur = 479;
					else if(noteDur == 227 || noteDur == 239)
						noteDur = 239;
					else if(noteDur == 113 || noteDur == 119)
						noteDur = 119;
					long dur = curStart + noteDur - measureStart;
					event.setTimeStamp(curStart + noteDur - measureStart);
					curStart = -1;
					cur.add(event);
					if(dur >= maxDur || maxDur - dur < 1.0 / 32 * res) {
						boolean tie = false;
						if(dur > maxDur) {
							cur.remove(cur.size() - 1);
							cur.add(new MIDIEvent(maxDur, (byte) (0x9 * 16 + j + 1), new byte[] { supportEvents.get(j - 1).getData()[0], (byte) 0x00 }));
							tie = true;
						}
						ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
						for(int k = 0; k < cur.size(); k++) {
							toAdd.add(cur.get(k));
						}
						measures.get(mCount).addSupport(toAdd);
						mCount++;
						cur.clear();
						if(tie) {
							cur.add(new MIDIEvent(0, (byte) (0x9 * 16 + j + 1), supportEvents.get(j - 1).getData()));
							cur.add(new MIDIEvent(dur - maxDur, (byte) (0x9 * 16 + j + 1), event.getData()));
						}
						measureStart += maxDur + 1;
					}
				} else if(status / 16 == 0xC) {
					instruments[i] = event.getData()[0];
				}
			}
			toAdd1.clear();
			for(int k = 0; k < cur.size(); k++) {
				toAdd1.add(cur.get(k));
			}
			if(measures.size() > mCount)
				measures.get(mCount).addSupport(toAdd1);
		}
		
		byte[] volAve = new byte[volTotal.length];
		for(int i = 0; i < volTotal.length; i++) {
			volAve[i] = (byte) (volTotal[i] / noteCount[i]);
		}
		
		int start = (int) (Math.random() * (measures.size() - duration - 1));
		MusicTrack[] outTracks = new MusicTrack[tracks.size()];
		for(int i = 0; i < outTracks.length; i++) {
			outTracks[i] = new MusicTrack();
		}
		outTracks[0].addEvent(TimeSignature.construct(0, (byte) 4, (byte) 4));
		outTracks[0].addEvent(Tempo.construct(0, tempo));
		for(int i = 0; i < outTracks.length; i++) {
			outTracks[i].changeInstrument(0, i, instruments[i]);
		}
		for(int i = 0; i < duration; i++) {
			int pos = i * res * 4;
			ArrayList<MIDIEvent> outEvents = measures.get(i + start).getEvents();
			for(int j = 0; j < outEvents.size(); j++) {
				MIDIEvent event = outEvents.get(j);
				event.setTimeStamp(pos + event.getTimeStamp());
				event.setData(new byte[] { event.getData()[0], (event.getData()[1] == 0 ? 0: volAve[0]) });
				outTracks[0].addEvent(event);
			}
			ArrayList<ArrayList<MIDIEvent>> supports = measures.get(i + start).getSupport();
			for(int j = 0; j < supports.size(); j++) {
				ArrayList<MIDIEvent> supEvents = supports.get(j);
				for(int k = 0; k < supEvents.size(); k++) {
					MIDIEvent event = supEvents.get(k);
					event.setTimeStamp(event.getTimeStamp() + pos);
					event.setData(new byte[] { event.getData()[0], (event.getData()[1] == 0 ? 0: volAve[j + 1]) });
					outTracks[j + 1].addEvent(event);
				}
			}
		}
		for(int i = 0; i < outTracks.length; i++) {
			output.addTrack(outTracks[i]);
		}
		return output;
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
		fos.write(ByteBuffer.allocate(4).putInt(tracks.size()).array(), 2, 2);
		fos.write(ByteBuffer.allocate(4).putInt(resolution).array(), 2, 2);
		if(debug) {
			System.out.println("Wrote MIDI header.");
		}
		for(int i = 0; i < tracks.size(); i++) {
			fos.write(tracks.get(i).toOutputArray(debug));
			if(debug) {
				System.out.println("Wrote track " + (1 + i) + ".");
			}
		}
		fos.close();
		if(debug)
			System.out.println("Wrote file \"" + f.getName() + "\".");
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
		if(debug)
			System.out.println("Read file \"" + f.getName() + "\".");
		return out;
	}
}
