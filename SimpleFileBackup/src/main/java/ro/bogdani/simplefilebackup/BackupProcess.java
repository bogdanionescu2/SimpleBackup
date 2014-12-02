package ro.bogdani.simplefilebackup;

public interface BackupProcess {

	enum State { READY, CHOOSING_FILES, BACKUP };
	
	void stateChange(State state);
}
