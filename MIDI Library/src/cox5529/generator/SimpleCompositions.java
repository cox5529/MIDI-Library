package cox5529.generator;

import java.util.ArrayList;

import cox5529.generator.storage.Measure;
import cox5529.midi.MIDIFile;
import cox5529.midi.event.MIDIEvent;
import cox5529.midi.track.MusicTrack;

/**
 * Class that contains basic methods of algorithmic composition.
 * 
 * @author Brandon Cox
 * 		
 */
public class SimpleCompositions {
	
	/**
	 * Creates a new MIDIFile object using Mozart's musical dice game's algorithm.
	 * 
	 * @param input the given file to randomize
	 * @param duration the duration of the song to be generated in measures
	 * @return the generated file
	 */
	public static MIDIFile diceGame(MIDIFile input, int duration) {
		ArrayList<MusicTrack> tracks = input.getTracks();
		MIDIFile output = new MIDIFile();
		output.setResolution(input.getResolution());
		for(int i = 0; i < tracks.size(); i++) {
			ArrayList<MIDIEvent> events = tracks.get(i).getEvents();
			ArrayList<Measure> measures = new ArrayList<Measure>();
			ArrayList<MIDIEvent> cur = new ArrayList<MIDIEvent>();
			long measureStart = 0;
			long maxDur = input.getResolution() * 4 - 1;
			long curStart = -1;
			for(int j = 0; j < events.size(); j++) {
				MIDIEvent event = events.get(j);
				if(event.getStatus() == (byte) 0x90 && event.getData()[1] != 0) {
					curStart = event.getTimeStamp();
					event.setTimeStamp(curStart - measureStart);
					cur.add(event);
				} else if(curStart != -1 && (event.getStatus() == (byte) 0x90 && event.getData()[1] == 0) || event.getStatus() == (byte) 0x80) {
					long dur = event.getTimeStamp() - measureStart;
					event.setTimeStamp(event.getTimeStamp() - measureStart);
					curStart = -1;
					cur.add(event);
					if(dur >= maxDur) {
						boolean tie = false;
						if(dur > maxDur) {
							cur.remove(cur.size() - 1);
							cur.add(new MIDIEvent(maxDur, (byte) 0x90, new byte[] { events.get(j - 1).getData()[0], (byte) 0x00 }));
							tie = true;
						}
						ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
						for(int k = 0; k < cur.size(); k++) {
							toAdd.add(cur.get(k));
						}
						measures.add(new Measure(toAdd, tie));
						cur.clear();
						if(tie) {
							cur.add(new MIDIEvent(0, (byte) 0x90, events.get(j - 1).getData()));
							cur.add(new MIDIEvent(dur - maxDur, (byte) 0x90, event.getData()));
						}
						measureStart += maxDur + 1;
					}
				}
			}
			measures.add(new Measure(cur, false));
			MusicTrack newTrack = new MusicTrack();
			long pos = 0;
			byte[] prev = null;
			for(int j = 0; j < duration; j++) {
				Measure toAdd = measures.get((int) (Math.random() * measures.size()));
				ArrayList<MIDIEvent> newEvents = toAdd.getEvents();
				pos = input.getResolution() * 4 * j;
				if(j > 0 && prev != null) {
					newEvents.remove(0);
					MIDIEvent rep = newEvents.remove(0);
					rep.setData(prev);
					newEvents.add(0, rep);
				}
				for(int k = 0; k < newEvents.size(); k++) {
					if(!(toAdd.isTie() && k == newEvents.size() - 1) || j == duration - 1) {
						MIDIEvent event = newEvents.get(k).clone();
						event.setTimeStamp(pos + newEvents.get(k).getTimeStamp());
						newTrack.addEvent(event);
						prev = null;
					} else {
						prev = newEvents.get(k).getData();
					}
				}
			}
			output.addTrack(newTrack);
		}
		
		return output;
	}
	
}
