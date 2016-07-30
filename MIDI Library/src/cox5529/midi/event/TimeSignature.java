package cox5529.midi.event;

/**
 * Class that contains the change time signature meta event.
 * 
 * @author Brandon Cox
 * 		
 */
public class TimeSignature extends MetaEvent {
	
	private byte num;
	private byte den;
	
	/**
	 * Constructs a TimeSignature MetaEvent.
	 * 
	 * @param timestamp the time at which the time signature changes
	 * @param num the numerator of the time signature
	 * @param den the denominator of the time signature
	 * @return a new TimeSignature event
	 */
	public static TimeSignature construct(long timestamp, byte num, byte den) {
		byte[] data = new byte[] { 0x58, 0x04, num, (byte) (Math.log(den) / Math.log(2)), 24, 8 };
		return new TimeSignature(num, den, timestamp, data);
	}
	
	private TimeSignature(byte num, byte den, long timestamp, byte[] data) {
		super(timestamp, data);
		this.num = num;
		this.den = den;
	}
	
	/**
	 * Gets the numerator of this TimeSignature event.
	 * 
	 * @return the numerator of this TimeSignature event.
	 */
	public byte getNumerator() {
		return num;
	}
	
	/**
	 * Gets the denominator of this TimeSignature event.
	 * 
	 * @return the denominator of this TimeSignature event.
	 */
	public byte getDenominator() {
		return den;
	}
}
