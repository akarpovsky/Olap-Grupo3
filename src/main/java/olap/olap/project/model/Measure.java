package olap.olap.project.model;

public class Measure {

	private String name, type, agg;

	public Measure(String name, String type, String agg) {
		super();
		this.name = name;
		this.type = type;
		this.agg = agg;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getAgg() {
		return agg;
	}
	
	public void print() {
		System.out.println("MEASURE: " +name +", type: " +type+", agg: "+agg);
	}
	
}
