package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TreeMap;


/** Represents a gitlet commit object.
 * *  does at a high level.
 *
 *  @author Jenny Nguyen
 */
public class Commit implements Serializable {
    // store this object for it to be read later
    private String ID;
    private String date; // same date everytime
    private String commitMessage; // message of commit
    private String parentID; // for sha1 purposes
    private TreeMap<String, Blob> blobs; // will point to the right version
    SimpleDateFormat dt = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    // commit0
    public Commit(String message) {
        this.commitMessage = message;
        this.parentID = null;
        this.ID = Utils.sha1(Utils.serialize(this));

        this.date = "Thu Jan 1 00:00:00 1970 +0000";
        this.blobs = new TreeMap<>();
    }

    public Commit(String message, String parent, TreeMap<String, Blob> oldBlob) {
        this.commitMessage = message;
        this.parentID = parent;
        this.ID = Utils.sha1(Utils.serialize(this));
        Date d = new Date();
        this.date = dt.format(d);
        this.blobs = oldBlob;
    }


    public String getMessage() {
        return this.commitMessage;
    }
    public String getDate() {
        return this.date;
    }
    public String getParent() {
        return this.parentID;
    }
    public String getID() {

        return this.ID;
    }
    public TreeMap<String, Blob> getBlobs() {
        return blobs;

    }

    public Blob getBlob(String key) {
        if (blobs.containsKey(key)) {
            return blobs.get(key);
        }
        return null;
    }



    // remove old version of blob from hashmap, only need String
    public void removeBlob(String name) {
        if (blobs.containsKey(name)) {
            blobs.remove(name);
        }
    }
    // puts new version of blob in hash map PROBABLY WRONG; string blob name and serialize
    public void putBlob(Blob b) {
        String name = b.getName();
        blobs.put(name, b);
    }

    /**
     * Reads in and deserializes a commit a file with name NAME in COMMIT_FOLDER.
     *
     * @param name Name of commit to load
     * @return commit read from file
     */

    public static Commit fromFile(String name) {
        File inFile = Utils.join(Repository.COMMIT_FOLDER, name);
        return Utils.readObject(inFile, Commit.class);
    }

    // saves commit into commit folder
    public void saveCommit() {
        File outFile = Utils.join(Repository.COMMIT_FOLDER, this.ID);
        Utils.writeObject(outFile, this);
    }

    public Boolean matchWithMessage(String msg) {
        if (msg.equals(getMessage())) {
            return true;
        }
        return false;
    }

    public void changeBlobs(TreeMap<String, Blob> newBlobs) {
        this.blobs = newBlobs;
    }


    /**

    public static void Main(String[] args) {
        Commit c = new Commit("JN", "March", "message", "parent id");
        File f = new File("commit example");
        Utils.writeObject(f,c);
    }
*/
}
