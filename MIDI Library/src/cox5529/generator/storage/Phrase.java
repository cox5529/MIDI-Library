/**
 * 
 */
package cox5529.generator.storage;

import java.util.ArrayList;

/**
 * Storage class for musical phrases.
 * 
 * @author Brandon Cox
 * 		
 */
public class Phrase {
	
	protected ArrayList<Note> notes;
	protected ArrayList<ArrayList<Note>> supports;
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
		return new ArrayList<ArrayList<Note>>(supports);
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
}
