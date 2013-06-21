package olap.olap.project.xml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.dom4j.DocumentException;

import olap.olap.project.model.Cube;
import olap.olap.project.model.Dimension;
import olap.olap.project.model.Hierarchy;
import olap.olap.project.model.Level;
import olap.olap.project.model.Measure;
import olap.olap.project.model.MultiDim;
import olap.olap.project.model.Property;

public class MultidimCubeToMDXUtils {
	
	/**
	 * Creates the schema in the DB with the user provided connection
	 */
	public static String convertToMDXAndCreateSchema(MultiDim multidim, Connection conn) throws SQLException{
		if(conn == null){
        	throw new RuntimeException("Invalid or null connection provided.");
		}
		String tablesCreation = convertToMDX(multidim);
		PreparedStatement statement = conn.prepareStatement(tablesCreation);
		statement.execute();
		return tablesCreation;
		
	}
	
	/**
	 * Converts a MultiDim to MDX
	 */
	public static String convertToMDX(MultiDim multidim) {
		StringBuilder sb = new StringBuilder();

		System.out.println("\n*** Creación de sentencias MDX *** ");
		/* Itarate over dimensions and create Dimension Tables */
		for(Entry<String,Dimension> entry: multidim.getCube().getDimensions().entrySet()){
			createDimensionTable(sb, entry.getKey(), entry.getValue());
		}
		
		createFactTable(sb, multidim.getCube());
		
		return sb.toString().toLowerCase();
		
	}
	
	private static void createFactTable(StringBuilder sb, Cube cube) {
		sb.append("CREATE TABLE " + cube.getName() + "_fact (\n");
		sb.append("\n\t/* Measures*/\n\n");
		for(Measure m: cube.getMeasures()){
			sb.append("\t\""+ m.getName() + "\" " + (m.getType().equals("string") ? "char[]":m.getType()) + ",\n");
		}
		
		createFactTablePrimaryKeys(sb, cube);
		sb.append("\n);\n");

	}
	
	private static void createFactTablePrimaryKeys(StringBuilder sb, Cube cube) {
		StringBuilder pks = new StringBuilder();
		StringBuilder fks = new StringBuilder();
		sb.append("\n\t/* Primary keys*/\n\n");
		for(Entry<String,Dimension> entry : cube.getDimensions().entrySet()){
			StringBuilder fksLevel = new StringBuilder();
			StringBuilder fksLevelReferences = new StringBuilder();
			Level l = entry.getValue().getLevel();
			for(Property p : l.getProperties()){
				if(p.isPK()){
					pks.append( "\"" + p.getName() + "_" + entry.getKey() + "_" + l.getName() + "\"" +",");
					fksLevel.append( p.getName() + "_" + entry.getKey() + "_" + l.getName() +",");
					fksLevelReferences.append(p.getName() + "_" + entry.getKey() + "_" + l.getName() + ",");
				}
				sb.append("\t\""+ p.getName() + "_" + entry.getKey() + "_" + l.getName()  + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + " NOT NULL,\n");
			}
			
			if(fksLevel.length() > 0){
				fksLevel.deleteCharAt(fksLevel.length()-1);
			}
			
			if(fksLevelReferences.length() > 0){
				fksLevelReferences.deleteCharAt(fksLevelReferences.length()-1);
			}
			
			fks.append("\tFOREIGN KEY (" + fksLevel +") REFERENCES " + entry.getKey() + "_" + l.getName() + "(" + fksLevelReferences +"),\n");
				
		}
		
		// Borro ultima coma
		if(pks.length() > 0){
			pks.deleteCharAt(pks.length()-1);
			sb.append("\n\tPRIMARY KEY(" + pks + "),\n");
		}
		
		if(fks.length() > 0){
			fks.deleteCharAt(fks.length()-2);
			sb.append(fks+"\n");
		}
		
	}

	private static void createDimensionTable(StringBuilder sb, String dimensionName, Dimension dimension) {
		// Se asume que la tabla no es vacia!
		
		sb.append("CREATE TABLE " + dimensionName + "_" + dimension.getName() + " (\n");
		
		// Creo los campos para el nivel inicial
		String pks = createDimensionBasicFields(sb, dimension.getLevel(), dimensionName + "_" + dimension.getName());

		
		for(Hierarchy h: dimension.getHierarchies()){
			createDimensionFields(sb, h, dimensionName + "_" + dimension.getName());
		}
		
		// Borro ultima coma
//		sb.deleteCharAt(sb.length()-2);

		sb.append(pks);
		
		sb.append("\n);\n");
	}

	private static String createDimensionBasicFields(StringBuilder sb, Level l, String dimensionFullName) {
		StringBuilder pks = new StringBuilder();
		for(Property p : l.getProperties()){
			if(p.isPK()){
				sb.append("\t\""+ p.getName() + "_"  + dimensionFullName + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + ",\n");
				pks.append( "\"" + p.getName() + "_"  + dimensionFullName  + "\"" +",");
			}else{
				sb.append("\t\""+  p.getName() + "_" + dimensionFullName  + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + ",\n");
			}
		}	
		
		String primaryKeysSentence = null;
		// Borro ultima coma
		if(pks.length() > 0){
			pks.deleteCharAt(pks.length()-1);
			primaryKeysSentence = new String("\n\tPRIMARY KEY(" + pks + ")\n");
		}
		
		return primaryKeysSentence;
	}

	private static void createDimensionFields(StringBuilder sb, Hierarchy h, String dimensionFullName) {
		for(Level l: h.getLevels()){
			for(Property p : l.getProperties()){
				sb.append("\t\""+ l.getName() + "_" + p.getName() + "_" + dimensionFullName + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + ",\n");
			}
		}
	}
	
	public static void main(String[] args) throws DocumentException,
	IOException {
		XmlConverter xml = new XmlConverter();
		MultiDim multiDim = xml.parse(new File("in/transform.xslt"));
		MultidimCubeToMDXUtils.convertToMDX(multiDim);
}

}
