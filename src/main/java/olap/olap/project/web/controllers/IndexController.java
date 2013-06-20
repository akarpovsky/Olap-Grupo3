package olap.olap.project.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import olap.olap.project.model.MultiDim;
import olap.olap.project.model.db.ConnectionManager;
import olap.olap.project.model.db.ConnectionManagerPostgreWithCredentials;
import olap.olap.project.model.db.DBColumn;
import olap.olap.project.model.db.DBTable;
import olap.olap.project.model.db.DBUtils;
import olap.olap.project.web.command.DBCredentialsForm;
import olap.olap.project.web.command.UploadXmlForm;
import olap.olap.project.xml.MultidimCubeToMDXUtils;
import olap.olap.project.xml.XmlConverter;
import olap.olap.project.xml.XmlFormatter;

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
	protected ModelAndView index(final DBCredentialsForm form, HttpServletRequest req)
			throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView("index/index");
		
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
		
		if(connectionManager != null){
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
	protected ModelAndView uploadxml(final UploadXmlForm form, HttpServletRequest req)
			throws ServletException, IOException {
		
		ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
		
		if(connectionManager == null){
			mav.setViewName("redirect:" + req.getServletPath()
					+ "/index/index");
			return mav;
		}
		
		try {
			final Connection conn = connectionManager.getConnectionWithCredentials();
			mav.addObject("dburl", connectionManager.getConnectionString());
		} catch (Exception e) {
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription", "No se ha podido establecer la conexión con la base de datos");
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
				final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.setConnectionManagerWithCredentials(form.getUrl_db(), form.getUser_db(), form.getPassword_db());
				connectionManager.getConnectionWithCredentials();
			} catch (Exception e) {
				mav.addObject("couldNotConnectToDB", true);
				return mav;
			}
		}
		
		mav.setViewName("redirect:" + req.getServletPath()
				+ "/index/uploadxml");
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
			MultipartFile xmlfile = form.getFile();
			File tmpFile = new File(System.getProperty("java.io.tmpdir")
					+ System.getProperty("file.separator")
					+ xmlfile.getOriginalFilename());
			
			xmlfile.transferTo(tmpFile);
			
			FileInputStream inputStream = new FileInputStream(tmpFile);
		    try {
		        String everything = IOUtils.toString(inputStream).toLowerCase();
		        FileOutputStream outputStream = new FileOutputStream(tmpFile);
		        IOUtils.write(everything, outputStream);
		    } finally {
		        inputStream.close();
		    }
			
			XmlConverter parser = new XmlConverter();
			
			final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
			Connection conn;
			try {
				conn = connectionManager.getConnectionWithCredentials();
			} catch (Exception e) {
				errorMav.addObject("errorDescription", "No se ha podido establecer la conexión con la base de datos");
				errorMav.addObject("errorMessage", e.getMessage());
				return errorMav;
			}

			
			MultiDim xmlDocument = parser.parse(tmpFile);
			xmlDocument.print();
			String MDXtables = null;
			try {
				MDXtables = MultidimCubeToMDXUtils.convertToMDXAndCreateSchema(xmlDocument, conn);
			} catch (SQLException e) {
				errorMav.addObject("errorDescription", "No se ha podido crear el esquema en la base de datos provista.");
				errorMav.addObject("errorMessage", e.getMessage());
				errorMav.addObject("errorCode", e.getErrorCode());
				return errorMav;
			}
			ModelAndView mav2 = new ModelAndView("/index/show_tables");
			mav2.addObject("dburl", connectionManager.getConnectionString());
			mav2.addObject("MDXtables", MDXtables.replace("\n", "<br />")
													.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
			
			//Generate MDX xml
			parser.generateXml(xmlDocument, "out/out.xml");
			
			FileInputStream inputStream2 = new FileInputStream("out/out.xml");
			String everything = "";
		    try {
		        everything = IOUtils.toString(inputStream2).toLowerCase();
		    } finally {
		        inputStream.close();
		    }
			mav2.addObject("MDXxml", new XmlFormatter().format(everything));
			return mav2;
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView show_tables(HttpServletRequest req) throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
		
		if(connectionManager == null){
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject("errorMessage", "No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}
		
		try {
			final Connection conn = connectionManager.getConnectionWithCredentials();
			mav.addObject("dburl", connectionManager.getConnectionString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mav;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView select_db_table(HttpServletRequest req, String choiceTable) throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
		
		if(connectionManager == null){
			ModelAndView errorMav = new ModelAndView("error/error");
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject("errorMessage", "No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}
		
		try {
			final Connection conn = connectionManager.getConnectionWithCredentials();
			mav.addObject("currentTable", choiceTable);
			mav.addObject("dburl", connectionManager.getConnectionString());
			mav.addObject("dbtables", DBUtils.getTablesInDB(conn));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mav;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView select_db_column(HttpServletRequest req, String currentTable, String choiceColumn) throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
		ModelAndView errorMav = new ModelAndView("error/error");
		
		if(connectionManager == null){
			errorMav.addObject("errorDescription", "Acceso no autorizado.");
			errorMav.addObject("errorMessage", "No deberia entrar a este sitio sin antes tener abierta la conexion con la base de datos.");
			errorMav.addObject("errorCode", "403");
			return errorMav;
		}
		
		try {
			final Connection conn = connectionManager.getConnectionWithCredentials();
			mav.addObject("currentColumn", choiceColumn);
			mav.addObject("dburl", connectionManager.getConnectionString());
			DBTable table = DBUtils.getTableInDB(conn, currentTable);
			if(table == null){
				errorMav.addObject("errorDescription", "Tabla inválida.");
				errorMav.addObject("errorMessage", "No se encontró la tabla que está buscando.");
				errorMav.addObject("errorCode", "404");
				return errorMav;
			}
			List<DBColumn> columns = table.getColumns();
			mav.addObject("dbcolumns", columns);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mav;
	}

}
