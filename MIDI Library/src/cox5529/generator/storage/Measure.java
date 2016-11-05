package cox5529.generator.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import cox5529.midi.Helper;
import cox5529.midi.event.MIDIEvent;

/**
 * Class used to store measures.
 * 
 * @author Brandon Cox
 * 		
 */
public class Measure {
	
	private ArrayList<MIDIEvent> events;
	private ArrayList<Note> notes;
	private ArrayList<ArrayList<Note>> supNotes;
	private ArrayList<ArrayList<MIDIEvent>> supports;
	
	/**
	 * Constructs a measure object.
	 * 
	 * @param events the MIDIEvents that make up the measure
	 * @param res the resolution of this measure
	 */
	public Measure(ArrayList<MIDIEvent> events, int res) {
		Collections.sort(events);
		this.supports = new ArrayList<ArrayList<MIDIEvent>>();
		notes = new ArrayList<Note>();
		long start = 0;
		if(events.size() > 0) {
			MIDIEvent event = events.get(0);
			int status = Byte.toUnsignedInt(event.getStatus());
			if(status / 0x10 == 0x9 && event.getData()[1] == 0) {
				System.out.println("note off");
				events.add(0, new MIDIEvent(0, (byte) status, new byte[] { event.getData()[0], events.get(1).getData()[1] }));
			}
			event = events.get(events.size() - 1);
			status = Byte.toUnsignedInt(event.getStatus());
			if(status / 0x10 == 0x9 && event.getData()[1] != 0) {
				System.out.println("note on");
				events.add(new MIDIEvent(4 * res - 1, (byte) status, new byte[] { event.getData()[0], 0 }));
			}
		}
		this.events = events;
		for(int i = 0; i < events.size(); i++) {
			MIDIEvent event = events.get(i);
			int status = Byte.toUnsignedInt(event.getStatus());
			if(status / 0x10 == 0x9 && event.getData()[1] != 0) {
				start = event.getTimeStamp();
			} else if(status / 0x10 == 0x9 && event.getData()[1] == 0) {
				notes.add(new Note(start, event.getTimeStamp(), event.getData()[0]));
			}
		}
		supNotes = new ArrayList<ArrayList<Note>>();
	}
	
	/**
	 * Adds the supporting melody to this measure
	 * 
	 * @param events the notes in the supporting measure
	 */
	public void addSupport(ArrayList<MIDIEvent> events) {
		supports.add(events);
		ArrayList<Note> notes = new ArrayList<Note>();
		if(events.size() != 0) {
			long start = 0;
			for(int i = 0; i < events.size(); i++) {
				MIDIEvent event = events.get(i);
				int status = Byte.toUnsignedInt(event.getStatus());
				if(status / 0x10 == 0x9 && event.getData()[1] != 0) {
					start = event.getTimeStamp();
				} else if(status / 0x10 == 0x9 && event.getData()[1] == 0) {
					notes.add(new Note(start, event.getTimeStamp(), event.getData()[0]));
				}
			}
		} else if(this.notes.size() > 0) {
			long start = this.notes.get(0).getStart();
			long stop = this.notes.get(this.notes.size() - 1).getStop();
			notes.add(new Note(start, stop, (byte) -1));
		}
		supNotes.add(notes);
	}
	
	/**
	 * Gets the average duration of notes in the melody of this measure in MIDI clocks
	 *
	 * @return the average duration of notes in this measure in MIDI clocks
	 */
	public double getAverageSpeed() {
		long sum = 0;
		int count = 0;
		for(int i = 0; i < events.size(); i += 2) {
			sum += (events.get(i + 1).getTimeStamp() - events.get(i).getTimeStamp());
			count++;
		}
		return (sum + 0.0) / count;
	}
	
