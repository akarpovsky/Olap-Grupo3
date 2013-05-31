package olap.olap.project.model;

import java.util.HashSet;
import java.util.Set;

public class Hierarchy {

	private String name;
	private Set<Level> levels = new HashSet<Level>();
	
	public Hierarchy(String name) {
		this.name = name;
	}

	public void addLevel(Level l) {
		levels.add(l);
	}
	
	public String getName() {
		return name;
	}
	
	public Set<Level> getLevels() {
		return levels;
	}
	
	public void print() {
		System.out.println("HIER: "+name);
		for (Level l :levels) {
			l.print();
		}
	}
}

