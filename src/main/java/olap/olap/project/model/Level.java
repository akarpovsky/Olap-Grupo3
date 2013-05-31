package olap.olap.project.model;

import java.util.HashSet;
import java.util.Set;


public class Level {

	private String name;
	private int pos;
	private Set<Property> properties = new HashSet<Property>();
	
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
