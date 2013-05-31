package olap.olap.project.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Cube {

	private Map<String, Dimension> dimensions = new HashMap<String, Dimension>();
	private Set<Measure> measures = new HashSet<Measure>();
	private String name;
	
	public Cube(String name) {
		this.name = name;
	}
	
	public void addDimension(String dimName, Dimension dim) {
		dimensions.put(dimName, dim);
	}
	
	public Map<String, Dimension> getDimensions() {
		return dimensions;
	}

	public void addMeasure(Measure m) {
		measures.add(m);
	}
	
	public Set<Measure> getMeasures() {
		return measures;
	}
	
	public String getName() {
		return name;
	}
	
	public void print() {
		System.out.println("CUBE: " +name);
		for(Measure m: measures) {
			m.print();
		}
		for(Entry<String, Dimension> e : dimensions.entrySet()) {
			System.out.println("DIM: " +e.getKey() + ": " + e.getValue().getName());
		}
	}
}
