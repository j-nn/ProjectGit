package gitlet;

import java.io.Serializable;
import java.util.ArrayList;


public class TrackedFiles implements Serializable {
    /** Key: name of file
     * Value: file
     */
    private ArrayList<String> tFiles;
    private ArrayList<String> untracked;

    TrackedFiles() {
        tFiles = new ArrayList<>();
        untracked = new ArrayList<>();
    }

    public ArrayList<String> gettFiles() {
        return tFiles;
    }

    public ArrayList<String> getUntrackedFiles() {
        return untracked;
    }

    public void putUntrackedFile(String n) {
        untracked.add(n);
        this.saveFiles();
    }

    public void putFile(String n) {
        tFiles.add(n);
        this.saveFiles();
    }

    public void removeFile(String n) {
        tFiles.remove(n);
        this.saveFiles();
    }

    public static TrackedFiles fromFile() {
        return Utils.readObject(Repository.TRACKEDFILES, TrackedFiles.class);
    }

    public void saveFiles() {
        Utils.writeObject(Repository.TRACKEDFILES, this);
    }

}
