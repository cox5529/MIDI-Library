package cox5529.generator;

import java.util.ArrayList;

import cox5529.generator.storage.Measure;
import cox5529.generator.storage.Note;
import cox5529.midi.Instruments;
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
					long noteDur = event.getTimeStamp() - curStart;
					if(noteDur == 455)
						noteDur = 479;
					else if(noteDur == 227)
						noteDur = 239;
					else if(noteDur == 113)
						noteDur = 119;
					long dur = curStart + noteDur - measureStart;
					event.setTimeStamp(curStart + noteDur - measureStart);
					curStart = -1;
					cur.add(event);
					if(dur >= maxDur || maxDur - dur < 1.0 / 32 * output.getResolution()) {
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
	
	/**
	 * Creates a new MIDIFile object by combining Mozart's musical dice game with an intelligent pitch scrambling algorithm.
	 * 
	 * @param input the given file to randomize
	 * @param duration the duration of the song to be generated in measures
	 * @param depth the depth to scan when generating a pitch
	 * @return the generated object
	 */
	public static MIDIFile diceGamePitch(MIDIFile input, int duration, int depth) {
		ArrayList<MusicTrack> tracks = input.getTracks();
		MIDIFile output = new MIDIFile();
		output.setResolution(input.getResolution());
		for(int i = 0; i < tracks.size(); i++) {
			ArrayList<MIDIEvent> events = tracks.get(i).getEvents();
			ArrayList<Measure> measures = new ArrayList<Measure>();
			ArrayList<Note> notes = new ArrayList<Note>();
			ArrayList<MIDIEvent> cur = new ArrayList<MIDIEvent>();
			int processed = 0;
			long measureStart = 0;
			long maxDur = input.getResolution() * 4 - 1;
			long curStart = -1;
			for(int j = 0; j < events.size(); j++) {
				MIDIEvent event = events.get(j);
				if(event.getStatus() == (byte) 0x90 && event.getData()[1] != 0) {
					if(processed >= depth && j != events.size() - 3) {
						// Pitch stuff
						byte[] key = new byte[depth];
						for(int k = 0; k < depth; k++) {
							key[k] = events.get(j - k).getData()[0];
						}
						byte nextPitch = 0;
						for(int k = j + 2; k < events.size(); k++) {
							if(events.get(k).getStatus() == (byte) 0x90 && event.getData()[1] != 0) {
								nextPitch = events.get(k).getData()[0];
								break;
							}
						}
						Note n = new Note(key, nextPitch);
						boolean cont = false;
						for(int k = 0; k < notes.size(); k++) {
							if(n.equals(notes.get(k))) {
								notes.get(k).addPitch(nextPitch);
								cont = true;
								break;
							}
						}
						if(!cont) {
							notes.add(n);
						}
					}
					// Duration stuff
					curStart = event.getTimeStamp();
					event.setTimeStamp(curStart - measureStart);
					cur.add(event);
				} else if(curStart != -1 && (event.getStatus() == (byte) 0x90 && event.getData()[1] == 0) || event.getStatus() == (byte) 0x80) {
					processed++;
					long noteDur = event.getTimeStamp() - curStart;
					if(noteDur == 455 || noteDur == 479)
						noteDur = 479;
					else if(noteDur == 227 || noteDur == 239)
						noteDur = 239;
					else if(noteDur == 113 || noteDur == 119)
						noteDur = 119;
					long dur = curStart + noteDur - measureStart;
					event.setTimeStamp(curStart + noteDur - measureStart);
					curStart = -1;
					cur.add(event);
					if(dur >= maxDur || maxDur - dur < 1.0 / 32 * output.getResolution()) {
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
			byte[] prevPitch = notes.get((int) (Math.random() * notes.size())).getPrecede();
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
				for(int k = 0; k < newEvents.size(); k += 2) {
					if(!(toAdd.isTie() && k == newEvents.size() - 1) || j == duration - 1) {
						MIDIEvent event = newEvents.get(k).clone();
						MIDIEvent event2 = newEvents.get(k + 1).clone();
						// Pitch stuff
						for(int m = 0; m < notes.size(); m++) {
							byte[] check = notes.get(m).getPrecede();
							boolean pass = true;
							for(int n = 0; n < depth; n++) {
								if(check[n] != prevPitch[n]) {
									pass = false;
									break;
								}
							}
							if(pass) {
								byte[] data = new byte[2];
								data[1] = 0x3F;
								data[0] = notes.get(m).getFollowPitch();
								event.setData(data);
								for(int n = depth - 1; n > 0; n--) {
									prevPitch[n] = prevPitch[n - 1];
								}
								prevPitch[0] = data[0];
								event2.setData(new byte[] { data[0], (byte) 0 });
								break;
							}
						}
						// Duration stuff
						event.setTimeStamp(pos + newEvents.get(k).getTimeStamp());
						event2.setTimeStamp(pos + newEvents.get(k + 1).getTimeStamp());
						newTrack.addEvent(event);
						newTrack.addEvent(event2);
						prev = null;
					} else {
						prev = newEvents.get(k).getData();
					}
				}
			}
			newTrack.changeInstrument(0, 0, Instruments.TRUMPET);
			output.addTrack(newTrack);
		}
		
		return output;
	}
}
