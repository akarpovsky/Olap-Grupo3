package olap.olap.project.xml;

import java.io.File;

import olap.olap.project.model.MultiDim;

public class MultiToGeomondrianTest {

	public static void main(String[] args) throws Exception {
		XmlConverter xmlConverter = new XmlConverter();
		MultiDim multiDim = xmlConverter.parse(new File("in/in2.xml"));
		xmlConverter.generateXml(multiDim, "in/test.xml");
		multiDim.print();
	}
}
