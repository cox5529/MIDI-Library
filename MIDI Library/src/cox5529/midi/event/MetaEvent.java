package cox5529.midi.event;

import cox5529.midi.track.MusicTrack;

/**
 * Class used to store Meta MIDI Events (0xFF)
 * 
 * @author Brandon Cox
 * 		
 */
public class MetaEvent extends MIDIEvent {
	
	private MetaEvent(long timestamp, byte[] data) {
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
		byte[] var = MusicTrack.decimalToMIDITime(data.length);
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
}
