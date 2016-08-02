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
	}
	
	/**
	 * Gets the list of MIDIEvents that makes up this measure.
	 * 
	 * @return the list of MIDIEvents that makes up this measure
	 */
	public ArrayList<MIDIEvent> getEvents() {
		return events;
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
