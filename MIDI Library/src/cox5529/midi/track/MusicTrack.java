package cox5529.midi.track;

import cox5529.midi.event.MIDIEvent;

/**
 * Class used to store music tracks within a MIDI file.
 * 
 * @author Brandon Cox
 * 		
 */
public class MusicTrack extends MIDITrack {
	
	/**
	 * Constructs a new MusicTrack
	 */
	public MusicTrack() {
		super();
	}
	
	/**
	 * Adds a note to this MusicTrack
	 * 
	 * @param time the timestamp at which the note begins
	 * @param dur the duration of the note in ticks
	 * @param channel the channel of the note
	 * @param pitch the pitch of the note
	 * @param vol the volume of the note
	 */
	public void addNote(long time, long dur, int channel, byte pitch, byte vol) {
		events.add(new MIDIEvent(time, (byte) (16 * 9 + channel), pitch, vol));
		events.add(new MIDIEvent(time + dur, (byte) (16 * 9 + channel), pitch, (byte) 0));
	}
	
	/**
	 * Changes the instrument of the track.
	 * 
	 * @param time the time time at which the instrument changes
	 * @param channel the channel to change the instrument of
	 * @param instrument the instrument to change to
	 */
	public void changeInstrument(long time, int channel, byte instrument) {
		events.add(new MIDIEvent(time, (byte) (16 * 0xC + channel), instrument));
	}
	
	/**
	 * Converts a time in ticks to its MIDI representation.
	 * 
	 * @param n the time in ticks to convert
	 * @return the time in its MIDI representation
	 */
	public static byte[] decimalToMIDITime(long n) {
		if(n == 0)
			return new byte[] { 0x00 };
		byte[] re = new byte[4];
		boolean val = false;
		if(n > 0x1FFFFF) {
			val = true;
			re[0] = (byte) (n / 0x200000 + 0x80);
			n %= 0x200000;
		}
		if(n > 0x3FFF || val) {
			val = true;
			re[1] = (byte) (n / 0x4000 + 0x80);
			n %= 0x4000;
		}
		if(n > 0x7F || val) {
			val = true;
			re[2] = (byte) (n / 0x80 + 0x80);
			n %= 0x80;
		}
		if(n > 0)
			re[3] = (byte) (n);
		int cut = 0;
		for(int i = 0; i < re.length; i++) {
			if(re[i] != 0x00) {
				cut = i;
				break;
			}
		}
		byte[] b = new byte[re.length - cut];
		for(int i = 0; i < b.length; i++) {
			b[i] = re[cut + i];
		}
		return b;
	}
}
