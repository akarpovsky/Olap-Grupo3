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
	public static List<DBTable> getTables(String fileNameIn)
			throws IOException, DocumentException {
		List<DBTable> ret = new LinkedList<DBTable>();
		XmlConverter xml = new XmlConverter();
		String fileName = "out/out.xml";
		FileInputStream inputStream = new FileInputStream(fileNameIn);
		try {
			String everything = IOUtils.toString(inputStream).toLowerCase();
			FileOutputStream outputStream = new FileOutputStream("in/temp.xml");
			IOUtils.write(everything, outputStream);
		} finally {
			inputStream.close();
		}
		MultiDim multiDim = xml.parse(new File("in/temp.xml"));
		xml.generateXml(multiDim, fileName);

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(fileName);
			// Get the root element

			// Get the staff element , it may not working if tag has spaces, or
			// whatever weird characters in front...it's better to use
			// getElementsByTagName() to get it directly.
			// Node staff = company.getFirstChild();
			NodeList tables = doc.getElementsByTagName("Table");
			Node fTable = tables.item(0);
			DBTable factTable = new DBTable(fTable.getAttributes()
					.getNamedItem("name").getTextContent());
			NodeList dimensions = doc.getElementsByTagName("Dimension");
			for (int i = 0; i < XmlConverter.foreignks.size(); i++) {
				factTable.addColumn(XmlConverter.foreignks.get(i));
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

	public static void main(String[] args) throws IOException,
			DocumentException {
		List<DBTable> tables = getTables("in/in2.xml");
		for (DBTable table : tables) {
			System.out.println("table:" + table.getName());
			for (DBColumn col : table.getColumns()) {
				System.out.println(col.getName() + " " + col.getType() + " "+col.isPK());
			}
			System.out.println("");
		}
	}
}