	/**
	 * Gets the chords within this measure.
	 * 
	 * @param sharps number of sharps in the key
	 * @param isMajor true if the key is major
	 * 		
	 * @return the chords within this measure represented as a 2D array of bytes, each row representing a chord.
	 */
	public byte[][] getChords(int sharps, boolean isMajor) {
		byte[][] re;
		int minNotes = 900;
		ArrayList<MIDIEvent> minTrack = null;
		for(int i = 0; i < supports.size(); i++) {
			ArrayList<MIDIEvent> track = supports.get(i);
			if(track.size() / 2 < minNotes) {
				minNotes = track.size() / 2;
				minTrack = track;
			}
		}
		if(minNotes == 0)
			return new byte[][] {};
		re = new byte[minNotes][];
		// find chord notes
		long[] stamps = new long[minNotes];
		int index = 0;
		for(int i = 0; i < minTrack.size(); i++) {
			int status = Byte.toUnsignedInt(minTrack.get(i).getStatus());
			if(status / 0x10 == 0x9 && minTrack.get(i).getData()[1] != 0) {
				stamps[index] = minTrack.get(i).getTimeStamp();
				index++;
			}
		}
		for(int i = 0; i < minNotes; i++) {
			byte[] pitches = new byte[supports.size() + 1];
			// loop through each track
			for(int j = events.size() - 1; j >= 0; j--) {
				if(events.get(j).getTimeStamp() <= stamps[i]) {
					pitches[0] = events.get(j).getData()[0];
					break;
				}
			}
			for(int j = 0; j < supports.size(); j++) {
				ArrayList<MIDIEvent> track = supports.get(j);
				for(int k = track.size() - 1; k >= 0; k--) {
					if(track.get(k).getTimeStamp() <= stamps[i]) {
						pitches[j + 1] = track.get(k).getData()[0];
						break;
					}
				}
			}
			// get numeral and add to array
			re[i] = Helper.getChord(sharps, isMajor, pitches);
		}
		// return
		return re;
	}
	
	/**
	 * Gets the list of MIDIEvents that makes up this measure.
	 * 
	 * @return the list of MIDIEvents that makes up this measure
	 */
	public ArrayList<MIDIEvent> getEvents() {
		ArrayList<MIDIEvent> re = new ArrayList<MIDIEvent>();
		for(int i = 0; i < events.size(); i++) {
			re.add(events.get(i));
		}
		return re;
	}
	
	/**
	 * Gets the note ArrayList in this measure
	 * 
	 * @return the note ArrayList in this measure
	 */
	public ArrayList<Note> getNotes() {
		ArrayList<Note> re = new ArrayList<Note>();
		for(int i = 0; i < notes.size(); i++) {
			re.add(notes.get(i).clone());
		}
		return re;
	}
	
	/**
	 * Gets the average speed in MIDI clocks for part of a measure
	 * 
	 * @param start the time in MIDI clocks when the measurement should begin or end
	 * @param end true if time marks the end of the measurement period, false if it marks the beginning
	 * @return the average speed of the appropriate half in MIDI clocks
	 */
	public double getPartialAverageSpeed(long start, boolean end) {
		long sum = 0;
		int count = 0;
		if(end) {
			for(int i = 0; i < events.size(); i += 2) {
				MIDIEvent event = events.get(i);
				if(event.getTimeStamp() > start)
					break;
				else {
					sum += (events.get(i + 1).getTimeStamp() - event.getTimeStamp());
					count++;
				}
			}
		} else {
			for(int i = 0; i < events.size(); i += 2) {
				MIDIEvent event = events.get(i);
				if(event.getTimeStamp() < start)
					continue;
				else {
					sum += (events.get(i + 1).getTimeStamp() - event.getTimeStamp());
					count++;
				}
			}
		}
		return (sum + 0.0) / count;
	}
	
	/**
	 * Gets the ID of the phrase segment represented by this measure
	 * 
	 * @param phrases ArrayList of phrases in the current song
	 * @return the ID of the phrase in this measure
	 */
	public int getPhrase(ArrayList<Phrase> phrases) {
		if(phrases.size() != 0) {
			for(int i = phrases.size() - 1; i >= 0; i--) {
				int val = isPhrase(phrases.get(i));
				if(val == 0)
					return phrases.get(i).getId();
				else if(val == 1)
					return phrases.get(i).getId() + 1;
			}
		}
		return -1; // phrases does not contain this measure
	}
	
	/**
	 * Gets the supporting measures for this measure.
	 * 
	 * @return the supporting measures
	 */
	public ArrayList<ArrayList<MIDIEvent>> getSupport() {
		ArrayList<ArrayList<MIDIEvent>> re = new ArrayList<ArrayList<MIDIEvent>>();
		for(int i = 0; i < supports.size(); i++) {
			ArrayList<MIDIEvent> arr = new ArrayList<MIDIEvent>();
			ArrayList<MIDIEvent> sup = supports.get(i);
			for(int j = 0; j < sup.size(); j++) {
				arr.add(sup.get(j));
			}
			re.add(arr);
		}
		return re;
	}
	
