package cox5529.midi.event;

import cox5529.midi.track.MusicTrack;

/**
 * Class used to store a MIDI event.
 * 
 * @author Brandon Cox
 * 		
 */
public class MIDIEvent {
	
	private long timeStamp;
	private byte status;
	private byte data1;
	private byte data2;
	private int dataBytes;
	
	/**
	 * Constructs a new MIDIEvent object
	 * 
	 * @param timeStamp the time at which the event occurs
	 * @param status the status byte
	 * @param data1 the data1 byte
	 */
	public MIDIEvent(long timeStamp, byte status, byte data1) {
		this.timeStamp = timeStamp;
		this.status = status;
		this.data1 = data1;
		dataBytes = 1;
	}
	
	/**
	 * Constructs a new MIDIEvent object
	 * 
	 * @param timeStamp the time at which the event occurs
	 * @param status the status byte
	 * @param data1 the data1 byte
	 * @param data2 the data2 byte
	 */
	public MIDIEvent(long timeStamp, byte status, byte data1, byte data2) {
		this.timeStamp = timeStamp;
		this.status = status;
		this.data1 = data1;
		this.data2 = data2;
		dataBytes = 2;
	}
	
	/**
	 * Converts a MIDIEvent object to a byte[] in order to write the event to a file
	 * 
	 * @param prevTime the time of the previous event. If this is the first event in the track, then prevTime should be 0.
	 * @return the byte[] representation of the MIDIEvent
	 */
	public byte[] toByteArray(long prevTime) {
		byte[] stamp = MusicTrack.decimalToMIDITime(timeStamp - prevTime);
		byte[] b = new byte[getSize(prevTime)];
		for(int i = 0; i < stamp.length; i++) {
			b[i] = stamp[i];
		}
		b[stamp.length] = status;
		if(dataBytes > 0)
			b[stamp.length + 1] = data1;
		if(dataBytes > 1)
			b[stamp.length + 2] = data2;
		for(int i = 0; i < b.length; i++)
			System.out.printf("%d: %02X\n", prevTime, b[i]);
		return b;
	}
	
	/**
	 * Gets the size of this MIDIEvent in bytes
	 * 
	 * @param prevTime the time of the previous event. If this is the first event in the track, then prevTime should be 0.
	 * @return the size of this MIDIEvent in bytes
	 */
	public int getSize(long prevTime) {
		byte[] stamp = MusicTrack.decimalToMIDITime(timeStamp - prevTime);
		return stamp.length + 1 + dataBytes;
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
	 * Gets the first data byte of this MIDIEvent
	 * 
	 * @return the first data byte of this MIDIEvent
	 */
	public byte getData1() {
		return data1;
	}
	
	/**
	 * Sets the first data byte of this MIDIEvent
	 * 
	 * @param data1 what the first data byte is being changed to
	 */
	public void setData1(byte data1) {
		this.data1 = data1;
	}
	
	/**
	 * Gets the second data byte of this MIDIEvent
	 * 
	 * @return the second data byte of this MIDIEvent
	 */
	public byte getData2() {
		return data2;
	}
	
	/**
	 * Sets the second data byte of this MIDIEvent
	 * 
	 * @param data2 what the second data byte is being changed to
	 */
	public void setData2(byte data2) {
		this.data2 = data2;
	}
	
}
