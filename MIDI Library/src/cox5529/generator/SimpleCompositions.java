package cox5529.generator;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import cox5529.generator.storage.Measure;
import cox5529.generator.storage.Note;
import cox5529.generator.storage.Phrase;
import cox5529.generator.storage.PhraseTree;
import cox5529.generator.storage.Pitch;
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
	
	private int depth;
	private byte[] instruments = null;
	private boolean isMajor = true;
	private ArrayList<Measure> measures = null;
	private long[] noteAverage = null;
	private ArrayList<Note> notes;
	private long[] noteSum = null;
	private long[] noteTotal = null;
	private ArrayList<Phrase> phrases;
	private ArrayList<Pitch> pitches;
	private int res = -1;
	private int sharps = 0;
	private int tempo = 120;
	private long[] volAverage = null;
	
	private long[] volTotal = null;
	
	/**
	 * Constructs a new object that can be used to generate music.
	 * 
	 * @param input the given files to randomize
	 * @param depth the depth to scan when generating a pitch
	 */
	public SimpleCompositions(int depth, MIDIFile... input) {
		this.depth = depth;
		isMajor = true;
		sharps = 0;
		tempo = 120;
		pitches = new ArrayList<Pitch>();
		notes = new ArrayList<Note>();
		phrases = new ArrayList<Phrase>();
		measures = new ArrayList<Measure>();
		noteSum = null;
		noteTotal = null;
		volTotal = null;
		instruments = null;
		res = -1;
		int prevMeasureSize = 0;
		long length = 0;
		for(int b = 0; b < input.length; b++) {
			ArrayList<Measure> measures = new ArrayList<Measure>();
			ArrayList<MusicTrack> tracks = input[b].getTracks();
			if(noteSum == null) {
				noteSum = new long[tracks.size()];
				noteTotal = new long[tracks.size()];
				volTotal = new long[tracks.size()];
				instruments = new byte[tracks.size()];
			}
			if(res == -1) {
				res = input[b].getResolution();
			}
			ArrayList<MIDIEvent> events = tracks.get(0).getEvents();
			ArrayList<MIDIEvent> cur = new ArrayList<MIDIEvent>();
			long measureStart = 0;
			long maxDur = res * 4 - 1;
			long curStart = -1;
			for(int j = 0; j < events.size(); j++) {
				MIDIEvent event = events.get(j);
				if(event.getStatus() == (byte) 0x90 && event.getData()[1] != 0) {
					if(event.getTimeStamp() > measureStart + res * 4) {
						ArrayList<MIDIEvent> toAdd = new ArrayList<MIDIEvent>();
						for(int k = 0; k < cur.size(); k++) {
							toAdd.add(cur.get(k));
						}
						measures.add(new Measure(toAdd, res));
						measureStart += maxDur + 1;
						j--;
						continue;
					}
					if(j != events.size() - 3) {
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
						Pitch n = new Pitch(key, nextPitch);
						boolean cont = false;
						for(int k = 0; k < pitches.size(); k++) {
							if(n.equals(pitches.get(k))) {
								pitches.get(k).addPitch(nextPitch);
								cont = true;
								break;
							}
						}
						if(!cont) {
							pitches.add(n);
						}
					}
					// Duration stuff
					curStart = event.getTimeStamp();
					event.setTimeStamp(curStart - measureStart);
					cur.add(event);
				} else if(curStart != -1 && (event.getStatus() == (byte) 0x90 && event.getData()[1] == 0) || event.getStatus() == (byte) 0x80) {
					long noteDur = event.getTimeStamp() - curStart;
					if(noteDur == 455 || noteDur == 479)
						noteDur = 479;
					else if(noteDur == 227 || noteDur == 239)
						noteDur = 239;
					else if(noteDur == 113 || noteDur == 119)
						noteDur = 119;
					Note n = new Note(curStart, event.getTimeStamp(), event.getData()[0]);
					notes.add(n);
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
						measures.add(new Measure(toAdd, res));
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
					tempo = (int) (0.00012 * new BigInteger(new byte[] { data[2], data[3], data[4] }).intValue());
				} else if(event.getStatus() == (byte) 0xC0) {
					instruments[0] = event.getData()[0];
				}
			}
			length = events.get(events.size() - 1).getTimeStamp();
			ArrayList<MIDIEvent> toAdd1 = new ArrayList<MIDIEvent>();
			for(int k = 0; k < cur.size(); k++) {
				toAdd1.add(cur.get(k));
			}
			measures.add(new Measure(toAdd1, res));
			
			for(int i = 1; i < tracks.size(); i++) {
				cur.clear();
				measureStart = 0;
				ArrayList<MIDIEvent> supportEvents = tracks.get(i).getEvents();
				curStart = -1;
				int mCount = prevMeasureSize;
				for(int j = 0; j < supportEvents.size(); j++) {
					MIDIEvent event = supportEvents.get(j);
					int status = Byte.toUnsignedInt(event.getStatus());
					if(status / 16 == 0x9 && event.getData()[1] != 0) {
						if(event.getTimeStamp() > measureStart + res * 4 - 1) {
							toAdd1.clear();
							for(int k = 0; k < cur.size(); k++) {
								toAdd1.add(cur.get(k));
							}
							if(mCount < measures.size())
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
							if(mCount < measures.size())
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
				if(mCount < measures.size())
					measures.get(mCount).addSupport(toAdd1);
				long trackLength = supportEvents.get(supportEvents.size() - 1).getTimeStamp();
				while(trackLength < length) {
					mCount++;
					measures.get(mCount).addSupport(new ArrayList<MIDIEvent>());
					trackLength += res * 4;
				}
				mCount = prevMeasureSize;
				
			}
			prevMeasureSize = measures.size();
			this.measures.addAll(measures);
			
			/*
			 * ArrayList<byte[]> chords = new ArrayList<byte[]>(); for(int i = 0; i < measures.size(); i++) { byte[][] c = measures.get(i).getChords(sharps, isMajor); for(int j = 0; j < c.length; j++) { chords.add(c[j]); } } chordList.add(chords);
			 */
			// Phrase location
			int l = notes.size() - 1;
			for(int i = 0; i < l; i++) {
				long stop = notes.get(i).getStop();
				long start = notes.get(i + 1).getStart();
				if(start - stop > 1)
					notes.add(new Note(stop + 1, start - 1, (byte) -127));
			}
			Collections.sort(notes);
			
			phrases = findPhrasesMeasure();
			
		}
		Collections.sort(pitches);
		
		noteAverage = new long[noteSum.length];
		volAverage = new long[volTotal.length];
		for(int i = 0; i < noteAverage.length; i++) {
			noteAverage[i] = noteSum[i] / noteTotal[i];
			noteAverage[i] -= noteAverage[i] % 12;
			volAverage[i] = volTotal[i] / noteTotal[i];
		}
		
		for(int i = 0; i < measures.size(); i++) {
			Measure m = measures.get(i);
			ArrayList<MIDIEvent> events = m.getEvents();
			ArrayList<ArrayList<MIDIEvent>> sup = m.getSupport();
			SimpleCompositions.write(new File("Test\\" + i + ".mid"), events, sup, res, tempo, instruments);
		}
	}
	
	private ArrayList<Phrase> combinePhrases(ArrayList<Phrase> phrases, int curId) {
		for(int i = 0; i < phrases.size() - 1; i++) {
			int[] window = new int[2];
			window[0] = phrases.get(i).getId();
			window[1] = phrases.get(i + 1).getId();
			boolean match = false;
			int deriv = 1;
			for(int j = i + 2; j < phrases.size() - 1; j++) {
				int[] ids = { phrases.get(j).getId(), phrases.get(j + 1).getId() };
				if(ids[0] / 100 == window[0] / 100 && ids[1] / 100 == window[1] / 100 && phrases.get(j).supportMatch(phrases.get(j + 1))) {
					if(!match) {
						PhraseTree pt = PhraseTree.construct(phrases.remove(i), phrases.remove(i), curId);
						phrases.add(i, pt);
					}
					int dif = (match ? 0: 1);
					if(ids[0] == window[0] && ids[1] == window[1]) {
						phrases.add(j - dif, PhraseTree.construct(phrases.remove(j - dif), phrases.remove(j - dif), curId));
					} else {
						PhraseTree pt = PhraseTree.construct(phrases.remove(j - dif), phrases.remove(j - dif), curId + deriv);
						boolean added = false;
						for(int k = 0; k < phrases.size(); k++) {
							Phrase p = phrases.get(k);
							if(pt.getStringId().equals(p.getStringId())) {
								pt.setId(p.getId());
								added = true;
								phrases.add(j - dif, pt);
								break;
							}
						}
						if(!added) {
							phrases.add(j - dif, pt);
							deriv++;
						}
					}
					if(!match)
						j--;
					match = true;
					i--;
				}
			}
			if(match) {
				curId += 100;
				return combinePhrases(phrases, curId);
			}
		}
		return phrases;
	}
	
	private ArrayList<Phrase> findPhrasesMeasure() {
		ArrayList<Phrase> phrases = new ArrayList<Phrase>();
		int curId = 100;
		for(int i = 0; i < measures.size(); i++) {
			Measure m = measures.get(i);
			if(m.getNotes().size() > 1) {
				ArrayList<Note> notes = addRests(m.getNotes());
				ArrayList<ArrayList<Note>> sup = m.getSupportNotes();
				for(int j = 0; j < sup.size(); j++) {
					ArrayList<Note> sups = sup.remove(j);
					sups = addRests(sups);
					sup.add(j, sups);
				}
				int id = m.getPhrase(phrases);
				if(id == -1) {
					phrases.add(new Phrase(notes, sup, curId));
					curId += 100;
				} else {
					phrases.add(new Phrase(notes, sup, id));
				}
				phrases.get(phrases.size() - 1).generateChords(sharps, isMajor);
			}
		}
		
		phrases = combinePhrases(phrases, curId);
		int maxId = 0;
		for(int i = 0; i < phrases.size(); i++) {
			int id = phrases.get(i).getId();
			if(id > maxId)
				maxId = id;
		}
		phrases = simplifyPhrases(phrases, 100 * (maxId / 100) + 100);
		return phrases;
	}
	
	private ArrayList<Note> addRests(ArrayList<Note> notes) {
		long prevEnd = 0;
		for(int i = 0; i < notes.size(); i++) {
			Note n = notes.get(i);
			long start = n.getStart();
			if(start - prevEnd > 1) {
				notes.add(i, new Note(prevEnd + 1, start - 1, (byte) -1));
			}
			prevEnd = n.getStop();
		}
		return notes;
	}
	
	/**
	 * Generates a new song
	 * 
	 * @param measureDepth scanning depth for rhythm generation
	 * 		
	 * @return the MIDIFile object representing the generated song
	 */
	public MIDIFile generateSong(int measureDepth) {
		for(int i = 0; i < measures.size(); i++) {
			if(measures.get(i).getNotes().size() == 0) {
				measures.remove(i);
				i--;
			}
		}
		MIDIFile output = new MIDIFile();
		output.setResolution(res);
		MusicTrack[] tracks = new MusicTrack[1 + phrases.get(0).getSupports().size()];
		for(int i = 0; i < tracks.length; i++) {
			tracks[i] = new MusicTrack();
		}
		tracks[0].addEvent(TimeSignature.construct(0, (byte) 4, (byte) 4));
		Tempo t = Tempo.construct(0, tempo);
		tracks[0].addEvent(t);
		for(int i = 0; i < tracks.length; i++) {
			tracks[i].changeInstrument(0, 0, instruments[i]);
		}
		
		TreeSet<Integer> ids = new TreeSet<Integer>();
		for(int i = 0; i < phrases.size(); i++) {
			Phrase p = phrases.get(i);
			if(p instanceof PhraseTree) {
				int[] toAdd = ((PhraseTree) p).getChildIds();
				for(int j = 0; j < toAdd.length; j++) {
					ids.add(toAdd[j]);
				}
			} else
				ids.add(p.getId());
		}
		@SuppressWarnings("unchecked") TreeSet<Integer> idsDeriv = (TreeSet<Integer>) ids.clone();
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()) {
			int val = it.next();
			if(val % 100 != 0) {
				ids.remove(val);
				it = ids.iterator();
			}
		}
		
		Iterator<Integer> itDeriv = idsDeriv.iterator();
		while(itDeriv.hasNext()) {
			int val = itDeriv.next();
			if(val % 100 == 0) {
				idsDeriv.remove(val);
				itDeriv = idsDeriv.iterator();
			}
		}
		long pos = 0;
		int idx = 0;
		HashSet<Phrase> measures = new HashSet<Phrase>();
		for(int i = 0; i < phrases.size(); i++) {
			Phrase p = phrases.get(i);
			System.out.println(p.toString(res) + "\n"); // TODO print
			if(p instanceof PhraseTree && p.getId() % 100 == 0)
				measures.addAll(((PhraseTree) p).getChildren());
			else if(p.getId() % 100 == 0)
				measures.add(p);
		}
		ArrayList<Double> avgDur = new ArrayList<Double>();
		Iterator<Phrase> mIt = new TreeSet<Phrase>(measures).iterator();
		while(mIt.hasNext()) {
			Phrase p = mIt.next();
			avgDur.add(p.getAverageDuration());
		}
		Phrase[] phrases = new Phrase[ids.size()];
		it = ids.iterator();
		while(it.hasNext()) {
			int val = it.next();
			Phrase[] m = new Phrase[measures.size()];
			measures.toArray(m);
			Phrase p = null;
			Phrase[] r = new Phrase[measureDepth];
			for(int i = 0; i < r.length; i++) {
				r[i] = m[(int) (Math.random() * m.length)];
			}
			double avg = avgDur.get(idx);
			for(int i = 0; i < r.length; i++) {
				if(p == null)
					p = r[i];
				else if(Math.abs(p.getAverageDuration() - avg) > Math.abs(r[i].getAverageDuration() - avg))
					p = r[i];
			}
			measures.remove(p);
			ArrayList<Note> notes = p.getNotes();
			int len = 0;
			for(int k = 0; k < notes.size(); k++) {
				if(notes.get(k).getPitch() != -1)
					len++;
			}
			ArrayList<Byte> pitches = getPitches(null, len);
			len = 0;
			for(int k = 0; k < notes.size(); k++) {
				Note n = notes.get(k);
				if(n.getPitch() != -1) {
					n.setPitch(pitches.get(len));
					
					len++;
				}
				long dur = n.getDuration();
				n.setStart(pos);
				n.setStop(pos + dur);
				pos += dur + 1;
			}
			phrases[idx++] = new Phrase(notes, val, p.getChords());
		}
		
		Phrase[] dPhrases = new Phrase[idsDeriv.size()];
		itDeriv = idsDeriv.iterator();
		idx = 0;
		while(itDeriv.hasNext()) {
			int val = itDeriv.next();
			Phrase p = phrases[val / 100 - 1];
			dPhrases[idx] = p.mutate(sharps, isMajor, val);
			idx++;
		}
		HashMap<Integer, Phrase> phraseSet = new HashMap<Integer, Phrase>();
		for(int i = 0; i < dPhrases.length; i++) {
			phraseSet.put(dPhrases[i].getId(), dPhrases[i]);
		}
		for(int i = 0; i < phrases.length; i++) {
			phraseSet.put(phrases[i].getId(), phrases[i]);
		}
		
		ArrayList<Phrase> actual = this.phrases;
		ArrayList<Phrase> song = new ArrayList<Phrase>();
		for(int i = 0; i < actual.size(); i++) {
			Phrase p = actual.get(i);
			int[] childs = p.getChildIds();
			Phrase[] create = new Phrase[childs.length];
			for(int j = 0; j < childs.length; j++) {
				create[j] = phraseSet.get(childs[j]);
			}
			Phrase toAdd;
			if(childs.length >= 2) {
				toAdd = PhraseTree.construct(create[0], create[1], p.getId());
				for(int j = 2; j < create.length; j++) {
					toAdd = PhraseTree.construct(toAdd, create[j], p.getId());
				}
			} else if(create.length == 1)
				toAdd = create[0];
			else
				continue;
			song.add(toAdd);
		}
		
		long ipos = 0;
		for(int i = 0; i < song.size(); i++) {
			pos = ipos;
			Phrase p = song.get(i);
			p.generateSupports(tracks.length - 1, noteAverage, sharps, isMajor);
			ArrayList<Note> mel = p.getNotes();
			for(int j = 0; j < mel.size(); j++) {
				Note n = mel.get(j).clone();
				long dur = n.getDuration();
				n.setStart(pos);
				n.setStop(pos + dur);
				tracks[0].addNote(n, (byte) volAverage[0]);
				pos += 1 + dur;
			}
			int m = (int) ((pos + 1) / (4 * res));
			ArrayList<ArrayList<Note>> supports = p.getSupports();
			for(int j = 0; j < supports.size(); j++) {
				pos = ipos;
				ArrayList<Note> sup = supports.get(j);
				for(int k = 0; k < sup.size(); k++) {
					Note n = sup.get(k).clone();
					long dur = n.getDuration();
					n.setStart(pos);
					n.setStop(pos + dur);
					tracks[j + 1].addNote(n, (byte) volAverage[j + 1]);
					pos += 1 + dur;
				}
			}
			ipos = m * res * 4;
		}
		for(int i = 0; i < tracks.length; i++) {
			output.addTrack(tracks[i]);
		}
		return output;
		
	}
	
	private ArrayList<Byte> getPitches(ArrayList<Byte> re, int length) {
		if(re == null || re.size() < depth) {
			Pitch start = pitches.get((int) (Math.random() * pitches.size()));
			re = new ArrayList<Byte>();
			byte[] pre = start.getPrecede();
			for(int i = 0; i < pre.length; i++) {
				if(i < length)
					re.add(pre[i]);
			}
		}
		if(re.size() == length)
			return re;
		for(int i = 0; i < pitches.size(); i++) {
			byte[] pre = pitches.get(i).getPrecede();
			boolean found = true;
			for(int j = 0; j < depth; j++) {
				if(pre[j] != re.get(re.size() - j - 1)) {
					found = false;
					break;
				}
			}
			if(found) {
				int s = re.size();
				ArrayList<Byte> ignore = new ArrayList<Byte>();
				do {
					byte pitch = pitches.get(i).getFollowPitch(ignore);
					if(pitch == -1)
						return re;
					re.add(pitch);
					re = getPitches(re, length);
					if(s == re.size())
						ignore.add(pitch);
					else
						return re;
				} while(s == re.size());
				break;
			}
		}
		re.remove(re.size() - 1);
		return getPitches(re, length);
	}
	
	private ArrayList<Phrase> simplifyPhrases(ArrayList<Phrase> phrases, int curId) {
		// combine non repeated phrases
		for(int size = phrases.size() - 1; size > 1; size--) {
			for(int i = 0; i < phrases.size() - size; i++) {
				boolean good = true;
				double avgDur = phrases.get(i).getAverageDuration();
				for(int j = i; j < i + size; j++) {
					Phrase p = phrases.get(j);
					if(p instanceof PhraseTree || p.getAverageDuration() > 1.25 * avgDur || p.getAverageDuration() < 0.75 * avgDur) {
						good = false;
						break;
					}
				}
				if(good) {
					PhraseTree pt = PhraseTree.construct(phrases.remove(i), phrases.remove(i), curId);
					phrases.add(i, pt);
					for(int j = 2; j < size; j++) {
						pt = (PhraseTree) phrases.remove(i);
						pt = PhraseTree.construct(pt, phrases.remove(i), curId);
						phrases.add(i, pt);
					}
					curId += 100;
				}
			}
		}
		return phrases;
	}
	
	public String toString() {
		String re = "";
		re += ("Major:\t" + isMajor);
		re += ("\nSharps:\t" + sharps);
		re += ("\nTempo:\t" + tempo);
		re += ("\nPitches:\n");
		for(int i = 0; i < pitches.size(); i++) {
			re += "\n" + pitches.get(i);
		}
		return re;
		
	}
	
	/**
	 * Debug code for writing a MIDIFile
	 * 
	 * @param file the file to write to
	 * @param events the events of the first track
	 * @param support the events of the support tracks
	 * @param res the resolution of the file
	 * @param tempo the tempo of the file
	 * @param instruments the instruments of the song
	 */
	public static void write(File file, ArrayList<MIDIEvent> events, ArrayList<ArrayList<MIDIEvent>> support, int res, int tempo, byte[] instruments) {
		MIDIFile output = new MIDIFile();
		output.setResolution(res);
		MusicTrack[] tracks = new MusicTrack[1 + support.size()];
		for(int i = 0; i < tracks.length; i++) {
			tracks[i] = new MusicTrack();
		}
		tracks[0].addEvent(TimeSignature.construct(0, (byte) 4, (byte) 4));
		Tempo t = Tempo.construct(0, tempo);
		tracks[0].addEvent(t);
		for(int i = 0; i < tracks.length; i++) {
			tracks[i].changeInstrument(0, i, instruments[i]);
		}
		
		if(events.size() > 0) {
			long offset = events.get(0).getTimeStamp();
			for(int i = 0; i < events.size(); i++) {
				MIDIEvent event = events.get(i).clone();
				event.setTimeStamp(event.getTimeStamp() - offset);
				tracks[0].addEvent(event);
			}
			output.addTrack(tracks[0]);
			for(int i = 1; i < tracks.length; i++) {
				events = support.get(i - 1);
				for(int j = 0; j < events.size(); j++) {
					MIDIEvent event = events.get(j).clone();
					event.setTimeStamp(event.getTimeStamp() - offset);
					tracks[0].addEvent(event);
				}
				output.addTrack(tracks[i]);
			}
			try {
				output.write(file, false);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}