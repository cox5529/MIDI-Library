/**
 * 
 */
package cox5529.generator.storage;

import java.util.ArrayList;
import java.util.Arrays;

import cox5529.midi.Helper;

/**
 * Storage class for musical phrases.
 * 
 * @author Brandon Cox
 * 		
 */
public class Phrase implements Comparable<Phrase> {
	
	protected ArrayList<Note> notes;
	protected ArrayList<ArrayList<Note>> supports;
	protected ArrayList<byte[]> chords;
	protected int id;
	
	/**
	 * Constructs a new Phrase object
	 * 
	 * @param notes the notes within this phrase
	 * @param supports the supporting notes for this phrase
	 * @param id the identifying name of this phrase (i.e) 10 instead of A, 11 instead of A' and so on
	 */
	public Phrase(ArrayList<Note> notes, ArrayList<ArrayList<Note>> supports, int id) {
		this.notes = notes;
		this.supports = supports;
		this.id = id;
		
	}
	
	/**
	 * Constructs a new Phrase object
	 * 
	 * @param notes the notes within this phrase
	 * @param id the identifying name of this phrase (i.e) 10 instead of A, 11 instead of A' and so on
	 * @param chords chord progression in this phrase
	 */
	public Phrase(ArrayList<Note> notes, int id, ArrayList<byte[]> chords) {
		this.notes = notes;
		this.id = id;
		this.chords = chords;
	}
	
	/**
	 * Generates the support measures for this Phrase
	 * 
	 * @param supCount the number of support measures
	 * @param avgOctave the average octave of the instruments
	 * @param sharps the number of sharps in the key signature, negative if flats
	 * @param isMajor true if the key is major
	 */
	public void generateSupports(int supCount, long[] avgOctave, int sharps, boolean isMajor) {
		supports = new ArrayList<ArrayList<Note>>();
		for(int i = 0; i < supCount; i++) {
			supports.add(new ArrayList<Note>());
		}
		int idx = 0;
		long pos = 0;
		int diSupIndex = (int) (supCount * Math.random());
		int triSupIndex = (int) (supCount * Math.random());
		int quaSupIndex = (int) (supCount * Math.random());
		while(triSupIndex == diSupIndex) {
			triSupIndex = (int) (supCount * Math.random());
		}
		while(quaSupIndex == diSupIndex || quaSupIndex == triSupIndex) {
			quaSupIndex = (int) (supCount * Math.random());
		}
		ArrayList<Note> qua = supports.get(quaSupIndex);
		ArrayList<Note> tri = supports.get(triSupIndex);
		ArrayList<Note> di = supports.get(diSupIndex);
		if(Math.random() >= 0) {
			byte[] c = chords.remove(chords.size() - 1);
			chords.add(Helper.getChordFromNumeral("V", c.length, sharps, isMajor));
		} else {
			byte[] c = chords.remove(chords.size() - 1);
			chords.remove(chords.size() - 1);
			chords.add(Helper.getChordFromNumeral("V", c.length, sharps, isMajor));
			chords.add(Helper.getChordFromNumeral("I", c.length, sharps, isMajor));
		}
		for(int i = 0; i < chords.size(); i++) {
			int step = (notes.size() - idx) / (chords.size() - i);
			long dur = 0;
			for(int j = idx; j < idx + step; j++) {
				dur += notes.get(j).getDuration() + 1;
			}
			idx += step;
			byte[] chord = chords.get(i);
			if(chord.length > 0) {
				for(int j = 0; j < notes.size(); j++) {
					Note n = notes.get(j);
					if(n.getStart() == pos) {
						byte p = Helper.increaseToAverageOctave(chord[chord.length - 1], (byte) avgOctave[0]);
						n.setPitch(p);
						break;
					}
				}
			}
			dur -= 1;
			if(chord.length > 1) {
				di.add(new Note(pos, pos + dur, Helper.increaseToAverageOctave(chord[0], (byte) avgOctave[diSupIndex + 1])));
			}
			if(chord.length > 2) {
				tri.add(new Note(pos, pos + dur, Helper.increaseToAverageOctave(chord[1], (byte) avgOctave[triSupIndex + 1])));
			}
			if(chord.length > 3) {
				qua.add(new Note(pos, pos + dur, Helper.increaseToAverageOctave(chord[2], (byte) avgOctave[quaSupIndex + 1])));
			}
			pos += dur + 1;
		}
		for(int i = 0; i < supports.size(); i++) {
			ArrayList<Note> sup = supports.get(i);
			if(sup.isEmpty()) {
				sup.add(new Note(0, pos, (byte) -1));
			}
		}
	}
	
