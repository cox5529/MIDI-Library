/**
 * 
 */
package cox5529.generator.storage;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Brandon Cox
 * 		
 */
public class PhraseTree extends Phrase {
	
	private Phrase p1;
	private Phrase p2;
	
	private PhraseTree(ArrayList<Note> notes, ArrayList<ArrayList<Note>> supports, int id, Phrase p1, Phrase p2) {
		super(notes, supports, id);
		this.p1 = p1;
		this.p2 = p2;
	}
	
	private PhraseTree(ArrayList<Note> notes, int id, ArrayList<byte[]> chords, Phrase p1, Phrase p2) {
		super(notes, id, chords);
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/**
	 * Gets the number of children in this tree
	 * 
	 * @return the number of children in this tree
	 */
	public int getChildCount() {
		int count = 0;
		if(p1 != null)
			count += p1.getChildCount();
		if(p2 != null)
			count += p2.getChildCount();
		return count;
	}
	
	/**
	 * Constructs a PhraseTree
	 * 
	 * @param p1 the first phrase child
	 * @param p2 the second phrase child
	 * @param id the id of this phrase
	 * @return a newly constructed PhraseTree
	 */
	public static PhraseTree construct(Phrase p1, Phrase p2, int id) {
		ArrayList<Note> notes = new ArrayList<Note>();
		ArrayList<Note> n1 = p1.getNotes();
		ArrayList<Note> n2 = p2.getNotes();
		notes.addAll(n1);
		notes.addAll(n2);
		
		ArrayList<ArrayList<Note>> supNotes = new ArrayList<ArrayList<Note>>();
		ArrayList<ArrayList<Note>> p1Sup = p1.getSupports();
		ArrayList<ArrayList<Note>> p2Sup = p2.getSupports();
		if(p1Sup == null) {
			ArrayList<byte[]> chords = new ArrayList<byte[]>();
			chords.addAll(p1.getChords());
			chords.addAll(p2.getChords());
			return new PhraseTree(notes, id, chords, p1, p2);
		} else {
			for(int i = 0; i < p1Sup.size(); i++) {
				ArrayList<Note> n = new ArrayList<Note>();
				n.addAll(p1Sup.get(i));
				n.addAll(p2Sup.get(i));
				supNotes.add(n);
			}
			return new PhraseTree(notes, supNotes, id, p1, p2);
		}
	}
	
	/**
	 * Gets the first child of this PhraseTree
	 * 
	 * @return the first child of this PhraseTree
	 */
	public Phrase getFirstChild() {
		return p1;
	}
	
	/**
	 * Gets the second child of this PhraseTree
	 * 
	 * @return the second child of this PhraseTree
	 */
	public Phrase getSecondChild() {
		return p2;
	}
	
	/**
	 * Gets a HashSet of the children of this phrase
	 * 
	 * @return a HashSet of the children of this phrase
	 */
	public HashSet<Phrase> getChildren() {
		HashSet<Phrase> re = new HashSet<Phrase>();
		if(p1 instanceof PhraseTree) {
			re.addAll(((PhraseTree) p1).getChildren());
		} else
			re.add(p1);
		if(p2 instanceof PhraseTree) {
			re.addAll(((PhraseTree) p2).getChildren());
		} else
			re.add(p2);
		return re;
	}
	
	/**
	 * Gets the ID of this phrase and its children as a String
	 * 
	 * @return the ID of this phrase and its children as a String
	 */
	public String getStringId() {
		return p1.getStringId() + " + " + p2.getStringId();
	}
	
	/**
	 * Gets the String representation of this phrase
	 * 
	 * @param res the resolution of this phrase
	 * @return the String representation of this phrase
	 */
	public String toString(int res) {
		String re = "ID: " + id + " (" + getStringId() + ")\nDurations:";
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
	 * Given an array of ids, determines which measures have different ids.
	 * 
	 * @param ids array of ids to compare to the ids in this tree
	 * @return a boolean array of the measures that are different, or null if ids.length is not the same as the number of children in this tree
	 */
	public boolean[] derivLocation(int[] ids) {
		int[] childs = getChildIds();
		if(ids.length != childs.length)
			return null;
		boolean[] deriv = new boolean[childs.length];
		for(int i = 0; i < deriv.length; i++) {
			if(ids[i] != childs[i])
				deriv[i] = true;
			else
				deriv[i] = false;
		}
		return deriv;
	}
	
	/**
	 * Gets the ids of all children in order in this tree
	 * 
	 * @return the ids of all children in order in this tree
	 */
	public int[] getChildIds() {
		String strId = getStringId();
		String[] cIds = strId.split(" \\+ ");
		int[] childs = new int[cIds.length];
		for(int i = 0; i < cIds.length; i++) {
			childs[i] = Integer.parseInt(cIds[i]);
		}
		return childs;
	}
	
}
