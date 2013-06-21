package olap.olap.project.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class Level {

	private String name;
	private int pos;
	private SortedSet<Property> properties = new TreeSet<Property>(new Comparator<Property>() {

		public int compare(Property o1, Property o2) {
			return o1.getName().compareTo(o2.getName());
		}
	});
	
	public Level(String name, int pos) {
		super();
		this.name = name;
		this.pos = pos;
	}
	
	public void addProperty(Property p) {
		properties.add(p);
	}
	
	public Set<Property> getProperties() {
		return properties;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPos() {
		return pos;
	}
	
	public void print() {
		System.out.println("LEVEL: "+name+" pos: "+pos);
		for(Property p: properties) {
			p.print();
		}
	}
	
}
