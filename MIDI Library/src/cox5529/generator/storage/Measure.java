package cox5529.generator.storage;

import java.util.ArrayList;
import java.util.Collections;

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
	 * @param events the MIDIEvents that make up the measure
	 * @param tie true if the last note of this measure is tied to the first note of the next measure
	 */
	public Measure(ArrayList<MIDIEvent> events, boolean tie) {
		this.events = events;
		this.tie = tie;
		Collections.sort(events);
		this.supports = new ArrayList<ArrayList<MIDIEvent>>();
	}
	
	/**
	 * Gets the average duration of notes in the melody of this measure in MIDI clocks
	 *
	 * @return the average duration of notes in this measure in MIDI clocks
	 */
	public double getAverageSpeed() {
		long sum = 0;
		int count = 0;
		for(int i = 0; i < events.size(); i += 2) {
			sum += (events.get(i + 1).getTimeStamp() - events.get(i).getTimeStamp());
			count++;
		}
		return (sum + 0.0) / count;
	}
	
	/**
	 * Gets the average speed in MIDI clocks for part of a measure
	 * 
	 * @param start the time in MIDI clocks when the measurement should begin or end
	 * @param end true if time marks the end of the measurement period, false if it marks the beginning
	 * @return the average speed of the appropriate half in MIDI clocks
	 */
	public double getPartialAverageSpeed(long start, boolean end) {
		long sum = 0;
		int count = 0;
		if(end) {
			for(int i = 0; i < events.size(); i += 2) {
				MIDIEvent event = events.get(i);
				if(event.getTimeStamp() > start)
					break;
				else {
					sum += (events.get(i + 1).getTimeStamp() - event.getTimeStamp());
					count++;
				}
			}
		} else {
			for(int i = 0; i < events.size(); i += 2) {
				MIDIEvent event = events.get(i);
				if(event.getTimeStamp() < start)
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
	 * @param events the notes in the supporting measure
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
		for(int i = 0; i < supports.size(); i++) {
			ArrayList<MIDIEvent> arr = new ArrayList<MIDIEvent>();
			ArrayList<MIDIEvent> sup = supports.get(i);
			for(int j = 0; j < sup.size(); j++) {
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
		for(int i = 0; i < events.size(); i++) {
			re.add(events.get(i));
		}
		return re;
	}
	
	/**
	 * Returns true if the last note of this measure is tied to the first note of the next measure
	 * 
	 * @return true if the last note of this measure is tied to the first note of the next measure
	 */
	public boolean isTie() {
		return tie;
	}
	
}
