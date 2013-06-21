package olap.olap.project.xml;

import java.util.LinkedList;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import olap.olap.project.model.MultiDim;
import olap.olap.project.model.db.DBColumn;
import olap.olap.project.model.db.DBTable;

public class SchemaTablesUpdater {
	public static List<DBTable> getTables(MultiDim multiDim, String fileNameOut)
			throws IOException, DocumentException {
		List<DBTable> ret = new LinkedList<DBTable>();
		XmlConverter xml = new XmlConverter();
		xml.generateXml(multiDim, fileNameOut);

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(fileNameOut);
			// Get the root element

			// Get the staff element , it may not working if tag has spaces, or
			// whatever weird characters in front...it's better to use
			// getElementsByTagName() to get it directly.
			// Node staff = company.getFirstChild();
			NodeList tables = doc.getElementsByTagName("Table");
			Node fTable = tables.item(0);
			DBTable factTable = new DBTable(fTable.getAttributes()
					.getNamedItem("name").getTextContent(),true);
			NodeList dimensions = doc.getElementsByTagName("Dimension");
			for (int i = 0; i < XmlConverter.foreignks.size(); i++) {
				factTable.addColumn(XmlConverter.foreignks.get(i));
			}
			NodeList measures = doc.getElementsByTagName("Measure");
			for(int i=0; i<measures.getLength();i++){
				Node measure = measures.item(i);
				String name = measure.getAttributes().getNamedItem("column").getNodeValue();
				String type = measure.getAttributes().getNamedItem("datatype").getNodeValue();
				factTable.addColumn(new DBColumn(name, type, false));
			}

			ret.add(factTable);
			for (int i = 0; i < dimensions.getLength(); i++) {
				Node dimension = dimensions.item(i);
				DBTable table = new DBTable(dimension.getAttributes().getNamedItem("name").getNodeValue());
				NodeList hierarchies = dimension.getChildNodes();
				for (int j = 0; j < hierarchies.getLength(); j++) {
					Node hierarchy = hierarchies.item(j);
					NodeList children = hierarchy.getChildNodes();
					for (int l = 0; l < children.getLength(); l++) {
						Node level = children.item(l);
						if (level.getNodeName().equals("Level")) {
							NodeList properties = level.getChildNodes();
							for (int k = 0; k < properties.getLength(); k++) {
								Node property = properties.item(k);
								NamedNodeMap atributes = property.getAttributes();
								String name = atributes.getNamedItem("column").getNodeValue();
								String type = atributes.getNamedItem("type").getNodeValue();
								Boolean isPK = false;
								table.addColumn(new DBColumn(name, type, isPK));
							}
						}
					}

				}

				ret.add(table);
			}

			System.out.println("Done");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}

