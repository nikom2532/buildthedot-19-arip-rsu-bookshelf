package th.co.arip.db;

public class Book {
	  private long pid;
	  private String id;
	  private String title;
	  private String detail;
	  private String date;
	  private String category;
	  private String image;
	  private String revision;

	  public long getPid() {
	    return pid;
	  }

	  public void setPid(long pid) {
	    this.pid = pid;
	  }

	  public String getId() {
		    return id;
		  }

	  public void setId(String id) {
		    this.id = id;
		 }
	  
	  public String getTitle() {
	    return title;
	  }

	  public void setTitle(String title) {
	    this.title = title;
	  }
	  
	  public String getDetail() {
		    return detail;
	  }

	  public void setDetail(String detail) {
		    this.detail = detail;
	  }
	  
	  public String getDate() {
		    return date;
	  }

	  public void setDate(String date) {
		    this.date = date;
	  }
	  
	  public String getCategory() {
		    return category;
		  }

	  public void setCategory(String category) {
		    this.category = category;
		  }
	  
	  public String getImage() {
		    return image;
		  }

	  public void setImage(String image) {
		    this.image = image;
		  }
	  
	  public String getRevision() {
		    return revision;
		  }

	  public void setRevision(String revision) {
		    this.revision = revision;
		  }
}
