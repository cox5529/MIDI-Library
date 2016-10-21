package cox5529.generator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;

import cox5529.generator.storage.Measure;
import cox5529.generator.storage.Note;
import cox5529.generator.storage.Phrase;
import cox5529.generator.storage.Pitch;
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
	
	private boolean isMajor = true;
	private int sharps = 0;
	private int tempo = 120;
	private ArrayList<Pitch> pitches;
	private ArrayList<Note> notes;
	private ArrayList<ArrayList<byte[]>> chordList;
	private ArrayList<Phrase> phrases;
	private long[] noteSum = null;
	private long[] noteTotal = null;
	private long[] volTotal = null;
	private byte[] instruments = null;
	private long[] noteAverage = null;
	private long[] volAverage = null;
	private int res = -1;
	private int duration;
	private int depth;
	private int measureDepth;
	private int chordDepth;
	
	private ArrayList<Measure> measures = null; // TODO fix
	
	/**
	 * Constructs a new object that can be used to generate music.
	 * 
	 * @param input the given files to randomize
	 * @param duration the duration of the song to be generated in measures
	 * @param depth the depth to scan when generating a pitch
	 * @param measureDepth amount of measures to generate when looking for best next measure
	 * @param chordDepth number of chord progressions to attempt before finding best match
	 */
	public SimpleCompositions(int duration, int depth, int measureDepth, int chordDepth, MIDIFile... input) {
		this.duration = duration;
		this.depth = depth;
		this.measureDepth = measureDepth;
		this.chordDepth = chordDepth;
		isMajor = true;
		sharps = 0;
		tempo = 120;
		pitches = new ArrayList<Pitch>();
		notes = new ArrayList<Note>();
		chordList = new ArrayList<ArrayList<byte[]>>();
		phrases = new ArrayList<Phrase>();
		noteSum = null;
		noteTotal = null;
		volTotal = null;
		instruments = null;
		res = -1;
		int prevMeasureSize = 0;
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
			int processed = 0;
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
					processed++;
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
					tempo = (int) (0.00012 * new BigInteger(new byte[] { data[2], data[3], data[4] }).intValue());
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
				mCount = prevMeasureSize;
			}
			prevMeasureSize = measures.size();
			
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
			
			findPhrases();
		}
		Collections.sort(pitches);
		
		noteAverage = new long[noteSum.length];
		volAverage = new long[volTotal.length];
		for(int i = 0; i < noteAverage.length; i++) {
			noteAverage[i] = noteSum[i] / noteTotal[i];
			noteAverage[i] -= noteAverage[i] % 12;
			volAverage[i] = volTotal[i] / noteTotal[i];
		}
	}
	
	private void findPhrases() {
		// [start index of window][start index of curLoc][length of window][intensity of window]
		// array of images with axes [start index of window][startIndex of curLoc] = intensity, different image per length
		long end = notes.get(notes.size() - 1).getStop() + 1;
		int totalBeats = (int) (end / res) + 1;
		System.out.println(totalBeats);
		for(int i = 1; i < 32; i++) { // 32 beats = 8 measures in 4/4 time
			long dur = res * i;
			int[][] img = new int[totalBeats - i][totalBeats - i];
			for(int j = 0; j < totalBeats - i; j++) { // loop through every possible start index, 1 beat at a time
				ArrayList<Note> window = getWindow(j * res, dur);
				for(int k = 0; k < totalBeats - i; k++) { // loop through every possible curLoc, 1 beat at a time
					if(Math.abs(j - k) >= i) {
						ArrayList<Note> cur = getWindow(k * res, dur);
						img[j][k] = getIntensity(window, cur);
					}
				}
			}
			writeImage(img, i);
		}
		// find phrases based on images
		// write a list of all possible phrases greater than a specific length
	}
	
	private int getIntensity(ArrayList<Note> window, ArrayList<Note> cur) {
		double intensity = 0;
		Note[] w = new Note[window.size()];
		Note[] c = new Note[cur.size()];
		window.toArray(w);
		cur.toArray(c);
		double div = window.size() + 0.0;
		for(int i = 0; i < w.length; i++) {
			// find closest match if one exists
			Note best = null;
			long wStart = (w[i].getStart() - w[0].getStart());
			long wDur = w[i].getDuration();
			for(int j = 0; j < c.length; j++) {
				long cStart = c[j].getStart() - c[0].getStart();
				long cDur = c[j].getDuration();
				if(best == null)
					best = c[j];
				if(cStart == wStart) { // both notes start at same time
					best = c[j];
					break;
				} else if(cDur < wDur && (wStart <= cStart && cStart <= wStart + wDur)) {
					best = c[j];
					break;
				} else if(wDur < cDur && (cStart <= wStart && wStart <= cStart + cDur)) {
					best = c[j];
					break;
				}
			}
			if(best == null)
				return 0;
			if(best.getDuration() == wDur)
				intensity += 0.5;
				
			if(best.getPitch() == w[i].getPitch()) {
				intensity += 0.5;
			} else {
				intensity += 0.5 * 1 / (Math.abs(w[i].getPitch() - best.getPitch()));
			}
			
		}
		return (int) (255 * intensity / div);
	}
	
	private ArrayList<Note> getWindow(long start, long dur) {
		ArrayList<Note> re = new ArrayList<Note>();
		for(int i = 0; i < notes.size(); i++) {
			Note n = notes.get(i);
			if(n.getStop() > start + dur)
				break;
			else if(n.getStart() >= start)
				re.add(n);
		}
		if(re.size() == 0) {
			for(int i = 0; i < notes.size(); i++) {
				Note n = notes.get(i);
				if(n.getStart() >= start) {
					re.add(n);
					break;
				}
			}
		}
		return re;
	}
	
	private void writeImage(int[][] data, int name) {
		String path = "Phrase Images\\" + name + ".png";
		BufferedImage img = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);
		for(int x = 0; x < data[0].length; x++) {
			for(int y = 0; y < data.length; y++) {
				img.setRGB(x, y, data[y][x] + (data[y][x] << 8) + (data[y][x] << 16));
			}
		}
		File imgFile = new File(path);
		imgFile.mkdirs();
		try {
			ImageIO.write(img, "png", imgFile);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a new song
	 * 
	 * @return the MIDIFile object representing the generated song
	 */
	public MIDIFile generateSong() {
		MIDIFile output = new MIDIFile();
		output.setResolution(res);
		MusicTrack newTrack = new MusicTrack();
		newTrack.addEvent(TimeSignature.construct(0, (byte) 4, (byte) 4));
		Tempo t = Tempo.construct(0, tempo);
		newTrack.addEvent(t);
		newTrack.changeInstrument(0, 0, instruments[0]);
		MusicTrack[] supportTracks = new MusicTrack[instruments.length - 1];
		for(int i = 0; i < supportTracks.length; i++) {
			supportTracks[i] = new MusicTrack();
			supportTracks[i].changeInstrument(0, i + 1, instruments[i + 1]);
		}
		
		long pos = 0;
		newTrack = getMelody(newTrack, (byte) volAverage[0]); // TODO fix
		
		int bestRating = -1;
		int bestStart = -1;
		int bestList = -1;
		// chord progressions
		for(int r = 0; r < chordDepth; r++) {
			int list = (int) (chordList.size() * Math.random());
			ArrayList<byte[]> chords = chordList.get(list);
			int chordIndex = (int) (Math.random() * (chords.size() - duration));
			int rating = 0;
			for(int j = 0; j < duration; j++) {
				byte[] chord = chords.get(chordIndex);
				chordIndex++;
				pos = 4 * j * res;
				byte melPitch = 0;
				ArrayList<MIDIEvent> events = newTrack.getEvents();
				for(int i = events.size() - 1; i >= 0; i--) {
					MIDIEvent e = events.get(i);
					int status = Byte.toUnsignedInt(e.getStatus()) / 0x10;
					if(e.getTimeStamp() < pos) {
						for(int k = i + 1; k < events.size(); k++) {
							e = events.get(k);
							if(e.getTimeStamp() > pos && status == 0x9 && e.getData()[1] != 0)
								break;
						}
						melPitch = Helper.getLowestOctave(sharps, isMajor, e.getData()[0]);
						break;
					} else if(e.getTimeStamp() == pos && status == 0x9 && e.getData()[1] != 0) {
						melPitch = Helper.getLowestOctave(sharps, isMajor, e.getData()[0]);
						break;
					}
				}
				int index = Helper.getIndex(chord, melPitch);
				if(index >= 0) {
					rating++;
				}
			}
			if(rating > bestRating) {
				bestStart = chordIndex - duration;
				bestRating = rating;
				bestList = list;
			}
		}
		
		if(bestRating < 0.5)
			return generateSong();
		// chords
		ArrayList<byte[]> chords = chordList.get(bestList);
		int chordIndex = bestStart;
		int diSupIndex = (int) (Math.random() * supportTracks.length);
		int triSupIndex = (int) (Math.random() * supportTracks.length);
		while(triSupIndex == diSupIndex) {
			triSupIndex = (int) (Math.random() * supportTracks.length);
		}
		int quaSupIndex = (int) (Math.random() * supportTracks.length);
		while(quaSupIndex == diSupIndex || quaSupIndex == triSupIndex)
			quaSupIndex = (int) (Math.random() * supportTracks.length);
		int melIndex = 0;
		ArrayList<MIDIEvent> events = newTrack.getEvents();
		for(int j = 0; j < duration; j++) {
			pos = 4 * j * res;
			byte[] chord = chords.get(chordIndex);
			chordIndex++;
			byte melPitch = 0;
			long startTime = pos;
			for(int i = events.size() - 1; i >= 0; i--) {
				MIDIEvent e = events.get(i);
				int status = Byte.toUnsignedInt(e.getStatus()) / 0x10;
				if(e.getTimeStamp() < pos) {
					for(int k = i + 1; k < events.size(); k++) {
						e = events.get(k);
						if(e.getTimeStamp() > pos && status == 0x9 && e.getData()[1] != 0) {
							melIndex = k;
							break;
						}
					}
					startTime = e.getTimeStamp();
					melPitch = Helper.getLowestOctave(sharps, isMajor, e.getData()[0]);
					break;
				} else if(e.getTimeStamp() == pos && status == 0x9 && e.getData()[1] != 0) {
					melIndex = i;
					melPitch = Helper.getLowestOctave(sharps, isMajor, e.getData()[0]);
					break;
				}
			}
			int index = Helper.getIndex(chord, melPitch);
			if(index >= 0) {
				// chord contains pitch
				if(chord.length == 2) {
					byte diPitch = chord[(index == 0 ? 1: 0)];
					diPitch = Helper.increaseToAverageOctave(diPitch, (byte) noteAverage[diSupIndex + 1]);
					MIDIEvent start = new MIDIEvent(startTime, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, (byte) volAverage[diSupIndex + 1] });
					MIDIEvent stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, 0 });
					supportTracks[diSupIndex].addEvent(start);
					supportTracks[diSupIndex].addEvent(stop);
				} else if(chord.length == 3) {
					byte diPitch = 0;
					byte triPitch = 0;
					if(index == 0) {
						diPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[2], (byte) noteAverage[triSupIndex + 1]);
					} else if(index == 1) {
						diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[2], (byte) noteAverage[triSupIndex + 1]);
					} else {
						diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[triSupIndex + 1]);
					}
					MIDIEvent start = new MIDIEvent(startTime, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, (byte) volAverage[diSupIndex + 1] });
					MIDIEvent stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, 0 });
					supportTracks[diSupIndex].addEvent(start);
					supportTracks[diSupIndex].addEvent(stop);
					start = new MIDIEvent(startTime, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, (byte) volAverage[triSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, 0 });
					supportTracks[triSupIndex].addEvent(start);
					supportTracks[triSupIndex].addEvent(stop);
				} else if(chord.length == 4) {
					byte diPitch = 0;
					byte triPitch = 0;
					byte quaPitch = 0;
					if(index == 0) {
						diPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[2], (byte) noteAverage[triSupIndex + 1]);
						quaPitch = Helper.increaseToAverageOctave(chord[3], (byte) noteAverage[quaSupIndex + 1]);
					} else if(index == 1) {
						diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[2], (byte) noteAverage[triSupIndex + 1]);
						quaPitch = Helper.increaseToAverageOctave(chord[3], (byte) noteAverage[quaSupIndex + 1]);
					} else if(index == 2) {
						diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[triSupIndex + 1]);
						quaPitch = Helper.increaseToAverageOctave(chord[3], (byte) noteAverage[quaSupIndex + 1]);
					} else {
						diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
						triPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[triSupIndex + 1]);
						quaPitch = Helper.increaseToAverageOctave(chord[2], (byte) noteAverage[quaSupIndex + 1]);
					}
					MIDIEvent start = new MIDIEvent(startTime, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, (byte) volAverage[diSupIndex + 1] });
					MIDIEvent stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, 0 });
					supportTracks[diSupIndex].addEvent(start);
					supportTracks[diSupIndex].addEvent(stop);
					start = new MIDIEvent(startTime, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, (byte) volAverage[triSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, 0 });
					supportTracks[triSupIndex].addEvent(start);
					supportTracks[triSupIndex].addEvent(stop);
					start = new MIDIEvent(startTime, (byte) (0x90 + quaSupIndex + 1), new byte[] { quaPitch, (byte) volAverage[quaSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + quaSupIndex + 1), new byte[] { quaPitch, 0 });
					supportTracks[quaSupIndex].addEvent(start);
					supportTracks[quaSupIndex].addEvent(stop);
				}
			} else {
				MIDIEvent start = events.get(melIndex);
				MIDIEvent stop = events.get(melIndex + 1);
				byte top = Helper.increaseToAverageOctave(chord[chord.length - 1], (byte) noteAverage[0]);
				start.setData(new byte[] { top, start.getData()[1] });
				stop.setData(new byte[] { top, 0 });
				index = chord.length - 1;
				events.remove(melIndex);
				events.add(melIndex, start);
				events.remove(melIndex + 1);
				events.add(melIndex + 1, stop);
				newTrack.setEvents(events);
				if(chord.length == 2) {
					byte diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
					start = new MIDIEvent(startTime, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, (byte) volAverage[diSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, 0 });
					supportTracks[diSupIndex].addEvent(start);
					supportTracks[diSupIndex].addEvent(stop);
				} else if(chord.length == 3) {
					byte diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
					byte triPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[triSupIndex + 1]);
					start = new MIDIEvent(startTime, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, (byte) volAverage[diSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, 0 });
					supportTracks[diSupIndex].addEvent(start);
					supportTracks[diSupIndex].addEvent(stop);
					start = new MIDIEvent(startTime, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, (byte) volAverage[triSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, 0 });
					supportTracks[triSupIndex].addEvent(start);
					supportTracks[triSupIndex].addEvent(stop);
				} else if(chord.length == 4) {
					byte diPitch = Helper.increaseToAverageOctave(chord[0], (byte) noteAverage[diSupIndex + 1]);
					byte triPitch = Helper.increaseToAverageOctave(chord[1], (byte) noteAverage[triSupIndex + 1]);
					byte quaPitch = Helper.increaseToAverageOctave(chord[2], (byte) noteAverage[quaSupIndex + 1]);
					start = new MIDIEvent(startTime, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, (byte) volAverage[diSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + diSupIndex + 1), new byte[] { diPitch, 0 });
					supportTracks[diSupIndex].addEvent(start);
					supportTracks[diSupIndex].addEvent(stop);
					start = new MIDIEvent(startTime, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, (byte) volAverage[triSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + triSupIndex + 1), new byte[] { triPitch, 0 });
					supportTracks[triSupIndex].addEvent(start);
					supportTracks[triSupIndex].addEvent(stop);
					start = new MIDIEvent(startTime, (byte) (0x90 + quaSupIndex + 1), new byte[] { quaPitch, (byte) volAverage[quaSupIndex + 1] });
					stop = new MIDIEvent(pos + 4 * res - 1, (byte) (0x90 + quaSupIndex + 1), new byte[] { quaPitch, 0 });
					supportTracks[quaSupIndex].addEvent(start);
					supportTracks[quaSupIndex].addEvent(stop);
				}
			}
			
		}
		output.addTrack(newTrack);
		for(int i = 0; i < supportTracks.length; i++) {
			output.addTrack(supportTracks[i]);
		}
		return output;
	}
	
	private MusicTrack getMelody(MusicTrack newTrack, byte vol) {
		// add all events to get rhythm
		// create a list of n pitches, where n is the number of notes in the rhythm
		// set the data of the rhythm events
		long pos = 0;
		Measure last = measures.get((int) (Math.random() * measures.size()));
		ArrayList<MIDIEvent> rhythm = new ArrayList<MIDIEvent>();
		for(int j = 0; j < duration; j++) {
			ArrayList<MIDIEvent> newEvents = new ArrayList<MIDIEvent>();
			Measure toAdd = null;
			while(newEvents.size() == 0) {
				Measure[] set = new Measure[measureDepth];
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
			pos = res * 4 * j;
			for(int k = 0; k < newEvents.size(); k += 2) {
				if(!(k == newEvents.size() - 1)) {
					MIDIEvent event = newEvents.get(k).clone();
					MIDIEvent event2 = newEvents.get(k + 1).clone();
					event.setTimeStamp(pos + newEvents.get(k).getTimeStamp());
					event2.setTimeStamp(pos + newEvents.get(k + 1).getTimeStamp());
					rhythm.add(event);
					rhythm.add(event2);
				}
			}
		}
		// Pitch stuff
		int length = rhythm.size();
		ArrayList<Byte> p = getPitches(null, length / 2);
		int index = 0;
		for(int i = 0; i < rhythm.size(); i++) {
			MIDIEvent event = rhythm.get(i);
			int status = Byte.toUnsignedInt(event.getStatus()) / 0x10;
			if(status == 0x9 && event.getData()[1] != 0)
				event.setData(new byte[] { p.get(index), vol });
			else if(status == 0x9 && event.getData()[1] == 0) {
				event.setData(new byte[] { p.get(index), 0 });
				index++;
			}
		}
		newTrack.addAllEvents(rhythm);
		return newTrack;
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
}