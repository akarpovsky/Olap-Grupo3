package olap.olap.project.web.command;

import org.springframework.web.multipart.MultipartFile;


public class UploadXmlForm {

	
	private MultipartFile file;
	private boolean manualDataSelection;
	

	public boolean getManualDataSelection() {
		return manualDataSelection;
	}

	public void setManualDataSelection(boolean manualDataSelection) {
		this.manualDataSelection = manualDataSelection;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public MultipartFile getFile() {
		return file;
	}

	
}
