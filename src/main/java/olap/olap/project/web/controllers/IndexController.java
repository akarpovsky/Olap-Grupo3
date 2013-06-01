package olap.olap.project.web.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import olap.olap.project.model.MultiDim;
import olap.olap.project.model.db.ConnectionManager;
import olap.olap.project.model.db.ConnectionManagerPostgreWithCredentials;
import olap.olap.project.model.db.TableCreator;
import olap.olap.project.web.command.DBCredentialsForm;
import olap.olap.project.web.command.UploadXmlForm;
import olap.olap.project.xml.MultidimCubeToMDXUtils;
import olap.olap.project.xml.XmlConverter;

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
	protected ModelAndView olap(final DBCredentialsForm form)
			throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView("index/index");
		mav.addObject("dbcredentialsform", form);
		return mav;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView uploadxml(final UploadXmlForm form, HttpServletRequest req)
			throws ServletException, IOException {
		
		ModelAndView mav = new ModelAndView();
		final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
		
		if(connectionManager == null){
			System.out.println("aca1");
			mav.setViewName("index/olap");
			return mav;
		}
		
		try {
			final Connection conn = connectionManager.getConnectionWithCredentials();
			mav.addObject("dburl", connectionManager.getConnectionString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mav.setViewName("index/uploadxml");
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
				TableCreator tc = new TableCreator(form.getUrl_db(), form.getUser_db(), form.getPassword_db());
			} catch (Exception e) {
				mav.addObject("couldNotConnectToDB", true);
				return mav;
			}
		}
		
		mav.setViewName("redirect:" + req.getServletPath()
				+ "/index/uploadxml");
		System.out.println("----------redirecting !!!!");
		return mav;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView uploadXml(final HttpServletRequest req,
			final UploadXmlForm form, Errors errors) throws DocumentException,
			IOException {
		ModelAndView mav = new ModelAndView();
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
			XmlConverter parser = new XmlConverter();
			
			//TODO remove - for parsing
			FileReader fr = null;
			fr = new FileReader(tmpFile);
			int inChar;
			while ((inChar = fr.read()) != -1) {
				System.out.printf("%c", inChar);
			}
			
			//TODO levantar las tablas y crear el archivo
			MultiDim xmlDocument = parser.parse(tmpFile);
			xmlDocument.print();
			String MDXtables = MultidimCubeToMDXUtils.convertToMDX(xmlDocument);
			ModelAndView mav2 = new ModelAndView("/index/show_tables");
			mav2.addObject("MDXtables", MDXtables.replace("\n", "<br />")
													.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));

			final ConnectionManager connectionManager = ConnectionManagerPostgreWithCredentials.getConnectionManagerWithCredentials();
			
			if(connectionManager == null){
				mav.setViewName("redirect:" + req.getServletPath()
						+ "/index/index");
				return mav;
			}
			
			try {
				final Connection conn = connectionManager.getConnectionWithCredentials();
				mav2.addObject("dburl", connectionManager.getConnectionString());
				TableCreator tc = new TableCreator(conn);
				//TODO: Para que funcione: MDXtables..replace("geometry", "integer")
				tc.createTables(MDXtables);

			} catch (SQLException sqlexception){
				System.out.println("SQL Exception!!");
				sqlexception.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return mav2;
		}
//		mav.setViewName("redirect:" + req.getServletPath()
//				+ "/index/show_tables");
//		
//		System.out.println("----------redirecting !!!!");
//		return mav;
	}

	@RequestMapping(method = RequestMethod.GET)
	protected ModelAndView show_tables(HttpServletRequest req) throws ServletException, IOException {
		final ModelAndView mav = new ModelAndView();
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
			e.printStackTrace();
		}
//		TableCreator tc = new TableCreator();
		return mav;
	}

}
