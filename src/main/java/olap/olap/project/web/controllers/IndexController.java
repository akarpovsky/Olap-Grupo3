package olap.olap.project.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import olap.olap.project.model.MultiDim;
import olap.olap.project.model.db.ConnectionManager;
import olap.olap.project.model.db.ConnectionManagerPostgreWithCredentials;
import olap.olap.project.model.db.DBColumn;
import olap.olap.project.model.db.DBTable;
import olap.olap.project.model.db.DBUtils;
import olap.olap.project.web.command.DBCredentialsForm;
import olap.olap.project.web.command.TableSelectForm;
import olap.olap.project.web.command.TableSelectionForm;
import olap.olap.project.web.command.UploadXmlForm;
import olap.olap.project.xml.MultidimCubeToMDXUtils;
import olap.olap.project.xml.SchemaTablesUpdater;
import olap.olap.project.xml.XmlConverter;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class IndexController {

	public IndexController() {
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView index(final DBCredentialsForm form,
			HttpServletRequest req) throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView("index/index");

		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
				.getConnectionManagerWithCredentials();

		if (connectionManager != null) {
			try {
				connectionManager.getConnectionWithCredentials();
			} catch (SQLException e) {
				mav.addObject("dbcredentialsform", form);
				return mav;
			}
			mav.setViewName("redirect:" + req.getServletPath()
					+ "/index/uploadxml");
			mav.addObject("uploadxmlform", new UploadXmlForm());
			return mav;
		}

		mav.addObject("dbcredentialsform", form);
		return mav;
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView uploadxml(final UploadXmlForm form,
			HttpServletRequest req) throws ServletException, IOException {

		ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
				.getConnectionManagerWithCredentials();

		if (connectionManager == null) {
			mav.setViewName("redirect:" + req.getServletPath() + "/index/index");
			return mav;
		}

		try {
			final Connection conn = connectionManager
					.getConnectionWithCredentials();
			mav.addObject("dburl", connectionManager.getConnectionString());
		} catch (Exception e) {
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription",
					"No se ha podido establecer la conexión con la base de datos");
			errorMav.addObject("errorMessage", e.getMessage());
			return errorMav;
		}

		mav.addObject("uploadxmlform", form);
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView connectToDB(final HttpServletRequest req,
			final DBCredentialsForm form, Errors errors) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("index/index");
		mav.addObject("dbcredentialsform", form);
		if (form.getUrl_db() == null) {
			errors.rejectValue("empty", "url_db");
			return mav;
		} else if (form.getUser_db() == null) {
			errors.rejectValue("empty", "user_db");
			return mav;

		} else if (form.getPassword_db() == null) {
			errors.rejectValue("empty", "password_db");
			return mav;
		} else {
			try {
				final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
						.setConnectionManagerWithCredentials(form.getUrl_db(),
								form.getUser_db(), form.getPassword_db());
				connectionManager.getConnectionWithCredentials();
			} catch (Exception e) {
				mav.addObject("couldNotConnectToDB", true);
				return mav;
			}
		}

		mav.setViewName("redirect:" + req.getServletPath() + "/index/uploadxml");
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView uploadxml(final HttpServletRequest req,
			final UploadXmlForm form, Errors errors) throws DocumentException,
			IOException {
		ModelAndView mav = new ModelAndView();
		ModelAndView errorMav = new ModelAndView("error/error");

		mav.addObject("uploadxmlform", form);
		if (form.getFile() == null) {
			errors.rejectValue("empty", "file");
			return mav;
		} else {

			XmlConverter parser = new XmlConverter();

			final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
					.getConnectionManagerWithCredentials();
			Connection conn;
			try {
				conn = connectionManager.getConnectionWithCredentials();
			} catch (Exception e) {
				errorMav.addObject("errorDescription",
						"No se ha podido establecer la conexión con la base de datos");
				errorMav.addObject("errorMessage", e.getMessage());
				return errorMav;
			}

			ModelAndView mav2;

			MultipartFile xmlfile = form.getFile();
			File tmpFile = new File(System.getProperty("java.io.tmpdir")
					+ System.getProperty("file.separator")
					+ xmlfile.getOriginalFilename());

			xmlfile.transferTo(tmpFile);

			FileInputStream inputStream = new FileInputStream(tmpFile);
			try {
				String everything = IOUtils.toString(inputStream)
						.toLowerCase();
				FileOutputStream outputStream = new FileOutputStream(
						tmpFile);
				IOUtils.write(everything, outputStream);
			} finally {
				inputStream.close();
			}
			
			MultiDim xmlDocument = parser.parse(tmpFile);
			req.getSession().setAttribute("originalMultidim", xmlDocument);

			if (!form.getManualDataSelection()) { // Automatic execution

				mav2 = new ModelAndView("/index/show_tables");
				mav2.addObject("dburl", connectionManager.getConnectionString());

				boolean dbError = false;
				xmlDocument.print();
				String MDXtables = null;
				try {
					MDXtables = MultidimCubeToMDXUtils
							.convertToMDXAndCreateSchema(xmlDocument, conn);
				} catch (SQLException e) {
					dbError = true;
					mav2.addObject("errorDescription",
							"No se ha podido crear el esquema en la base de datos provista.");
					mav2.addObject("errorMessage", e.getMessage());
					mav2.addObject("errorCode", e.getErrorCode());
				}

				mav2.addObject("dbError", dbError);

				if (!dbError) {
					mav2.addObject(
							"MDXtables",
							MDXtables.replace("\n", "<br />").replace("\t",
									"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
				}
				// Generate MDX xml
				parser.generateXml(xmlDocument, "out/out.xml");

				FileInputStream inputStream2 = new FileInputStream(
						"out/out.xml");
				String everything = "";
				String everythingPretty = "";
				try {
					everything = IOUtils.toString(inputStream2);
				} finally {
					inputStream.close();
				}
				try {
					everythingPretty = parser.getTransformedHtml(everything);
					File write_file = new File("out/out.xml");
					FileOutputStream fileOut = new FileOutputStream(write_file);
					fileOut.write(everythingPretty.getBytes());
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				mav2.addObject("MDXxml", everythingPretty);

			} else { // Manual execution
				
				List<DBTable> userSelectedTablesList = SchemaTablesUpdater.getTables(xmlDocument, "out/out.xml");
				
				mav2 = new ModelAndView("/index/select_db_table");
				mav2.addObject("dburl", connectionManager.getConnectionString());

				List<DBTable> existingDBTablesList = new ArrayList<DBTable>();

				
				List<DBTable> userSelectedFieldList = userSelectedTablesList;
				
				try {
					existingDBTablesList = DBUtils.getTablesInDB(conn);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				TableSelectForm f = new TableSelectForm();
				mav2.addObject("existingDBTablesList", existingDBTablesList);
				mav2.addObject("userSelectedFieldList", userSelectedFieldList);
				mav2.addObject("tableSelectForm", f);
				req.getSession().setAttribute("originalXMLDataList", userSelectedTablesList);
			}

			return mav2;
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView select_db_table(TableSelectForm form,
			final HttpServletRequest req) {
		for (Entry<String, String> entry : form.getTablesMap().entrySet()) {
			System.out.println(entry.getKey() + ", " + entry.getValue());
		}

		ModelAndView mav = new ModelAndView();

		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
				.getConnectionManagerWithCredentials();
		ModelAndView errorMav = new ModelAndView("error/error");

		if (connectionManager == null) {
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject(
					"errorMessage",
					"No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}

		try {
			mav = new ModelAndView("/index/select_db_column");
			final Connection conn = connectionManager
					.getConnectionWithCredentials();
			mav.addObject("dburl", connectionManager.getConnectionString());
			
			Map<String, Map<String, List<DBColumn>>> userFieldToDBFieldMap = new HashMap<String, Map<String, List<DBColumn>>>();
			
			List<DBTable> userSelectedTablesList = (List<DBTable>) req.getSession().getAttribute("originalXMLDataList");
			
			Map<String, String> tablesMap = form.getTablesMap();
			
			// Update selected tables in session object
			for(DBTable t: userSelectedTablesList){
				t.update(tablesMap.get(t.getName()));
			}
			
			for(DBTable t: userSelectedTablesList){
				Map<String, List<DBColumn>> tableDBFieldsMap = new HashMap<String, List<DBColumn>>();
				DBTable table = DBUtils.getTableInDB(conn, t.getName());
				if (table == null) {
					errorMav.addObject("errorDescription", "Tabla inválida.");
					errorMav.addObject("errorMessage",
							"No se encontró la tabla que está buscando.");
					errorMav.addObject("errorCode", "404");
					return errorMav;
				}
				List<DBColumn> columns = table.getColumns();
				List<DBColumn> currentTableFields = columns;
				
				for (DBColumn dbcol : t.getColumns()) {
					tableDBFieldsMap.put(dbcol.getName(), columns);
				}
				userFieldToDBFieldMap.put(t.getOldName(), tableDBFieldsMap);
			}
			
			
			TableSelectForm f = new TableSelectForm();
			mav.addObject("tableSelectForm", f);
			mav.addObject("userFieldToDBFieldMap", userFieldToDBFieldMap);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mav;
	}
	
	private void printDBTableList(List<DBTable> dbtable){
		for(DBTable t: dbtable){
			System.out.println("(" +dbtable.indexOf(t)+ ") " + "DBTable: old == " + t.getOldName() + "  || new ==" + t.getName());
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView select_db_column(TableSelectForm form,
			final HttpServletRequest req) throws Exception {

		ModelAndView mav = new ModelAndView("/index/show_manual_mdx");
		
		List<DBTable> userSelectedTablesList = (List<DBTable>) req.getSession().getAttribute("originalXMLDataList");
		
		Map<String, String> tablesMap = form.getTablesMap();
		
		for(Entry<String, String> t1: tablesMap.entrySet()){
			for(DBTable t: userSelectedTablesList){
				if(t.getOldName().equals(t1.getKey().split(":")[0])){
					t.updateColumn(t1.getKey().split(":")[1],t1.getValue());
				}
			}
		}
		
		try {
			SchemaTablesUpdater.putTables(userSelectedTablesList, (MultiDim) req.getSession().getAttribute("originalMultidim"), "out/out.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		XmlConverter xml = new XmlConverter();
		FileInputStream inputStream = new FileInputStream("out/out.xml");
		try {
			String everything = IOUtils.toString(inputStream);
			try {
				mav.addObject("MDXxml", xml.getTransformedHtml(everything));
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		} finally {
			inputStream.close();
		}
		
		return mav;
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView show_tables(HttpServletRequest req)
			throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
				.getConnectionManagerWithCredentials();

		if (connectionManager == null) {
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject(
					"errorMessage",
					"No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}

		try {
			final Connection conn = connectionManager
					.getConnectionWithCredentials();
			mav.addObject("dburl", connectionManager.getConnectionString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mav;
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView select_db_table(HttpServletRequest req,
			TableSelectionForm form) throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
				.getConnectionManagerWithCredentials();

		if (connectionManager == null) {
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject(
					"errorMessage",
					"No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}

		return mav;
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView getOutputXML(HttpServletRequest req,
			HttpServletResponse response) throws IOException {
		
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials
				.getConnectionManagerWithCredentials();

		if (connectionManager == null) {
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject(
					"errorMessage",
					"No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}
		
		String file = "out.xml";
		response.setHeader("Content-Disposition", "attachment;filename=" + file);
		response.setContentType("text/plain");

		File down_file = new File("out/" + file);
		FileInputStream fileIn = new FileInputStream(down_file);
		
		IOUtils.copy(fileIn, response.getOutputStream());
		
		response.flushBuffer();
		return null;
	}
}