	/**
	 * Gets the supporting tracks in Note form
	 * 
	 * @return the supporting tracks in Note form
	 */
	public ArrayList<ArrayList<Note>> getSupportNotes() {
		return supNotes;
	}
	
	private int isPhrase(Phrase p) { // -1 for false, 0 for true, 1 for deriv
		ArrayList<Note> pNotes = p.getNotes();
		// is same?
		if(pNotes.size() == notes.size()) {
			boolean equal = true;
			for(int i = 0; i < pNotes.size(); i++) {
				if(!pNotes.get(i).equals(notes.get(i))) {
					equal = false;
					break;
				}
			}
			if(equal && isSimilarSupport(p))
				return 0;
		}
		// is deriv?
		if(isPitchDeriv(pNotes, notes) && isSimilarSupport(p))
			return 1;
		return -1;
	}
	
	private boolean isPitchDeriv(ArrayList<Note> pNotes, ArrayList<Note> notes) {
		if(pNotes.size() == notes.size()) {
			if(!isSameRhythm(pNotes, notes))
				return false;
			Byte[] pPitch = new Byte[notes.size()];
			Byte[] nPitch = new Byte[notes.size()];
			for(int i = 0; i < pPitch.length; i++) {
				pPitch[i] = pNotes.get(i).getPitch();
				nPitch[i] = notes.get(i).getPitch();
			}
			byte pMin = Collections.min(Arrays.asList(pPitch));
			byte nMin = Collections.min(Arrays.asList(nPitch));
			boolean deriv = true;
			for(int i = 0; i < pPitch.length; i++) {
				pPitch[i] = (byte) (pPitch[i] - pMin);
				nPitch[i] = (byte) (nPitch[i] - nMin);
				if(nPitch[i] != pPitch[i]) {
					deriv = false;
					break;
				}
			}
			return deriv;
		}
		return false;
	}
	
	private boolean isRhythmicDeriv(ArrayList<Note> pNotes, ArrayList<Note> notes) {
		if(isSameRhythm(pNotes, notes))
			return false;
		long[] nDur = new long[notes.size()];
		long[] pDur = new long[pNotes.size()];
		int pInd = 0;
		int misses = 0;
		int iter = 0;
		long skipSum = 0;
		int lastSkip = 0; // -1 = p, 1 = n
		for(int i = 0; i < nDur.length; i++) {
			nDur[i] = notes.get(i).getDuration();
		}
		for(int i = 0; i < pDur.length; i++) {
			pDur[i] = pNotes.get(i).getDuration();
		}
		for(int i = 0; i < nDur.length; i++) {
			iter++;
			if(pInd == pDur.length) {
				misses += (pDur.length - pInd);
				break;
			} else if((skipSum > pDur[pInd] && lastSkip == -1) || (skipSum > nDur[i] && lastSkip == 1)) {
				return false;
			} else if(pDur[pInd] == nDur[i] || (skipSum == pDur[pInd] && lastSkip == -1) || (skipSum == nDur[i] && lastSkip == 1)) {
				pInd++;
				skipSum = 0;
			} else if(pDur[pInd] > nDur[i]) {
				skipSum += nDur[i];
				lastSkip = -1;
				misses++;
			} else if(pDur[pInd] < nDur[i]) {
				skipSum += pDur[pInd];
				lastSkip = 1;
				misses++;
				pInd++;
				i--;
			}
		}
		return ((misses + 0.0) / iter) < 0.1;
	}
	
	private boolean isSameRhythm(ArrayList<Note> pNotes, ArrayList<Note> notes) {
		if(pNotes.size() == notes.size()) {
			for(int i = 0; i < pNotes.size(); i++) {
				if(pNotes.get(i).getDuration() != notes.get(i).getDuration())
					return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean isSimilarSupport(Phrase p) { // chords + same rhythms
		ArrayList<ArrayList<Note>> pSupports = p.getSupports();
		for(int i = 0; i < pSupports.size(); i++) {
			ArrayList<Note> pList = pSupports.get(i);
			ArrayList<Note> sList = supNotes.get(i);
			boolean pRest = true;
			for(int j = 0; j < pList.size(); j++) {
				if(pList.get(j).getPitch() != -1) {
					pRest = false;
					break;
				}
			}
			boolean sRest = true;
			for(int j = 0; j < sList.size(); j++) {
				if(sList.get(j).getPitch() != -1) {
					sRest = false;
					break;
				}
			}
			if(sRest != pRest)
				return false;
		}
		return true;
	}
}
