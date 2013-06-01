package olap.olap.project.xml;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map.Entry;

import olap.olap.project.model.Cube;
import olap.olap.project.model.Dimension;
import olap.olap.project.model.Hierarchy;
import olap.olap.project.model.Level;
import olap.olap.project.model.Measure;
import olap.olap.project.model.MultiDim;
import olap.olap.project.model.Property;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class MultidimCubeToMDXUtils {
	
	
	/**
	 * Converts a MultiDim to MDX
	 */
	public static String convertToMDX(MultiDim multidim) throws IOException {
		Document out = DocumentHelper.createDocument();

		Connection connection = null;
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
		sb.append("\n\t/* Primary keys*/\n\n");
		for(Entry<String,Dimension> entry : cube.getDimensions().entrySet()){
			Level l = entry.getValue().getLevel();
			for(Property p : l.getProperties()){
				if(p.isPK()){
					pks.append( "\"" + entry.getKey() + "_" + p.getName() + "\"" +",");
				}
				sb.append("\t\""+ entry.getKey() + "_" + p.getName() + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + " NOT NULL,\n");
			}
				
		}
		
		// Borro ultima coma
		pks.deleteCharAt(pks.length()-1);
		sb.append("\n\tPRIMARY KEY(" + pks + ")\n");
		
		
	}

//	PRIMARY KEY(question_id, tag_id)
	
	private static void createDimensionTable(StringBuilder sb, String dimensionName, Dimension dimension) {
		// Se asume que la tabla no es vacia!
		
		sb.append("CREATE TABLE " + dimensionName + "_" + dimension.getName() + " (\n");
		
		// Creo los campos para el nivel inicial
		createDimensionBasicFields(sb, dimension.getLevel());

		
		for(Hierarchy h: dimension.getHierarchies()){
			createDimensionFields(sb, h);
		}
		
		// Borro ultima coma
		sb.deleteCharAt(sb.length()-2);

		
		sb.append("\n);\n");
		System.out.println(sb.toString());
	}

	private static void createDimensionBasicFields(StringBuilder sb, Level l) {
		for(Property p : l.getProperties()){
			sb.append("\t\""+ l.getName() + "_" + p.getName() + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + ",\n");
		}		
	}

	private static void createDimensionFields(StringBuilder sb, Hierarchy h) {
		for(Level l: h.getLevels()){
			for(Property p : l.getProperties()){
				sb.append("\t\""+ l.getName() + "_" + p.getName() + "\" " + (p.getType().equals("string") ? "char[]":p.getType()) + ",\n");
			}
		}
	}
	

}
