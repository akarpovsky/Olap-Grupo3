package olap.olap.project.web.command;

import org.springframework.web.multipart.MultipartFile;


public class DBCredentialsForm {

	
	private String url_db;
	private String user_db;
	private String password_db;
	
	public String getUrl_db() {
		return url_db;
	}
	public void setUrl_db(String url_db) {
		this.url_db = url_db;
	}
	public String getUser_db() {
		return user_db;
	}
	public void setUser_db(String user_db) {
		this.user_db = user_db;
	}
	public String getPassword_db() {
		return password_db;
	}
	public void setPassword_db(String password_db) {
		this.password_db = password_db;
	}

	
}
