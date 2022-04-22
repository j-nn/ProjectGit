package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;


public class StagingArea implements Serializable {
    // key: file name; value: file's contents/text or blob
    private TreeMap<String, Blob> stageAddition; //make a new commit in the future
    private TreeMap<String, String> stageRemoval; //staged files for removal

    public StagingArea() {
        clearStages();
    }

    public TreeMap<String, Blob> getStageAddition() {
        return stageAddition;
    }

    public TreeMap<String, String> getStageRemoval() {
        return stageRemoval;
    }

    // add new blob or version
    public void addNew(Blob b) {
        String n = b.getName();
        Boolean stagedFileName = stageAddition.containsKey(n);
        Boolean sameFileVersion = b.getText().equals(stageAddition.get(n));

        if (stagedFileName && sameFileVersion) { // if same file then remove from staging
            stageAddition.remove(n);
        }
        if (!stagedFileName || !sameFileVersion) {
            stageAddition.put(b.getName(), b);
        }
    }

    // removes blob because exact version already in current version of commit
    public void remove(Blob b) {
        String name = b.getName();
        if (stageAddition.containsKey(name)) {
            stageAddition.remove(name);
        }
    }

    public boolean checkStageRemoval(String name) {
        if (stageRemoval.containsKey(name)) {
            stageRemoval.remove(name);
            return true;
        }
        return false;
    }
    // after each commit, stages are cleared
    public void clearStages() {
        stageAddition = new TreeMap<>();
        stageRemoval = new TreeMap<>();
    }

    // HELP
    public void setStageRemoval(String name, Commit curr, Boolean fileExists) {
        if (curr.getBlob(name) == null && !stageAddition.containsKey(name)) {
            Utils.message("No reason to remove the file.");
        }

        if (stageAddition.containsKey(name)) {
            stageAddition.remove(name);
        }
        if (curr.getBlob(name) != null) {
            stageRemoval.put(name, null);
            TrackedFiles tracked = TrackedFiles.fromFile();
            tracked.removeFile(name);

            if (fileExists) {
                File f = Utils.join(Repository.CWD, name);
                f.delete();
            }
        }
    }


    public static StagingArea fromFile() {
        return Utils.readObject(Repository.STAGING_AREA, StagingArea.class);
    }

    public void saveStage() {
        Utils.writeObject(Repository.STAGING_AREA, this);
    }

    /** fixes the new version of a file
     public static void setStageAddition(String file, String contents) {
     String fileID = sha1(file);
     String fileContent = sha1(contents);
     if (checkFileModified(file, fileContent)) {
     stageAddition.put(fileID, contents);
     } else if (stageAddition.get(fileID) != null) {
     stageAddition.remove(fileID);
     }
     }


     public void checkIfMapNotNull() {
     if (stageAddition == null) {
     System.out.println("stage is null");
     }
     }


     // returns true if modified
     private static boolean checkFileModified(String file, String contents) {
     String existentFile = stageAddition.get(Utils.sha1(file));
     return !existentFile.equals((contents));
     }

     */
}