	/**
	 * Gets the chord progressions for this Phrase
	 * 
	 * @return the chord progressions for this Phrase
	 */
	public ArrayList<byte[]> getChords() {
		return chords;
	}
	
	/**
	 * Generates the chords for this phrase
	 * 
	 * @param sharps the number of sharps in a key. If there are flats, number of flats * -1
	 * @param isMajor true if the key is major
	 */
	public void generateChords(int sharps, boolean isMajor) {
		chords = new ArrayList<byte[]>();
		int minI = -1;
		int minNotes = -1;
		for(int i = 0; i < supports.size(); i++) {
			ArrayList<Note> sup = supports.get(i);
			if(sup.size() > 1 || (sup.size() == 1 && sup.get(0).getPitch() != -1)) {
				if(minI == -1) {
					minI = i;
					minNotes = sup.size();
				} else if(sup.size() < minNotes) {
					minNotes = sup.size();
					minI = i;
				}
			}
		}
		long[] starts = new long[minNotes];
		for(int i = 0; i < starts.length; i++) {
			starts[i] = supports.get(minI).get(i).getStart();
		}
		for(int k = 0; k < starts.length; k++) {
			ArrayList<Byte> pitches = new ArrayList<Byte>();
			for(int i = 0; i < notes.size(); i++) {
				if(notes.get(i).getStart() >= starts[k] && notes.get(i).getPitch() != -1) {
					pitches.add(Helper.getLowestOctave(sharps, isMajor, notes.get(i).getPitch()));
					break;
				}
			}
			for(int i = 0; i < supports.size(); i++) {
				ArrayList<Note> sup = supports.get(i);
				for(int j = 0; j < sup.size(); j++) {
					if(sup.get(j).getStart() >= starts[k] && sup.get(j).getPitch() != -1) {
						pitches.add(Helper.getLowestOctave(sharps, isMajor, sup.get(j).getPitch()));
						break;
					}
				}
			}
			Byte[] toAdd = new Byte[pitches.size()];
			pitches.toArray(toAdd);
			byte[] real = new byte[toAdd.length];
			Arrays.sort(toAdd);
			for(int i = 0; i < toAdd.length; i++) {
				real[i] = toAdd[i];
			}
			chords.add(real);
		}
	}
	
