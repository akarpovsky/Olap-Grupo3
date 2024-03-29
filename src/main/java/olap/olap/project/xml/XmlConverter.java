package olap.olap.project.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import olap.olap.project.model.Attribute;
import olap.olap.project.model.Cube;
import olap.olap.project.model.Dimension;
import olap.olap.project.model.Hierarchy;
import olap.olap.project.model.Level;
import olap.olap.project.model.Measure;
import olap.olap.project.model.MultiDim;
import olap.olap.project.model.Property;
import olap.olap.project.model.db.DBColumn;

import org.apache.commons.io.IOUtils;
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
	public static List<DBColumn> foreignks = new LinkedList<DBColumn>();

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
			} else if (e.getName().equals("dimension")) {
				parseDimension(multiDim, e);
			} else {
				throw new RuntimeException("invalid " + e.getName()
						+ " dimension or cube tags only accepted");
			}
		}
		parseCube(multiDim, cubeElement);
		// multiDim.print();
		return multiDim;
	}

	/**
	 * Converts a MultiDim to a GeoMondrian XML
	 */
	public void generateXml(MultiDim multiDim, String fileName)
			throws IOException {
		foreignks = new LinkedList<DBColumn>();
		Document out = DocumentHelper.createDocument();

		Element schema = out.addElement("Schema");

		Element cubeElem = addCubeElementToXml(multiDim, schema);

		addFactTableElementToXml(multiDim, cubeElem);

		addDimensionsToXml(multiDim, cubeElem);

		// Element tableElem = cubeElem.addElement("Relation");
		// Element table = cubeElem.addElement("Table");

		addMeasuresToXml(multiDim, cubeElem);
		// List<DBTable> tables = getTables(out);
		XMLWriter writer = new XMLWriter(new FileWriter(fileName));
		writer.write(out);
		writer.close();
	}

	private Element addCubeElementToXml(MultiDim multiDim, Element schema) {
		schema.addAttribute("name", multiDim.getCube().getName());
		Element cubeElem = schema.addElement("Cube");
		Cube cube = multiDim.getCube();
		cubeElem.addAttribute("name", cube.getName());
		cubeElem.addAttribute("cache", "true");
		cubeElem.addAttribute("enabled", "true");
		return cubeElem;
	}

	private void addFactTableElementToXml(MultiDim multiDim, Element cubeElem) {
		Element factTable = cubeElem.addElement("Table");
		factTable.addAttribute("name", multiDim.getCube().getName() + "_"
				+ "fact");
	}

	private void addDimensionsToXml(MultiDim multiDim, Element cubeElem) {
		for (Entry<String, Dimension> entry : multiDim.getCube()
				.getDimensions().entrySet()) {
			String pk = "";
			String pkType = "";
			String dimName = entry.getKey() + "_" + entry.getValue().getName();
			Element dim = cubeElem.addElement("Dimension");
			Dimension dimension = entry.getValue();
			dim.addAttribute("name", dimName);
			for (Property p : dimension.getLevel().getProperties()) {
				if (p.isPK()) {
					pk = p.getName() + "_" + dimName;
					dim.addAttribute("foreignKey", pk);
					pkType = Attribute.valueOf(p.getType().toUpperCase())
							.toString();
					foreignks.add(new DBColumn(pk, pkType, p.isPK()));
					break; // Lo hace sólo para el primero, si es compuesta se
							// debe cambiar.
				}
			}

			addHierarchiesToXml(dimension, dim, pk, dimName, pkType);

		}
	}

	private void addHierarchiesToXml(Dimension dimension, Element dim,
			String pk, String dimName, String pkType) {

		// caso especial
		if (dimension.getHierarchies() == null
				|| dimension.getHierarchies().isEmpty()) {
			fixEmptyHierarchyCase(dim, pk, dimName, pkType);
		}

		for (Hierarchy h : dimension.getHierarchies()) {
			handleHierarchy(dim, h, pk, pkType, dimName);
		}
	}

	private void fixEmptyHierarchyCase(Element dim, String pk, String dimName,
			String pkType) {
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

	private void addMeasuresToXml(MultiDim multiDim, Element cubeElem) {
		for (Measure m : multiDim.getCube().getMeasures()) {
			Element measure = cubeElem.addElement("Measure");
			String aggName = m.getAgg();
			// if (aggName.equals("st_union"))
			// aggName = "sum";
			measure.addAttribute("aggregator", aggName);
			measure.addAttribute("name", m.getName());
			measure.addAttribute("column", m.getName());
			measure.addAttribute("datatype",
					Attribute.valueOf(m.getType().toUpperCase()).toString());
		}
	}

	private void handleHierarchy(Element dim, Hierarchy hierarchy, String pk,
			String pktype, String dimName) {
		Element h = dim.addElement("Hierarchy");
		h.addAttribute("name", hierarchy.getName());
		h.addAttribute("hasAll", "true");
		h.addAttribute("allMemberName", "All" + hierarchy.getName());
		h.addAttribute("primaryKey", pk);
		h.addElement("Table").addAttribute("name", dimName);
		for (Level l : hierarchy.getLevels()) {
			handleLevel(h, l, dimName);
		}
	}

	private void handleLevel(Element hierarchy, Level l, String dimName) {
		Element level = hierarchy.addElement("Level");
		level.addAttribute("name", l.getName());
		level.addAttribute("uniqueMembers", "false");
		level.addAttribute("levelType", "Regular");
		level.addAttribute("hideMemberIf", "Never");
		for (Property p : l.getProperties()) {
			if (p.isPK()) {
				level.addAttribute("column", l.getName() + "_" + p.getName()
						+ "_" + dimName);
				level.addAttribute("type",
						Attribute.valueOf(p.getType().toUpperCase()).toString());
			}
			Element prop = level.addElement("Property");
			prop.addAttribute("name", l.getName() + "_" + p.getName());
			prop.addAttribute("column", l.getName() + "_" + p.getName() + "_"
					+ dimName);
			prop.addAttribute("type",
					Attribute.valueOf(p.getType().toUpperCase()).toString());
		}
	}

	private void parseCube(MultiDim multiDim, Element c) {
		Cube cube = new Cube(c.attributeValue("name"));
		Iterator<Element> i = c.elementIterator();
		while (i.hasNext()) {
			Element e = i.next();
			if (e.getName().equals("measure")) {
				cube.addMeasure(new Measure(e.attributeValue("name"), e
						.attributeValue("type"), e.attributeValue("agg")));
			} else if (e.getName().equals("dimension")) {
				String ptr = e.attributeValue("ptr");
				Dimension dim = multiDim.getDimension(ptr);
				if (dim == null) {
					throw new RuntimeException("No dimension " + ptr
							+ " was found");
				}
				cube.addDimension(e.attributeValue("name"), dim);
			} else {
				throw new RuntimeException("invalid " + e.getName()
						+ " measure or dimension tags only accepted");
			}
		}
		multiDim.setCube(cube);
	}

	private void parseDimension(MultiDim multiDim, Element dimension) {
		Dimension dim = new Dimension(dimension.attributeValue("name"));
		Iterator<Element> i = dimension.elementIterator();
		while (i.hasNext()) {
			Element e = i.next();
			if (e.getName().equals("level")) {
				Level level = new Level(dim.getName(), 0);
				parseProperties(level, e);
				dim.setLevel(level);
			} else if (e.getName().equals("hierarchy")) {
				parseHierarchy(dim, e);
			} else {
				throw new RuntimeException("invalid tag '" + e.getName()
						+ "' level or hierarchy tags only accepted");
			}
		}
		multiDim.addDimension(dim);
	}

	private void parseProperties(Level level, Element levelElem) {
		Iterator<Element> i = levelElem.elementIterator();
		while (i.hasNext()) {
			Element prop = i.next();
			boolean id;
			if (prop.attribute("id") != null) {
				id = prop.attributeValue("id").equals("true");
			} else {
				id = false;
			}
			Property property = new Property(prop.getText().replaceAll("\\s",
					""), prop.attributeValue("type"), id);
			level.addProperty(property);
		}
	}

	private void parseHierarchy(Dimension dim, Element h) {
		Hierarchy hierachy = new Hierarchy(h.attributeValue("name"));
		Iterator<Element> i = h.elementIterator();
		while (i.hasNext()) {
			Element l = i.next();
			Level level = new Level(l.attributeValue("name"), Integer.valueOf(l
					.attributeValue("pos")));
			parseProperties(level, l);
			hierachy.addLevel(level);
		}
		dim.addHierarchy(hierachy);
	}

	public String getTransformedHtml(String xml) throws TransformerException {
//		URL url = Thread.currentThread().getContextClassLoader().getResource("in/transform.xslt");
//		String path = new File(".").getAbsolutePath() + "/src/main/resources/in/transform.xslt"; 
		InputStream path = getClass().getClassLoader().getResourceAsStream("in/transform.xslt");
		
		byte[] xsl = getStringFromFile(InputStreamAFile(path))
				.getBytes();
		return getTransformedHtml(xml.getBytes(), xsl);
	}

	public File InputStreamAFile(InputStream entrada) {
		try {
			File f = File.createTempFile("temp","file");// Aqui le dan el nombre y/o con la
											// ruta del archivo salida
			OutputStream salida = new FileOutputStream(f);
			byte[] buf = new byte[1024];// Actualizado me olvide del 1024
			int len;
			while ((len = entrada.read(buf)) > 0) {
				salida.write(buf, 0, len);
			}
			salida.close();
			entrada.close();
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getTransformedHtml(byte[] xml, byte[] xsl)
			throws TransformerException {
		Source srcXml = new StreamSource(new ByteArrayInputStream(xml));
		Source srcXsl = new StreamSource(new ByteArrayInputStream(xsl));
		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(srcXsl);
		transformer.transform(srcXml, result);
		return writer.toString();
	}

	private static String getStringFromFile(File f) {
		StringBuilder sb = new StringBuilder(1000);
		try {
			Scanner sc = new Scanner(f);
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void main(String[] args) throws DocumentException,
			IOException {
		XmlConverter xml = new XmlConverter();

		FileInputStream inputStream = new FileInputStream("in/in2.xml");
		try {
			String everything = IOUtils.toString(inputStream).toLowerCase();
			FileOutputStream outputStream = new FileOutputStream("in/temp.xml");
			IOUtils.write(everything, outputStream);
		} finally {
			inputStream.close();
		}
		MultiDim multiDim = xml.parse(new File("in/temp.xml"));
		xml.generateXml(multiDim, "out/output.xml");

		// FileInputStream inputStream = new FileInputStream("out/out.xml");
		// try {
		// String everything = IOUtils.toString(inputStream).toLowerCase();
		// try {
		// } catch (TransformerException e) {
		// e.printStackTrace();
		// }
		// } finally {
		// inputStream.close();
		// }

	}
}
