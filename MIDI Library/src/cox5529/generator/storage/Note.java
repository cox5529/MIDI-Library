/**
 * 
 */
package cox5529.generator.storage;

import cox5529.midi.event.MIDIEvent;

/**
 * Object used to store notes
 * 
 * @author Brandon Cox
 * 		
 */
public class Note implements Comparable<Note> {
	
	private long start;
	private long stop;
	
	private byte pitch;
	
	/**
	 * Constructs a new Note object.
	 * 
	 * @param start the time at which the note begins
	 * @param stop the time at which the note ends
	 * @param pitch the pitch of the Note
	 */
	public Note(long start, long stop, byte pitch) {
		this.start = start;
		this.stop = stop;
		this.pitch = pitch;
	}
	
	/**
	 * Gets the MIDIEvents representing this Note
	 * 
	 * @param vol the volume of the MIDIEvents
	 * @return an array with 2 MIDIEvents, the first being a note_on and the second a note_off
	 */
	public MIDIEvent[] toEvents(byte vol) {
		if(pitch != -1) {
			MIDIEvent[] re = new MIDIEvent[2];
			re[0] = new MIDIEvent(start, (byte) 0x90, new byte[] { pitch, vol });
			re[1] = new MIDIEvent(stop, (byte) 0x90, new byte[] { pitch, 0 });
			return re;
		}
		return null;
	}
	
	/**
	 * Determines if a given time occurs within the times in which this note is playing
	 * 
	 * @param time the time to test
	 * @return true if this note is playing at the given time
	 */
	public boolean isDuring(long time) {
		return (time >= start) && (time <= stop);
	}
	
	/**
	 * Gets the duration of this note
	 * 
	 * @return the duration of this note in MIDI clocks
	 */
	public long getDuration() {
		return stop - start;
	}
	
	/**
	 * Sets the duration of the note while keeping the start time of the note the same
	 * 
	 * @param dur the new duration of the note
	 */
	public void setDuration(long dur) {
		this.stop = start + dur;
	}
	
	/**
	 * Gets the start timestamp of the note
	 * 
	 * @return the start timestamp of the note
	 */
	public long getStart() {
		return start;
	}
	
	/**
	 * Sets the start timestamp of the note
	 * 
	 * @param start the new start timestamp of the note
	 */
	public void setStart(long start) {
		this.start = start;
	}
	
	/**
	 * Gets the stop timestamp of the note
	 * 
	 * @return the stop timestamp of the note
	 */
	public long getStop() {
		return stop;
	}
	
	/**
	 * Sets the stop timestamp of the note
	 * 
	 * @param stop the new stop timestamp of the note
	 */
	public void setStop(long stop) {
		this.stop = stop;
	}
	
	/**
	 * Gets the pitch of the note.
	 * 
	 * @return the pitch of the note, -127 if rest
	 */
	public byte getPitch() {
		return pitch;
	}
	
	/**
	 * Sets the pitch of the note.
	 * 
	 * @param pitch the pitch of the note, -127 if rest
	 */
	public void setPitch(byte pitch) {
		this.pitch = pitch;
	}
	
	/**
	 * Determines if this note is a rest
	 * 
	 * @return true if the note is a rest
	 */
	public boolean isRest() {
		return pitch == -127;
	}
	
	/**
	 * Gets the number of beats since the beginning of the song for this note
	 * 
	 * @param res the resolution of this note
	 * @return the number of beats since the beginning of the song
	 */
	public int getAbsoluteBeatNumber(int res) {
		return (int) (getBeatNumber(res) + getMeasureNumber(res) * 4);
	}
	
	/**
	 * Gets the measure number in which this note begins. Assumes 4/4 time.
	 * 
	 * @param res the resolution of the note
	 * @return the measure number in which this note begins
	 */
	public int getMeasureNumber(int res) {
		return (int) (start / (4 * res));
	}
	
	/**
	 * Gets the beat number on which this note begins
	 * 
	 * @param res the resolution of the note
	 * @return the beat number on which this note begins
	 */
	public double getBeatNumber(int res) {
		int measure = getMeasureNumber(res);
		int mStart = measure * res * 4;
		return (double) ((start - mStart + 0.0) / res) + 1;
	}
	
	/**
	 * Gets the String representation of this note
	 * 
	 * @return the String representation of this note
	 */
	public String toString() {
		return String.format("Start: %d\tStop: %d\tPitch: %d", start, stop, pitch);
	}
	
	@Override public int compareTo(Note n) {
		long s = n.getStart();
		if(s > start)
			return -1;
		else if(s < start)
			return 1;
		return 0;
	}
	
	@Override public boolean equals(Object o) {
		if(o instanceof Note) {
			Note n = (Note) o;
			return(n.getDuration() == getDuration() && n.getPitch() == getPitch());
		} else
			return false;
	}
	
	@Override public Note clone() {
		return new Note(start, stop, pitch);
	}
	
}
