package ro.bogdani.simplefilebackup.model;

public class StatusModel {
	
	final static String DEFAULT_STATUS = "Ready";
	
	String status;
	
	StatusModelChangeListner listener;
	
	public void setStatus(String status) {
		this.status = status;
		listener.change(status);
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatusModelChangeListner(StatusModelChangeListner listener) {
		this.listener = listener;
	}

	public void setDefaultStatus() {
		setStatus(DEFAULT_STATUS);
	}
}
