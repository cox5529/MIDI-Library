package cox5529.midi.track;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import cox5529.midi.event.MetaEvent;

/**
 * Class used to store the Meta track of a MIDI file.
 * 
 * @author Brandon Cox
 * 		
 */
public class MetaTrack {
	
	private ArrayList<MetaEvent> events;
	
	/**
	 * Constructs a new MetaTrack.
	 */
	public MetaTrack() {
		events = new ArrayList<MetaEvent>();
		events.add(MetaEvent.construct(0, (byte) 0x00, new byte[] { (byte) 0x00, (byte) 0x00 }));
	}
	
	/**
	 * Changes the tempo to the give tempo.
	 * 
	 * @param tempo the tempo to change the song to. Give in beats per minute.
	 * @param timestamp the time at which the tempo should be changed
	 */
	public void changeTempo(int tempo, long timestamp) {
		tempo = (6 * (int) Math.pow(10, 7)) / tempo;
		byte[] b = ByteBuffer.allocate(4).putInt(tempo).array();
		events.add(MetaEvent.construct(timestamp, (byte) 0x51, new byte[] { b[1], b[2], b[3] }));
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
			if(debug)
				System.out.println("Created meta event with data: " + events.get(i).toString((i == 0 ? 0: events.get(i - 1).getTimeStamp())) + ".");
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
