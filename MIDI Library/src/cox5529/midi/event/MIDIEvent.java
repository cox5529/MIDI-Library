package cox5529.midi.event;

import java.util.Arrays;

import cox5529.midi.Helper;

/**
 * Class used to store a MIDI event.
 * 
 * @author Brandon Cox
 * 		
 */
public class MIDIEvent implements Comparable<MIDIEvent> {
	
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
	 * @param runningStat true if the status byte will be omitted
	 * @return the byte[] representation of the MIDIEvent
	 */
	public byte[] toByteArray(long prevTime, boolean runningStat) {
		byte[] stamp = Helper.decimalToMIDITime(timeStamp - prevTime);
		byte[] b = new byte[getSize(prevTime, runningStat)];
		for(int i = 0; i < stamp.length; i++) {
			b[i] = stamp[i];
		}
		if(!runningStat)
			b[stamp.length] = status;
		for(int i = 0; i < data.length; i++) {
			b[stamp.length + (runningStat ? 0: 1) + i] = data[i];
		}
		return b;
	}
	
	/**
	 * Gets the size of this MIDIEvent in bytes
	 * 
	 * @param prevTime the time of the previous event. If this is the first event in the track, then prevTime should be 0.
	 * @param runningStat true if the status byte will be omitted
	 * @return the size of this MIDIEvent in bytes
	 */
	public int getSize(long prevTime, boolean runningStat) {
		byte[] stamp = Helper.decimalToMIDITime(timeStamp - prevTime);
		return stamp.length + 1 + data.length - (runningStat ? 1: 0);
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
		byte[] data = new byte[this.data.length];
		for(int i = 0; i < data.length; i++) {
			data[i] = this.data[i];
		}
		return data;
	}
	
	/**
	 * Sets the data bytes of this MIDIEvent
	 * 
	 * @param data what the data bytes are being changed to
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Gets the String representation of this MIDIEvent.
	 * 
	 * @param prevTime the time of the previous MIDIEvent in this track.
	 * @return the String representation of this MIDIEvent
	 */
	public String toString(long prevTime) {
		byte[] bytes = toByteArray(prevTime, false);
		String re = "";
		for(int i = 0; i < bytes.length; i++) {
			re += String.format("%02X", bytes[i]) + (i == bytes.length - 1 ? "": " ");
		}
		return re;
		
	}
	
	/**
	 * Gets the String representation of this MIDIEvent.
	 * 
	 * @return the String representation of this MIDIEvent
	 */
	public String toString() {
		String re = timeStamp + String.format(" %02x ", status);
		for(int i = 0; i < data.length; i++) {
			re += String.format("%02X", data[i]) + (i == data.length - 1 ? "": " ");
		}
		return re;
		
	}
	
	/**
	 * Creates a copy of this MIDIEvent with no common references.
	 * 
	 * @return the copy
	 */
	public MIDIEvent clone() {
		byte[] data = new byte[this.data.length];
		for(int i = 0; i < data.length; i++) {
			data[i] = this.data[i];
		}
		return new MIDIEvent(timeStamp, status, data);
	}
	
	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 * 
	 * @param event the MIDIEvent to compare this one to
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	@Override public int compareTo(MIDIEvent event) {
		long ts = event.getTimeStamp();
		if(ts > timeStamp)
			return -1;
		else if(ts == timeStamp)
			if(status == 0xFF)
				return -1;
			else
				return 0;
		else
			return 1;
	}
	
	/**
	 * Constructs a new MIDIEvent from a byte array.
	 * 
	 * @param in the byte array to construct the event from
	 * @param dtime the delta-time at which this MIDIEvent occurs
	 * @param prevTime the time of the preceeding event
	 * @param prevStatus the status byte of the preceeding event
	 * @return the new MIDIEvent
	 */
	public static MIDIEvent readFromByteArray(byte[] in, long dtime, long prevTime, byte prevStatus) {
		byte status = in[0];
		if(status == (byte) 0xFF) {
			return MetaEvent.readFromByteArray(Arrays.copyOfRange(in, 1, in.length), dtime + prevTime);
		}
		int unsigStatus = Byte.toUnsignedInt(status);
		int end = 0;
		int start = 1;
		if(unsigStatus / 0x10 < 8 || unsigStatus / 0x10 > 0x0E) {
			status = prevStatus;
			unsigStatus = Byte.toUnsignedInt(status);
			end--;
			start = 0;
		}
		if(unsigStatus / 0x10 == 0xC || unsigStatus / 0x10 == 0xD)
			end += 2;
		else
			end += 3;
		// if(unsigStatus / 0x10 != 0xB)
		return new MIDIEvent(dtime + prevTime, status, Arrays.copyOfRange(in, start, end));
		// else
		// return new MIDIEvent(0, (byte) 0xFF, new byte[] { 0x2F, 0x00 });
	}
	
}
