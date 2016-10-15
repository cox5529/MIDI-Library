package cox5529.generator.storage;

import java.util.ArrayList;
import java.util.Collections;

import cox5529.midi.Helper;
import cox5529.midi.event.MIDIEvent;

/**
 * Class used to store measures.
 * 
 * @author Brandon Cox
 * 
 */
public class Measure {

	private ArrayList<MIDIEvent> events;
	private ArrayList<ArrayList<MIDIEvent>> supports;
	private boolean tie;

	/**
	 * Constructs a measure object.
	 * 
	 * @param events
	 *            the MIDIEvents that make up the measure
	 * @param tie
	 *            true if the last note of this measure is tied to the first
	 *            note of the next measure
	 */
	public Measure(ArrayList<MIDIEvent> events, boolean tie) {
		this.events = events;
		this.tie = tie;
		Collections.sort(events);
		this.supports = new ArrayList<ArrayList<MIDIEvent>>();
	}

	/**
	 * Gets the chords within this measure.
	 * 
	 * @param sharps
	 *            number of sharps in the key
	 * @param isMajor
	 *            true if the key is major
	 * 
	 * @return the chords within this measure represented as a 2D array of
	 *         bytes, each row representing a chord.
	 */
	public byte[][] getChords(int sharps, boolean isMajor) {
		byte[][] re;
		int minNotes = 900;
		ArrayList<MIDIEvent> minTrack = null;
		for (int i = 0; i < supports.size(); i++) {
			ArrayList<MIDIEvent> track = supports.get(i);
			if (track.size() / 2 < minNotes) {
				minNotes = track.size() / 2;
				minTrack = track;
			}
		}
		if (minNotes == 0)
			return new byte[][] {};
		re = new byte[minNotes][];
		// find chord notes
		long[] stamps = new long[minNotes];
		int index = 0;
		for (int i = 0; i < minTrack.size(); i++) {
			int status = Byte.toUnsignedInt(minTrack.get(i).getStatus());
			if (status / 0x10 == 0x9 && minTrack.get(i).getData()[1] != 0) {
				stamps[index] = minTrack.get(i).getTimeStamp();
				index++;
			}
		}
		for (int i = 0; i < minNotes; i++) {
			byte[] pitches = new byte[supports.size() + 1];
			// loop through each track
			for (int j = events.size() - 1; j >= 0; j--) {
				if (events.get(j).getTimeStamp() <= stamps[i]) {
					pitches[0] = events.get(j).getData()[0];
					break;
				}
			}
			for (int j = 0; j < supports.size(); j++) {
				ArrayList<MIDIEvent> track = supports.get(j);
				for (int k = track.size() - 1; k >= 0; k--) {
					if (track.get(k).getTimeStamp() <= stamps[i]) {
						pitches[j + 1] = track.get(k).getData()[0];
						break;
					}
				}
			}
			// get numeral and add to array
			re[i] = Helper.getChord(sharps, isMajor, pitches);
		}
		// return
		return re;
	}

	/**
	 * Gets the average duration of notes in the melody of this measure in MIDI
	 * clocks
	 *
	 * @return the average duration of notes in this measure in MIDI clocks
	 */
	public double getAverageSpeed() {
		long sum = 0;
		int count = 0;
		for (int i = 0; i < events.size(); i += 2) {
			sum += (events.get(i + 1).getTimeStamp() - events.get(i).getTimeStamp());
			count++;
		}
		return (sum + 0.0) / count;
	}

	/**
	 * Gets the average speed in MIDI clocks for part of a measure
	 * 
	 * @param start
	 *            the time in MIDI clocks when the measurement should begin or
	 *            end
	 * @param end
	 *            true if time marks the end of the measurement period, false if
	 *            it marks the beginning
	 * @return the average speed of the appropriate half in MIDI clocks
	 */
	public double getPartialAverageSpeed(long start, boolean end) {
		long sum = 0;
		int count = 0;
		if (end) {
			for (int i = 0; i < events.size(); i += 2) {
				MIDIEvent event = events.get(i);
				if (event.getTimeStamp() > start)
					break;
				else {
					sum += (events.get(i + 1).getTimeStamp() - event.getTimeStamp());
					count++;
				}
			}
		} else {
			for (int i = 0; i < events.size(); i += 2) {
				MIDIEvent event = events.get(i);
				if (event.getTimeStamp() < start)
					continue;
				else {
					sum += (events.get(i + 1).getTimeStamp() - event.getTimeStamp());
					count++;
				}
			}
		}
		return (sum + 0.0) / count;
	}

	/**
	 * Adds the supporting melody to this measure
	 * 
	 * @param events
	 *            the notes in the supporting measure
	 */
	public void addSupport(ArrayList<MIDIEvent> events) {
		supports.add(events);
	}

	/**
	 * Gets the supporting measures for this measure.
	 * 
	 * @return the supporting measures
	 */
	public ArrayList<ArrayList<MIDIEvent>> getSupport() {
		ArrayList<ArrayList<MIDIEvent>> re = new ArrayList<ArrayList<MIDIEvent>>();
		for (int i = 0; i < supports.size(); i++) {
			ArrayList<MIDIEvent> arr = new ArrayList<MIDIEvent>();
			ArrayList<MIDIEvent> sup = supports.get(i);
			for (int j = 0; j < sup.size(); j++) {
				arr.add(sup.get(j));
			}
			re.add(arr);
		}
		return re;
	}

	/**
	 * Gets the list of MIDIEvents that makes up this measure.
	 * 
	 * @return the list of MIDIEvents that makes up this measure
	 */
	public ArrayList<MIDIEvent> getEvents() {
		ArrayList<MIDIEvent> re = new ArrayList<MIDIEvent>();
		for (int i = 0; i < events.size(); i++) {
			re.add(events.get(i));
		}
		return re;
	}

	/**
	 * Returns true if the last note of this measure is tied to the first note
	 * of the next measure
	 * 
	 * @return true if the last note of this measure is tied to the first note
	 *         of the next measure
	 */
	public boolean isTie() {
		return tie;
	}

}
