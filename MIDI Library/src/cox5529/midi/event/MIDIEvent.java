package cox5529.midi.event;

import cox5529.midi.Helper;

/**
 * Class used to store a MIDI event.
 * 
 * @author Brandon Cox
 * 		
 */
public class MIDIEvent {
	
	private long timeStamp;
	private byte status;
	private byte[] data;
	
	/**
	 * Constructs a new MIDIEvent object
	 * 
	 * @param timeStamp the time at which the event occurs
	 * @param status the status byte
	 * @param data the data bytes
	 */
	public MIDIEvent(long timeStamp, byte status, byte[] data) {
		this.timeStamp = timeStamp;
		this.status = status;
		this.data = data;
	}
	
	/**
	 * Converts a MIDIEvent object to a byte[] in order to write the event to a file
	 * 
	 * @param prevTime the time of the previous event. If this is the first event in the track, then prevTime should be 0.
	 * @return the byte[] representation of the MIDIEvent
	 */
	public byte[] toByteArray(long prevTime) {
		byte[] stamp = Helper.decimalToMIDITime(timeStamp - prevTime);
		byte[] b = new byte[getSize(prevTime)];
		for(int i = 0; i < stamp.length; i++) {
			b[i] = stamp[i];
		}
		b[stamp.length] = status;
		for(int i = 0; i < data.length; i++) {
			b[stamp.length + 1 + i] = data[i];
		}
		return b;
	}
	
	/**
	 * Gets the size of this MIDIEvent in bytes
	 * 
	 * @param prevTime the time of the previous event. If this is the first event in the track, then prevTime should be 0.
	 * @return the size of this MIDIEvent in bytes
	 */
	public int getSize(long prevTime) {
		byte[] stamp = Helper.decimalToMIDITime(timeStamp - prevTime);
		return stamp.length + 1 + data.length;
	}
	
	/**
	 * Gets the timeStamp of this event.
	 * 
	 * @return the timeStamp of this event in MIDI ticks.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Sets the timeStamp of this MIDIEvent
	 * 
	 * @param timeStamp the timeStamp to change this MIDIEvent to
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * Gets the status byte of this MIDIEvent
	 * 
	 * @return the status byte of this MIDIEvent
	 */
	public byte getStatus() {
		return status;
	}
	
	/**
	 * Sets the status byte of this MIDIEvent
	 * 
	 * @param status what the status byte is being changed to
	 */
	public void setStatus(byte status) {
		this.status = status;
	}
	
	/**
	 * Gets the data bytes of this MIDIEvent
	 * 
	 * @return the data bytes of this MIDIEvent
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Sets the data bytes of this MIDIEvent
	 * 
	 * @param data what the data bytes are being changed to
	 */
	public void setData2(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Gets the String representation of this MIDIEvent.
	 * 
	 * @param prevTime the time of the previous MIDIEvent in this track.
	 * @return the String representation of this MIDIEvent
	 */
	public String toString(long prevTime) {
		byte[] bytes = toByteArray(prevTime);
		String re = "";
		for(int i = 0; i < bytes.length; i++) {
			re += String.format("%02X", bytes[i]) + (i == bytes.length - 1 ? "": " ");
		}
		return re;
		
	}
	
}
