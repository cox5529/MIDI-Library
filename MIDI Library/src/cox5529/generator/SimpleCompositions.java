package cox5529.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import cox5529.generator.storage.Measure;
import cox5529.generator.storage.Note;
import cox5529.midi.Helper;
import cox5529.midi.MIDIFile;
import cox5529.midi.event.MIDIEvent;
import cox5529.midi.event.Tempo;
import cox5529.midi.event.TimeSignature;
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
			output.addTrack(newTrack);
		}
		
		return output;
	}
	
	/**
	 * Creates a new MIDIFile object by combining Mozart's musical dice game with an intelligent pitch scrambling and support track-generating algorithm.
	 * 
	 * @param input the given file to randomize
	 * @param duration the duration of the song to be generated in measures
	 * @param depth the depth to scan when generating a pitch
	 * @return the generated object
	 */
	public static MIDIFile fullCompose(MIDIFile input, int duration, int depth) {
		boolean isMajor = true;
		int sharps = 0;
		int tempo = 120;
		ArrayList<MusicTrack> tracks = input.getTracks();
		byte[] instruments = new byte[tracks.size()];
		MIDIFile output = new MIDIFile();
		output.setResolution(input.getResolution());
		int res = output.getResolution();
		ArrayList<MIDIEvent> events = tracks.get(0).getEvents();
		ArrayList<Measure> measures = new ArrayList<Measure>();
		ArrayList<Note> notes = new ArrayList<Note>();
		ArrayList<MIDIEvent> cur = new ArrayList<MIDIEvent>();
		int processed = 0;
		long measureStart = 0;
		long maxDur = input.getResolution() * 4 - 1;
		long curStart = -1;
		long[] noteSum = new long[tracks.size()];
		long[] noteTotal = new long[tracks.size()];
		long[] volTotal = new long[tracks.size()];
		for(int j = 0; j < events.size(); j++) {
			MIDIEvent event = events.get(j);
			if(event.getStatus() == (byte) 0x90 && event.getData()[1] != 0) {
				if(event.getTimeStamp() > measureStart + res * 4) {
					ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
					for(int k = 0; k < cur.size(); k++) {
						toAdd.add(cur.get(k));
					}
					measures.add(new Measure(toAdd, false));
					measureStart += maxDur + 1;
					j--;
					continue;
				}
				if(processed >= depth && j != events.size() - 3) {
					// Pitch stuff
					noteSum[0] += event.getData()[0];
					noteTotal[0]++;
					volTotal[0] += event.getData()[1];
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
				if(dur >= maxDur || maxDur - dur < 1.0 / 32 * res) {
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
			} else if(event.getStatus() == (byte) 0xFF && event.getData()[0] == 0x59) {
				sharps = event.getData()[2];
				if(event.getData()[3] == 1)
					isMajor = false;
			} else if(event.getStatus() == (byte) 0xFF && event.getData()[0] == 0x51) {
				byte[] data = event.getData();
				tempo = (int) (0.00006 * new BigInteger(new byte[] { data[2], data[3], data[4] }).intValue());
			} else if(event.getStatus() == (byte) 0xC0) {
				instruments[0] = event.getData()[0];
			}
		}
		ArrayList<MIDIEvent> toAdd1 = new ArrayList<MIDIEvent>();
		for(int k = 0; k < cur.size(); k++) {
			toAdd1.add(cur.get(k));
		}
		measures.add(new Measure(toAdd1, false));
		
		for(int i = 1; i < tracks.size(); i++) {
			cur.clear();
			measureStart = 0;
			ArrayList<MIDIEvent> supportEvents = tracks.get(i).getEvents();
			curStart = -1;
			int mCount = 0;
			for(int j = 0; j < supportEvents.size(); j++) {
				MIDIEvent event = supportEvents.get(j);
				int status = Byte.toUnsignedInt(event.getStatus());
				if(status / 16 == 0x9 && event.getData()[1] != 0) {
					if(event.getTimeStamp() > measureStart + res * 4 - 1) {
						toAdd1.clear();
						for(int k = 0; k < cur.size(); k++) {
							toAdd1.add(cur.get(k));
						}
						measures.get(mCount).addSupport(toAdd1);
						mCount++;
						measureStart += maxDur + 1;
						j--;
						continue;
					}
					noteSum[i] += event.getData()[0];
					volTotal[i] += event.getData()[1];
					noteTotal[i]++;
					curStart = event.getTimeStamp();
					event.setTimeStamp(curStart - measureStart);
					cur.add(event);
				} else if(curStart != -1 && (status / 16 == 0x9 && event.getData()[1] == 0) || event.getStatus() == (byte) 0x80) {
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
					if(dur >= maxDur || maxDur - dur < 1.0 / 32 * res) {
						boolean tie = false;
						if(dur > maxDur) {
							cur.remove(cur.size() - 1);
							cur.add(new MIDIEvent(maxDur, (byte) (0x9 * 16 + j + 1), new byte[] { supportEvents.get(j - 1).getData()[0], (byte) 0x00 }));
							tie = true;
						}
						ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
						for(int k = 0; k < cur.size(); k++) {
							toAdd.add(cur.get(k));
						}
						measures.get(mCount).addSupport(toAdd);
						mCount++;
						cur.clear();
						if(tie) {
							cur.add(new MIDIEvent(0, (byte) (0x9 * 16 + j + 1), supportEvents.get(j - 1).getData()));
							cur.add(new MIDIEvent(dur - maxDur, (byte) (0x9 * 16 + j + 1), event.getData()));
						}
						measureStart += maxDur + 1;
					}
				} else if(status / 16 == 0xC) {
					instruments[i] = event.getData()[0];
				}
			}
			toAdd1.clear();
			for(int k = 0; k < cur.size(); k++) {
				toAdd1.add(cur.get(k));
			}
			measures.get(mCount).addSupport(toAdd1);
			mCount = 0;
		}
		Collections.sort(notes);
		
		long[] noteAverage = new long[noteSum.length];
		long[] volAverage = new long[volTotal.length];
		for(int i = 0; i < noteAverage.length; i++) {
			noteAverage[i] = noteSum[i] / noteTotal[i];
			noteAverage[i] -= noteAverage[i] % 12;
			volAverage[i] = volTotal[i] / noteTotal[i];
		}
		
		MusicTrack newTrack = new MusicTrack();
		newTrack.addEvent(TimeSignature.construct(0, (byte) 4, (byte) 4));
		Tempo t = Tempo.construct(0, tempo);
		newTrack.addEvent(t);
		newTrack.changeInstrument(0, 0, instruments[0]);
		MusicTrack[] supportTracks = new MusicTrack[tracks.size() - 1];
		for(int i = 0; i < supportTracks.length; i++) {
			supportTracks[i] = new MusicTrack();
			supportTracks[i].changeInstrument(0, i + 1, instruments[i + 1]);
		}
		long pos = 0;
		byte[] prev = null;
		byte[] prevPitch = notes.get((int) (Math.random() * notes.size())).getPrecede();
		Measure last = measures.get((int) (Math.random() * measures.size()));
		for(int j = 0; j < duration; j++) {
			ArrayList<MIDIEvent> newEvents = new ArrayList<MIDIEvent>();
			Measure toAdd = null;
			while(newEvents.size() == 0) {
				Measure[] set = new Measure[3];
				for(int i = 0; i < set.length; i++) {
					set[i] = measures.get((int) (Math.random() * measures.size()));
				}
				int closest = 0;
				for(int i = 1; i < set.length; i++) {
					if(Math.abs(set[closest].getPartialAverageSpeed(res * 2, false) - last.getPartialAverageSpeed(res * 2, true)) > Math.abs(set[i].getPartialAverageSpeed(res * 2, false) - last.getPartialAverageSpeed(res * 2, true))) {
						closest = i;
					}
				}
				toAdd = set[closest];
				last = toAdd;
				newEvents = toAdd.getEvents();
			}
			pos = input.getResolution() * 4 * j;
			if(j > 0 && prev != null) {
				newEvents.remove(0);
				MIDIEvent rep = newEvents.remove(0);
				rep.setData(prev);
				newEvents.add(0, rep);
			}
			ArrayList<ArrayList<MIDIEvent>> supports = toAdd.getSupport();
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
							data[1] = (byte) volAverage[0];
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
					byte[] chord = Helper.getChord(sharps, isMajor, event.getData()[0]);
					if(chord.length > 0) {
						// Support stuff
						for(int i = 0; i < supports.size(); i++) {
							ArrayList<MIDIEvent> m = supports.get(i);
							while(m.size() > 0) {
								if(event.getTimeStamp() >= m.get(0).getTimeStamp() - 1) {
									MIDIEvent e1 = m.remove(0).clone();
									MIDIEvent e2 = m.remove(0).clone();
									e1.setStatus((byte) (0x9 * 16 + i + 1));
									e2.setStatus((byte) (0x9 * 16 + i + 1));
									e1.setTimeStamp(pos + e1.getTimeStamp());
									e2.setTimeStamp(pos + e2.getTimeStamp());
									byte[] data = new byte[2];
									data[1] = (byte) volAverage[i + 1];
									if(i % 3 == 0) {
										data[0] = (byte) (chord[2] % 12 + noteAverage[i + 1]);
									} else if(i % 3 == 1) {
										data[0] = (byte) (chord[1] % 12 + noteAverage[i + 1]);
									} else if(i % 3 == 2) {
										data[0] = (byte) (chord[0] % 12 + noteAverage[i + 1]);
									}
									e1.setData(data);
									e2.setData(new byte[] { data[0], 0 });
									supportTracks[i].addEvent(e1);
									supportTracks[i].addEvent(e2);
								} else
									break;
							}
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
			byte[] chord = Helper.getChord(sharps, isMajor, newEvents.get(newEvents.size() - 2).getData()[0]);
			if(chord.length > 0) {
				for(int i = 0; i < supports.size(); i++) {
					ArrayList<MIDIEvent> m = supports.get(i);
					while(m.size() > 0) {
						MIDIEvent e1 = m.remove(0).clone();
						MIDIEvent e2 = m.remove(0).clone();
						e1.setStatus((byte) (0x9 * 16 + i + 1));
						e2.setStatus((byte) (0x9 * 16 + i + 1));
						e1.setTimeStamp(pos + e1.getTimeStamp());
						e2.setTimeStamp(pos + e2.getTimeStamp());
						byte[] data = new byte[2];
						data[1] = 0x3F;
						if(i % 3 == 0) { // top
							data[0] = (byte) (chord[2] % 12 + noteAverage[i + 1]);
						} else if(i % 3 == 1) {
							data[0] = (byte) (chord[1] % 12 + noteAverage[i + 1]);
						} else if(i % 3 == 2) { // bottom
							data[0] = (byte) (chord[0] % 12 + noteAverage[i + 1]);
						}
						e1.setData(data);
						e2.setData(new byte[] { data[0], 0 });
						supportTracks[i].addEvent(e1);
						supportTracks[i].addEvent(e2);
					}
				}
			}
		}
		output.addTrack(newTrack);
		for(int i = 0; i < supportTracks.length; i++)
			output.addTrack(supportTracks[i]);
			
		return output;
	}
}
