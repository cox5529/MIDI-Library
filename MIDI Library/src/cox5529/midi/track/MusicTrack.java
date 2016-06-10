package cox5529.midi.track;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import cox5529.midi.event.MIDIEvent;

/**
 * Class used to store music tracks within a MIDI file.
 * 
 * @author Brandon Cox
 * 		
 */
public class MusicTrack {
	
	private ArrayList<MIDIEvent> events;
	
	/**
	 * Constructs a new MusicTrack
	 */
	public MusicTrack() {
		events = new ArrayList<MIDIEvent>();
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
		events.add(new MIDIEvent(time, (byte) (16 * 9 + channel), new byte[] { pitch, vol }));
		events.add(new MIDIEvent(time + dur, (byte) (16 * 9 + channel), new byte[] { pitch, 0 }));
	}
	
	/**
	 * Changes the instrument of the track.
	 * 
	 * @param time the time time at which the instrument changes
	 * @param channel the channel to change the instrument of
	 * @param instrument the instrument to change to
	 */
	public void changeInstrument(long time, int channel, byte instrument) {
		events.add(new MIDIEvent(time, (byte) (16 * 0xC + channel), new byte[] { instrument }));
	}
	
	/**
	 * Converts this MIDITrack to its output format in order to write to a file.
	 * 
	 * @param debug true if status should be printed to the console.
	 * @return the byte[] representation of this MIDITrack
	 */
	public byte[] toOutputArray(boolean debug) {
		Collections.sort(events);
		ArrayList<byte[]> out = new ArrayList<byte[]>();
		out.add(new byte[] { 0x4D, 0x54, 0x72, 0x6B });
		int trackLength = 4;
		for(int i = 0; i < events.size(); i++) {
			trackLength += events.get(i).getSize((i == 0 ? 0: events.get(i - 1).getTimeStamp()));
		}
		out.add(ByteBuffer.allocate(4).putInt(trackLength).array());
		if(debug)
			System.out.println("Created track header.");
		for(int i = 0; i < events.size(); i++) {
			out.add(events.get(i).toByteArray((i == 0 ? 0: events.get(i - 1).getTimeStamp())));
			if(debug)
				System.out.println("Created MIDI event with data: " + events.get(i).toString((i == 0 ? 0: events.get(i - 1).getTimeStamp())) + ".");
		}
		int len = 0;
		for(int i = 0; i < out.size(); i++) {
			len += out.get(i).length;
		}
		byte[] re = new byte[len];
		int index = 0;
		for(int i = 0; i < out.size(); i++) {
			byte[] arr = out.get(i);
			for(int j = 0; j < arr.length; j++) {
				re[index] = arr[j];
				index++;
			}
		}
		return re;
	}
}
