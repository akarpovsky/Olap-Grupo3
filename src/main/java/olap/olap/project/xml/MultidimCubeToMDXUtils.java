package olap.olap.project.xml;

import java.io.IOException;
import java.sql.Connection;

import olap.olap.project.model.Dimension;
import olap.olap.project.model.Hierarchy;
import olap.olap.project.model.Level;
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
		for(Dimension dimension : multidim.getDimensions()){
			createDimensionTable(sb, dimension);
		}
		
		return sb.toString();
		
	}

	private static void createDimensionTable(StringBuilder sb, Dimension dimension) {
		// Se asume que la tabla no es vacia!
		
		sb.append("CREATE TABLE " + dimension.getName() + " (\n");
		
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
