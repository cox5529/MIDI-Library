package cox5529.midi.track;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import cox5529.midi.event.MIDIEvent;

/**
 * Class used to store a track within a MIDI file.
 * 
 * @author Brandon Cox
 * 		
 */
public class MIDITrack {
	
	/**
	 * The ArrayList of MIDIEvents contained within this MIDITrack
	 */
	protected ArrayList<MIDIEvent> events;
	
	/**
	 * Constructs a new MIDITrack object
	 */
	public MIDITrack() {
		events = new ArrayList<MIDIEvent>();
	}
	
	/**
	 * Converts this MIDITrack to its output format in order to write to a file.
	 * 
	 * @return the byte[] representation of this MIDITrack
	 */
	public byte[] toOutputArray() {
		System.out.println(events.size());
		ArrayList<byte[]> out = new ArrayList<byte[]>();
		out.add(new byte[] { 0x4D, 0x54, 0x72, 0x6B });
		int trackLength = 4;
		for(int i = 0; i < events.size(); i++) {
			trackLength += events.get(i).getSize((i == 0 ? 0: events.get(i - 1).getTimeStamp()));
		}
		out.add(ByteBuffer.allocate(4).putInt(trackLength).array());
		for(int i = 0; i < events.size(); i++) {
			System.out.println(i);
			out.add(events.get(i).toByteArray((i == 0 ? 0: events.get(i - 1).getTimeStamp())));
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
