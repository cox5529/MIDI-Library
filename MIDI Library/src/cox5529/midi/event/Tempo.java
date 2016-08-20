/**
 * 
 */
package cox5529.midi.event;

import java.nio.ByteBuffer;

/**
 * @author Brandon Cox
 * 		
 */
public class Tempo extends MIDIEvent {
	
	private Tempo(long timeStamp, byte status, byte[] data) {
		super(timeStamp, status, data);
	}
	
	/**
	 * Constructs a new Tempo object
	 * 
	 * @param timestamp the timestamp to change the tempo at
	 * @param tempo the tempo to change to
	 * @return the constructed tempo object
	 */
	public static Tempo construct(long timestamp, int tempo) {
		byte[] data = new byte[5];
		data[0] = 0x51;
		data[1] = 0x03;
		byte[] temp = ByteBuffer.allocate(4).putInt(60000000 / tempo).array();
		data[2] = temp[1];
		data[3] = temp[2];
		data[4] = temp[3];
		return new Tempo(timestamp, (byte) 0xFF, data);
	}
	
	/**
	 * Gets the string representation of this Tempo object
	 * 
	 * @return the string representation of this Tempo object
	 */
	public String toString() {
		return super.toString();
	}
}
