package cox5529.midi.event;

import java.util.Arrays;

import cox5529.midi.Helper;

/**
 * Class used to store Meta MIDI Events (0xFF)
 * 
 * @author Brandon Cox
 * 		
 */
public class MetaEvent extends MIDIEvent {
	
	/**
	 * Constructs a new MetaEvent.
	 * 
	 * @param timestamp the time at which the MetaEvent occurs
	 * @param data the data bytes for the MetaEvent
	 */
	public MetaEvent(long timestamp, byte[] data) {
		super(timestamp, (byte) 0xFF, data);
	}
	
	/**
	 * Constructs a new MetaEvent.
	 * 
	 * @param timestamp the time at which the MetaEvent occurs
	 * @param type the type of MetaEvent as a byte
	 * @param data the data of the MetaEvent
	 * @return a new MetaEvent object
	 */
	public static MetaEvent construct(long timestamp, byte type, byte[] data) {
		byte[] var = Helper.decimalToMIDITime(data.length);
		byte[] b = new byte[var.length + 1 + data.length];
		b[0] = type;
		for(int i = 0; i < var.length; i++) {
			b[i + 1] = var[i];
		}
		for(int i = 0; i < data.length; i++) {
			b[i + 1 + var.length] = data[i];
		}
		return new MetaEvent(timestamp, b);
	}
	
	/**
	 * Gets the type of MetaEvent.
	 * 
	 * @return the byte value of the type of MetaEvent
	 */
	public byte getType() {
		return getData()[0];
	}
	
	/**
	 * Constructs a new MetaEvent from a byte array.
	 * 
	 * @param in the byte array to construct the event from
	 * @param time the time at which this MetaEvent occurs
	 * @return the new MetaEvent
	 */
	public static MetaEvent readFromByteArray(byte[] in, long time) {
		int end = 2;
		end += Byte.toUnsignedInt(in[1]);
		return new MetaEvent(time, Arrays.copyOfRange(in, 0, end));
	}
}
