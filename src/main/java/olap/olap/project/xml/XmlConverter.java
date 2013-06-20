package olap.olap.project.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import olap.olap.project.model.Attribute;
import olap.olap.project.model.Cube;
import olap.olap.project.model.Dimension;
import olap.olap.project.model.Hierarchy;
import olap.olap.project.model.Level;
import olap.olap.project.model.Measure;
import olap.olap.project.model.MultiDim;
import olap.olap.project.model.Property;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

@SuppressWarnings("unchecked")
public class XmlConverter {
	
	/**
	 * Converts a xmlFile to a MultiDim 
	 */
	public MultiDim parse(File xml) throws DocumentException, IOException {
		MultiDim multiDim = new MultiDim();
		SAXReader reader = new SAXReader();
		Document in = reader.read(xml);
		Element multidim = in.getRootElement();
		Iterator<Element> i = multidim.elementIterator();
		Element cubeElement = null;
		while (i.hasNext()) {
            Element e = i.next();
            if (e.getName().equals("cubo")) {
            	cubeElement = e;
            } else if (e.getName().equals("dimension")){
            	parseDimension(multiDim, e);
            } else {
            	throw new RuntimeException("invalid " + e.getName() +" dimension or cube tags only accepted");
            }
        }
		parseCube(multiDim, cubeElement);
//		multiDim.print();
		return multiDim;
	}
	
	/**
	 * Converts a MultiDim to a GeoMondrian XML
     * Lo hice entero, falta debuguearlo pero a primera vista parece estar mas o menos bien. 
	 */
	public void generateXml(MultiDim multiDim, String fileName) throws IOException {
		Document out = DocumentHelper.createDocument();
		out = out.addDocType("Schema", null, "mondrian.dtd");
		Element schema = out.addElement("Schema");
		schema.addAttribute("name",multiDim.getCube().getName());
		Element cubeElem = schema.addElement("Cube");
		Cube cube = multiDim.getCube();
		cubeElem.addAttribute("name", cube.getName());
		cubeElem.addAttribute("cache", "true");
		cubeElem.addAttribute("enabled", "true");
		Element factTable = cubeElem.addElement("Table");
		factTable.addAttribute("name", cube.getName() + "Fact");
		
		for (Entry<String, Dimension> entry : multiDim.getCube().getDimensions().entrySet()) {
			String pk = "";
			String pkType = "";
			String dimName = entry.getKey() + entry.getValue().getName();
			Element dim = cubeElem.addElement("Dimension");
			Dimension dimension = entry.getValue();
			dim.addAttribute("name", dimName);
			for(Property p: dimension.getLevel().getProperties()){
				if(p.isPK()){
					pk = p.getName()+dimName;
					dim.addAttribute("foreignKey",pk );
					pkType =  Attribute.valueOf(p.getType().toUpperCase()).toString();
					break; //Lo hace sólo para el primero, si es compuesta se debe cambiar.
				}
			}
			if(dimension.getHierarchies()==null || dimension.getHierarchies().isEmpty()){
				Element h = dim.addElement("Hierarchy");
				h.addAttribute("name", pk);
				h.addAttribute("hasAll", "true");
				h.addAttribute("allMemberName", "All" + dimName);
				h.addAttribute("primaryKey", pk);
				h.addElement("Table").addAttribute("name", dimName);
				Element level = h.addElement("Level");
				level.addAttribute("name", dimName);
				level.addAttribute("type", pkType);
				level.addAttribute("uniqueMembers", "false");
				level.addAttribute("levelType", "Regular");
				level.addAttribute("hideMemberIf", "Never");
				level.addAttribute("column", pk);
				Element prop = level.addElement("Property");
				prop.addAttribute("name", pk);
				prop.addAttribute("column", pk);
				prop.addAttribute("type", pkType);
				
			}
			for(Hierarchy h : dimension.getHierarchies()) {
				handleHierarchy(dim, h,pk,pkType,dimName);
			}
		}
		
//		Element tableElem = cubeElem.addElement("Relation");
		//Element table = cubeElem.addElement("Table");
		
		for (Measure m : cube.getMeasures()) {
			Element measure = cubeElem.addElement("Measure");
			String aggName = m.getAgg();
			if(aggName.equals("st_union")) aggName = "sum";
			measure.addAttribute("aggregator", aggName);
			measure.addAttribute("name", m.getName());
			measure.addAttribute("column", m.getName());
			measure.addAttribute("datatype", Attribute.valueOf(m.getType().toUpperCase()).toString());
		}
		XMLWriter writer = new XMLWriter(
				new FileWriter( fileName )
				);
		writer.write(out);
		writer.close();
	}
	
