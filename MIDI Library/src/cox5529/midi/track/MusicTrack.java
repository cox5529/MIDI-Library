package cox5529.midi.track;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import cox5529.midi.Helper;
import cox5529.midi.event.MIDIEvent;
import cox5529.midi.event.MetaEvent;

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
	 * Constructs a new MusicTrack
	 * 
	 * @param events the ArrayList of events that this MusicTrack will use
	 */
	public MusicTrack(ArrayList<MIDIEvent> events) {
		this.events = events;
	}
	
	/**
	 * Adds a MIDIEvent to the track.
	 * 
	 * @param event the event to add
	 */
	public void addEvent(MIDIEvent event) {
		events.add(event);
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
	 * Gets the ArrayList of MIDIEvents that make up this MusicTrack.
	 * 
	 * @return the ArrayList of MIDIEvents
	 */
	public ArrayList<MIDIEvent> getEvents() {
		ArrayList<MIDIEvent> re = new ArrayList<MIDIEvent>();
		for(int i = 0; i < events.size(); i++) {
			re.add(events.get(i).clone());
		}
		return re;
	}
	
	/**
	 * Deletes all of the current events and replaces them with the given ArrayList of MIDIEvents.
	 * 
	 * @param events the ArrayList of MIDIEvents to replace the current event list with.
	 */
	public void setEvents(ArrayList<MIDIEvent> events) {
		this.events = events;
	}
	
	/**
	 * Removes the last event in the events ArrayList.
	 * 
	 * @return the last event in the events ArrayList that was just removed.
	 */
	public MIDIEvent removeLastEvent() {
		if(events.size() > 0)
			return events.remove(events.size() - 1);
		else
			return null;
	}
	
	/**
	 * Converts this MIDITrack to its output format in order to write to a file.
	 * 
	 * @param debug true if status should be printed to the console.
	 * @return the byte[] representation of this MIDITrack
	 */
	public byte[] toOutputArray(boolean debug) {
		Collections.sort(events);
		events.add(MetaEvent.construct(events.get(events.size() - 1).getTimeStamp(), (byte) 0x2F, new byte[] {}));
		ArrayList<byte[]> out = new ArrayList<byte[]>();
		out.add(new byte[] { 0x4D, 0x54, 0x72, 0x6B });
		int trackLength = 0;
		for(int i = 0; i < events.size(); i++) {
			boolean runningStat = false;
			if(i != 0) {
				runningStat = events.get(i).getStatus() != (byte) 0xFF && events.get(i - 1).getStatus() == events.get(i).getStatus();
			}
			trackLength += events.get(i).getSize((i == 0 ? 0: events.get(i - 1).getTimeStamp()), runningStat);
		}
		out.add(ByteBuffer.allocate(4).putInt(trackLength).array());
		if(debug)
			System.out.println("Created track header.");
		for(int i = 0; i < events.size(); i++) {
			boolean runningStat = false;
			if(i != 0) {
				runningStat = events.get(i).getStatus() != (byte) 0xFF && events.get(i - 1).getStatus() == events.get(i).getStatus();
			}
			out.add(events.get(i).toByteArray((i == 0 ? 0: events.get(i - 1).getTimeStamp()), runningStat));
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
	
	/**
	 * Converts an array of bytes to a MIDI track.
	 * 
	 * @param in the array of bytes to use
	 * @param debug true if status should be printed to the console.
	 * @return a new MusicTrack
	 */
	public static MusicTrack byteArrayToTrack(byte[] in, boolean debug) {
		int length = Helper.byteArrayToInt(in[4], in[5], in[6], in[7]);
		ArrayList<MIDIEvent> events = new ArrayList<MIDIEvent>();
		for(int i = 8; i < length + 8; i += 0) {
			int timeLength = 1;
			for(int j = i; j < i + 4; j++) {
				if(Byte.toUnsignedInt(in[j]) >= 128)
					timeLength++;
				else
					break;
			}
			byte[] time = new byte[timeLength];
			for(int j = i; j < i + timeLength; j++) {
				time[j - i] = in[j];
			}
			long dtime = Helper.midiTimeToDecimal(time);
			MIDIEvent event = MIDIEvent.readFromByteArray(Arrays.copyOfRange(in, i + timeLength, in.length), dtime, (events.size() == 0 ? 0: events.get(events.size() - 1).getTimeStamp()), (events.size() == 0 ? 0x00: events.get(events.size() - 1).getStatus()));
			if(!event.toString((events.size() == 0 ? 0: events.get(events.size() - 1).getTimeStamp())).equals("00 FF 2F 00"))
				events.add(event);
			if(debug) {
				System.out.println("Read MIDI event with data: " + events.get(events.size() - 1).toString((events.size() == 1 ? 0: events.get(events.size() - 2).getTimeStamp())) + ".");
			}
			int size = events.get(events.size() - 1).getSize((events.size() == 1 ? 0: events.get(events.size() - 2).getTimeStamp()), false);
			if(in[i + timeLength] != event.getStatus())
				i--;
			i += size;
		}
		return new MusicTrack(events);
	}
	
	public String toString() {
		Collections.sort(events);
		String re = "";
		for(int i = 0; i < events.size(); i++) {
			re += events.get(i) + "\n";
		}
		return re;
	}
}