	/**
	 * Determines if two phrases have the same instrumentation
	 * 
	 * @param p the phrase to compare
	 * @return true if the instrumentation is the same
	 */
	public boolean supportMatch(Phrase p) {
		ArrayList<ArrayList<Note>> pSupports = p.getSupports();
		for(int i = 0; i < pSupports.size(); i++) {
			ArrayList<Note> pList = pSupports.get(i);
			ArrayList<Note> sList = supports.get(i);
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
	
	/**
	 * Gets the number of children in this tree
	 * 
	 * @return the number of children in this tree
	 */
	public int getChildCount() {
		return 1;
	}
	
	/**
	 * Mutates a phrase so that it can be used while generating music
	 * 
	 * @param sharps the number of sharps in a key. If there are flats, number of flats * -1
	 * @param isMajor true if the key is major
	 * @param id the id of the mutated phrase
	 * @return mutated version of this phrase
	 */
	public Phrase mutate(int sharps, boolean isMajor, int id) {
		ArrayList<Note> mel = new ArrayList<Note>();
		double rand = Math.random();
		if(rand > 0.5) { // raise all notes by 1
			mel = raise(notes, sharps, isMajor);
		} else { // lower all notes by 1
			mel = lower(notes, sharps, isMajor);
		}
		return new Phrase(mel, id, chords);
		
	}
	
	private ArrayList<Note> raise(ArrayList<Note> notes, int sharps, boolean isMajor) {
		ArrayList<Note> re = new ArrayList<Note>();
		for(int i = 0; i < notes.size(); i++) {
			Note n = notes.get(i).clone();
			if(n.getPitch() != -1) {
				n.setPitch(Helper.getPitchAbove(sharps, isMajor, n.getPitch()));
			}
			re.add(n);
		}
		return re;
	}
	
	private ArrayList<Note> lower(ArrayList<Note> notes, int sharps, boolean isMajor) {
		ArrayList<Note> re = new ArrayList<Note>();
		for(int i = 0; i < notes.size(); i++) {
			Note n = notes.get(i).clone();
			if(n.getPitch() != -1)
				n.setPitch(Helper.getPitchBelow(sharps, isMajor, n.getPitch()));
			re.add(n);
		}
		return re;
	}
	
	/**
	 * Gets the average duration of the notes in the melody in this phrase
	 * 
	 * @return the average duration of the notes in the melody in this phrase in midi clocks
	 */
	public double getAverageDuration() {
		long sum = 0;
		for(int i = 0; i < notes.size(); i++) {
			sum += notes.get(i).getDuration();
		}
		return (sum + 0.0) / notes.size();
	}
	
	/**
	 * Gets the list of notes in this phrase
	 * 
	 * @return the list of notes in this phrase
	 */
	public ArrayList<Note> getNotes() {
		return new ArrayList<Note>(notes);
	}
	
	/**
	 * Gets the supporting notes for this phrase
	 * 
	 * @return the supporting notes for this phrase
	 */
	public ArrayList<ArrayList<Note>> getSupports() {
		return supports;
	}
	
	/**
	 * Gets the identifying name of this phrase
	 * 
	 * @return the identifying name of this phrase
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Adds an ArrayList of Notes to this phrase
	 * 
	 * @param notes the ArrayList of Notes to add
	 */
	public void addNotes(ArrayList<Note> notes) {
		this.notes.addAll(notes);
	}
	
	/**
	 * Gets the Id of this Phrase as a String
	 * 
	 * @return the ID of this Phrase as a String
	 */
	public String getStringId() {
		return id + "";
	}
	
	/**
	 * Sets the ID of this phrase
	 * 
	 * @param id the new ID of this phrase
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the String representation of this phrase
	 * 
	 * @param res the resolution of this phrase
	 * @return the String representation of this phrase
	 */
	public String toString(int res) {
		String re = "ID: " + id + "\nDurations:";
		for(int i = 0; i < notes.size(); i++) {
			double dur = ((notes.get(i).getDuration() + 1.0) / (res + 0.0));
			re += " " + dur;
		}
		re += "\nPitches:";
		for(int i = 0; i < notes.size(); i++) {
			re += " " + notes.get(i).getPitch();
		}
		return re;
	}
	
	/**
	 * Gets the ids of all children in order in this
	 * 
	 * @return an array with the id of this phrase in it
	 */
	public int[] getChildIds() {
		return new int[] { id };
	}
	
	public Phrase clone() {
		ArrayList<Note> nNotes = new ArrayList<Note>();
		for(int i = 0; i < notes.size(); i++) {
			nNotes.add(notes.get(i).clone());
		}
		ArrayList<ArrayList<Note>> nSup = new ArrayList<ArrayList<Note>>();
		for(int i = 0; i < supports.size(); i++) {
			ArrayList<Note> orig = supports.get(i);
			ArrayList<Note> sup = new ArrayList<Note>();
			for(int j = 0; j < orig.size(); j++) {
				sup.add(orig.get(j).clone());
			}
			nSup.add(sup);
		}
		return new Phrase(nNotes, nSup, id);
	}
	
	@Override public int compareTo(Phrase p) {
		if(p.getId() > id)
			return -1;
		else if(p.getId() < id)
			return 1;
		else
			return 0;
	}
}