	private void handleHierarchy(Element dim, Hierarchy hierarchy, String pk, String pktype, String dimName) {
		Element h = dim.addElement("Hierarchy");
		h.addAttribute("name", hierarchy.getName());
		h.addAttribute("hasAll", "true");
		h.addAttribute("allMemberName", "All" + hierarchy.getName());
		h.addAttribute("primaryKey", pk);
		h.addElement("Table").addAttribute("name", dimName);
		for(Level l : hierarchy.getLevels()) {
			handleLevel(h, l, dimName);
		}
	}
	
	private void handleLevel(Element hierarchy, Level l, String dimName) {
		Element level = hierarchy.addElement("Level");
		level.addAttribute("name", l.getName());
		level.addAttribute("uniqueMembers", "false");
		level.addAttribute("levelType", "Regular");
		level.addAttribute("hideMemberIf", "Never");
		for(Property p : l.getProperties()) {
			if(p.isPK()){
				level.addAttribute("column", l.getName()+"_"+p.getName()+"_" +dimName );
				level.addAttribute("type",  Attribute.valueOf(p.getType().toUpperCase()).toString());
			}
			Element prop = level.addElement("Property");
			prop.addAttribute("name", l.getName()+"_"+p.getName());
			prop.addAttribute("column", l.getName()+"_"+p.getName()+"_" +dimName);
			prop.addAttribute("type", Attribute.valueOf(p.getType().toUpperCase()).toString());
		}
	}
	
	private void parseCube(MultiDim multiDim, Element c) {
		Cube cube = new Cube(c.attributeValue("name"));
		Iterator<Element> i = c.elementIterator();
		while (i.hasNext()) {
			Element e = i.next();
			if (e.getName().equals("measure")) {
				cube.addMeasure(new Measure(e.attributeValue("name"), e.attributeValue("type"), e.attributeValue("agg")));
			} else if(e.getName().equals("dimension")) {
				String ptr = e.attributeValue("ptr");
				Dimension dim = multiDim.getDimension(ptr);
				if (dim == null) {
	            	throw new RuntimeException("No dimension "+ptr+" was found");
				}
				cube.addDimension(e.attributeValue("name"), dim);
			} else {
            	throw new RuntimeException("invalid " + e.getName() +" measure or dimension tags only accepted");
            }
		}
		multiDim.setCube(cube);
	}
	
	private void parseDimension(MultiDim multiDim, Element dimension) {
		Dimension dim = new Dimension(dimension.attributeValue("name"));
		Iterator<Element> i = dimension.elementIterator();
		while(i.hasNext()) {
			Element e = i.next();
			if(e.getName().equals("level")) {
				Level level = new Level(dim.getName(), 0);
				parseProperties(level, e);
				dim.setLevel(level);
			} else if(e.getName().equals("hierarchy")) {
				parseHierarchy(dim, e);
			} else {
            	throw new RuntimeException("invalid tag '" + e.getName() +"' level or hierarchy tags only accepted");
            }
		}
		multiDim.addDimension(dim);
	}

	private void parseProperties(Level level, Element levelElem) {
		Iterator<Element> i = levelElem.elementIterator();
		while(i.hasNext()) {
			Element prop = i.next();
			boolean id;
			if (prop.attribute("ID") != null) {
				id = prop.attributeValue("ID").equals("true");
			} else {
				id = false;
			}
			Property property = new Property(prop.getText().replaceAll("\\s",""), prop.attributeValue("type"), id);
			level.addProperty(property);
		}
	}
	
	private void parseHierarchy(Dimension dim, Element h) {
		Hierarchy hierachy = new Hierarchy(h.attributeValue("name"));
		Iterator<Element> i = h.elementIterator();
		while(i.hasNext()) {
			Element l = i.next();
			Level level = new Level(l.attributeValue("name"), Integer.valueOf(l.attributeValue("pos")));
			parseProperties(level, l);
			hierachy.addLevel(level);
		}
		dim.addHierarchy(hierachy);
	}
	
	public static void main(String[] args) throws DocumentException, IOException {
		XmlConverter xml = new XmlConverter();
		MultiDim multiDim = xml.parse(new File("in/in2.xml"));
		xml.generateXml(multiDim, "out/output.xml");
	}
}
