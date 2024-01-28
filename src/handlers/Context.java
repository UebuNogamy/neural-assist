package handlers;

public class Context {
	private String fileName;
	private String fileContents;
	private String selectedContent;
	private String selectedItem;
    private String selectedItemType;
    private String lang;
    
	public Context(String fileName, String fileContents, String selectedContent, String selectedItem,
			String selectedItemType, String lang) {
		super();
		this.fileName = fileName;
		this.fileContents = fileContents;
		this.selectedContent = selectedContent;
		this.selectedItem = selectedItem;
		this.selectedItemType = selectedItemType;
		this.lang = lang;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileContents() {
		return fileContents;
	}

	public void setFileContents(String fileContents) {
		this.fileContents = fileContents;
	}

	public String getSelectedContent() {
		return selectedContent;
	}

	public void setSelectedContent(String selectedContent) {
		this.selectedContent = selectedContent;
	}

	public String getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
	}

	public String getSelectedItemType() {
		return selectedItemType;
	}

	public void setSelectedItemType(String selectedItemType) {
		this.selectedItemType = selectedItemType;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
