package olap.olap.project.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Hierarchy {

	private String name;
	private SortedSet<Level> levels = new TreeSet<Level>(new Comparator<Level>() {

		public int compare(Level o1, Level o2) {
			// TODO Auto-generated method stub
			return o1.getPos()-o2.getPos();
		}
		
	});
	
	public Hierarchy(String name) {
		this.name = name;
	}

	public void addLevel(Level l) {
		levels.add(l);
	}
	
	public String getName() {
		return name;
	}
	
	public SortedSet<Level> getLevels() {
		return levels;
	}
	
	public void print() {
		System.out.println("HIER: "+name);
		for (Level l :levels) {
			l.print();
		}
	}
}