		return ret;


	}
	
	public static List<DBTable> getTables(String fileNameIn, String fileNameOut)
			throws IOException, DocumentException {
		XmlConverter xml = new XmlConverter();
		FileInputStream inputStream = new FileInputStream(fileNameIn);
		try {
			String everything = IOUtils.toString(inputStream).toLowerCase();
			FileOutputStream outputStream = new FileOutputStream("in/temp.xml");
			IOUtils.write(everything, outputStream);
		} finally {
			inputStream.close();
		}
		MultiDim multiDim = xml.parse(new File("in/temp.xml"));
		return getTables(multiDim,fileNameOut);

	}

	public static void putTables(List<DBTable> newTables, String fileNameIn, String fileNameOut)
			throws IOException, DocumentException {
		XmlConverter xml = new XmlConverter();
		FileInputStream inputStream = new FileInputStream(fileNameIn);
		try {
			String everything = IOUtils.toString(inputStream).toLowerCase();
			FileOutputStream outputStream = new FileOutputStream("in/temp.xml");
			IOUtils.write(everything, outputStream);
		} finally {
			inputStream.close();
		}
		MultiDim multiDim = xml.parse(new File("in/temp.xml"));
		putTables(newTables, multiDim, fileNameOut);

	}
	
	public static void putTables(List<DBTable> newTables, MultiDim multiDim, String fileNameOut)
			throws IOException, DocumentException {
		List<DBTable> ret = new LinkedList<DBTable>();
		XmlConverter xml = new XmlConverter();
		xml.generateXml(multiDim, fileNameOut);

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(fileNameOut);
			// Get the root element

			DBTable factTable = null;
			for(DBTable t:newTables){
				if(t.isFactTable()){
					factTable = t;
					break;
				}
			}
			
			NodeList tables = doc.getElementsByTagName("Table");
			Node fTable = tables.item(0);
			fTable.getAttributes()
					.getNamedItem("name").setTextContent(factTable.getName());
			
			NodeList measures = doc.getElementsByTagName("Measure");
			for(int i=0; i<measures.getLength();i++){
				Node measure = measures.item(i);
				for(DBColumn c: factTable.getColumns()){
					if(c.getOldName().equals(measure.getAttributes().getNamedItem("column").getNodeValue())){
						measure.getAttributes().getNamedItem("column").setNodeValue(c.getName());
						measure.getAttributes().getNamedItem("datatype").setNodeValue(c.getType());
					}
				}
			}
			
			NodeList dimensions = doc.getElementsByTagName("Dimension");


			for (int i = 0; i < dimensions.getLength(); i++) {
				Node dimension = dimensions.item(i);
				for(DBColumn c: factTable.getColumns()){
					if(c.getOldName().equals(dimension.getAttributes().getNamedItem("foreignKey").getNodeValue())){
						dimension.getAttributes().getNamedItem("foreignKey").setNodeValue(c.getName());
					}
				}
				DBTable currentTable = null;
				for(DBTable t: newTables){
					if(t.getOldName().equals(dimension.getAttributes().getNamedItem("name").getNodeValue())){
						currentTable = t;
						break;
					}
				}
				NodeList hierarchies = dimension.getChildNodes();
				for (int j = 0; j < hierarchies.getLength(); j++) {
					Node hierarchy = hierarchies.item(j);
					for(DBColumn c: factTable.getColumns()){
						if(c.getOldName().equals(hierarchy.getAttributes().getNamedItem("primaryKey").getNodeValue())){
							hierarchy.getAttributes().getNamedItem("primaryKey").setNodeValue(c.getName());
						}
					}
					NodeList children = hierarchy.getChildNodes();
					for (int l = 0; l < children.getLength(); l++) {
						Node level = children.item(l);
						if (level.getNodeName().equals("Level")) {
							for(DBColumn c:currentTable.getColumns()){
								if(c.getOldName().equals(level.getAttributes().getNamedItem("column").getNodeValue())){
									level.getAttributes().getNamedItem("column").setNodeValue(c.getName());
									level.getAttributes().getNamedItem("type").setNodeValue(c.getType());
									break;
								}
							}
							NodeList properties = level.getChildNodes();
							for (int k = 0; k < properties.getLength(); k++) {
								DBColumn currentColumn = null;
								
								Node property = properties.item(k);
								NamedNodeMap atributes = property.getAttributes();
								for(DBColumn c:currentTable.getColumns()){
									if(c.getOldName().equals(atributes.getNamedItem("column").getNodeValue())){
										atributes.getNamedItem("column").setNodeValue(c.getName());
										atributes.getNamedItem("type").setNodeValue(c.getType());
										break;
									}
								}
							}
						}else if(level.getNodeName().equals("Table")){
							level.getAttributes().getNamedItem("name").setNodeValue(currentTable.getName());
						}
					}

				}
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileNameOut));
			transformer.transform(source, result);
			
			System.out.println("Done");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		FileInputStream inputStream2 = new FileInputStream(
				fileNameOut);
		String everything = "";
		String everythingPretty = "";
		try {
			everything = IOUtils.toString(inputStream2);
		} finally {
			inputStream2.close();
		}
		try {
			everythingPretty = xml.getTransformedHtml(everything);
			File write_file = new File(fileNameOut);
			FileOutputStream fileOut = new FileOutputStream(write_file);
			fileOut.write(everythingPretty.getBytes());
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws IOException,
			DocumentException {
		List<DBTable> tables = getTables("in/in2.xml","out/out.xml");
		for (DBTable table : tables) {
			System.out.println("table:" + table.getName());
			table.update(table.getName() + "_nueva");
			for (DBColumn col : table.getColumns()) {
				System.out.println(col.getName() + " " + col.getType() + " "+col.isPK());
				col.update(col.getName()+"_nuevo");
				col.setType("Boolean");
			}
			System.out.println("");
		}
		putTables(tables, "in/in2.xml","out/out.xml");
//		tables = getTables("in/in2.xml");
//		for (DBTable table : tables) {
//			System.out.println("table:" + table.getName());
//			for (DBColumn col : table.getColumns()) {
//				System.out.println(col.getName() + " " + col.getType() + " "+col.isPK());
//			}
//			System.out.println("");
//		}
	}
}
