package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Jenny Nguyen
 */
public class Repository {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_FOLDER = join(GITLET_DIR, ".commit");
    public static final File BLOB_FOLDER = join(GITLET_DIR, ".blobs"); // contains all the blobs
    public static final File STAGING_AREA = join(GITLET_DIR, ".stagingarea");
    public static final File TRACKEDFILES = join(GITLET_DIR, ".trackefiles");

    public static final File BRANCHES = join(GITLET_DIR, ".branches");
    private static final File MASTER = join(BRANCHES, "master");
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    public void init() {
        if (Repository.GITLET_DIR.exists()) {
            printErrorMessage("A Gitlet version-control system already "
                    + "exists in the current directory.");
        } else {
            if (!Repository.GITLET_DIR.exists()) {
                Repository.GITLET_DIR.mkdir();
            }
            if (!Repository.COMMIT_FOLDER.exists()) {
                Repository.COMMIT_FOLDER.mkdir();
            }
            if (!Repository.BLOB_FOLDER.exists()) {
                Repository.BLOB_FOLDER.mkdir();
            }
            if (!Repository.BRANCHES.exists()) {
                Repository.BRANCHES.mkdir();
            }
            if (!Repository.TRACKEDFILES.exists()) {
                try {
                    Repository.TRACKEDFILES.createNewFile();
                } catch (IOException excp) {
                    throw new IllegalArgumentException(excp.getMessage());
                }
            }
            if (!MASTER.exists()) {
                try {
                    MASTER.createNewFile();
                } catch (IOException excp) {
                    throw new IllegalArgumentException(excp.getMessage());
                }
            }
            if (!HEAD.exists()) {
                try {
                    HEAD.createNewFile();
                } catch (IOException excp) {
                    throw new IllegalArgumentException(excp.getMessage());
                }
            }
            if (!STAGING_AREA.exists()) {
                try {
                    Repository.STAGING_AREA.createNewFile();
                } catch (IOException excp) {
                    throw new IllegalArgumentException(excp.getMessage());
                }
            }

            StagingArea stage = new StagingArea();
            stage.saveStage();

            TrackedFiles track = new TrackedFiles();
            track.saveFiles();

            Commit initialCommit = new Commit("initial commit");
            initialCommit.saveCommit();

            writeContents(MASTER, initialCommit.getID());
            writeContents(HEAD, "master");
        }
    }

    private static void printErrorMessage(String s) {
        Utils.message(s);
        System.exit(0);
    }

    /** Adds a copy of the file as it currently exists to the staging area
     * (see the description of the commit command).
     * adding a file is also called staging the file for addition.
     * Staging an already-staged file overwrites the previous entry in the
     * staging area with the new contents. If the current working version of
     * the file is identical to the version in the current commit, do not
     * stage it to be added, and remove it from the staging area if it is
     * already there (as can happen when a file is changed, added, and
     * then changed back to it’s original version). The file will no longer
     * be staged for removal (see gitlet rm), if it was at the time of the command. */
    public void add(String name) {
        File f = join(CWD, name);
        StagingArea stage = StagingArea.fromFile();
        if (stage.checkStageRemoval(name)) {
            stage.saveStage();
            return;
        }
        if (!f.exists()) {
            printErrorMessage("File does not exist.");
        }
        String text = readContentsAsString(f);
        Commit curr = Commit.fromFile(findHeadID());

        TrackedFiles track = TrackedFiles.fromFile();
        track.putFile(name); // automatically saves

        Blob b = new Blob(f, name);


        /** if file and its contents the same in current commit, remove from staging area
         * if commit does not have or diff version, add to staging area
         */
        if (curr.getBlob(name) != null && curr.getBlob(name).getText().equals(text)) {
            stage.remove(b);
        } else {
            stage.addNew(b);
        }
        stage.saveStage();
    }

    // read from my computer the head commit object and the staging area
    // clone the HEAD commit
    // modify its message and timestamp according to user input
    // usage the staging area in order to modify the files tracked by the new commit
    // write back any new objects made or any modified objects read earlier
    // want to make a new commit object, normal runtime
    public void commit(String msg) {
        if (msg.equals("")) {
            printErrorMessage("Please enter a commit message.");
        }
        Commit oldCommit = Commit.fromFile(findHeadID());
        Commit newCommit = new Commit(msg, oldCommit.getID(), oldCommit.getBlobs());
        StagingArea stage = StagingArea.fromFile();

        if (stage.getStageAddition().isEmpty() && stage.getStageRemoval().isEmpty()) {
            printErrorMessage("No changes added to the commit.");
        }

        for (String key : stage.getStageAddition().keySet()) {
            Blob b = stage.getStageAddition().get(key);
            newCommit.removeBlob(b.getName());
            newCommit.putBlob(b);
        }
        for (String key: stage.getStageRemoval().keySet()) {
            newCommit.removeBlob(key);
        }
        stage.clearStages();

        newCommit.saveCommit();
        stage.saveStage();

        updateHead(newCommit.getID());

    }

    /** Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal
     * and remove the file from the working directory if the user has not
     * already done so (do not remove it unless it is tracked in the current
     * commit). */
    public void rm(String file) {
        StagingArea stage = StagingArea.fromFile();
        File head = join(BRANCHES, readContentsAsString(HEAD));
        Commit commitHead = Commit.fromFile(readContentsAsString(head));
        File f = Utils.join(CWD, file);
        Boolean fileExists = false;

        if (f.exists()) {
            fileExists = true;
        }
        stage.setStageRemoval(file, commitHead, fileExists);
        stage.saveStage();
    }

    // maybe us formatter
    public void log() {
        File head = join(BRANCHES, readContentsAsString(HEAD));
        String commitID = readContentsAsString(head);
        while (commitID != null) {
            Commit com = Commit.fromFile(commitID);
            System.out.println("===");
            System.out.println("commit " + com.getID());
            System.out.println("Date: " + com.getDate());
            System.out.println(com.getMessage() + "\n");
            commitID = com.getParent();
        }
    }

    // checkout [file name]: take file version in head commit and put/override in CWD; not staged
    public void checkoutFile(String name) {
        File f = join(CWD, name);
        if (!f.exists()) {
            printErrorMessage("File does not exist in that commit.");
        }
        Commit headCommit = Commit.fromFile(findHeadID());
        if (headCommit.getBlob(name) != null) {
            writeContents(f, headCommit.getBlob(name).getText());
        }
    }

    public void checkoutCommit(String commitID, String name) {
        commitID = updateShortenedID(commitID);
        if (!checkIfCommitExists(commitID)) {
            printErrorMessage("No commit with that id exists.");
        }
        Commit com = Commit.fromFile(commitID);
        File f = join(CWD, name);
        if (!com.getBlobs().containsKey(name)) {
            printErrorMessage("File does not exist in that commit.");
        } else if (com == null) {
            printErrorMessage("No commit with that id exists.");
        } else {
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException excp) {
                    throw new IllegalArgumentException(excp.getMessage());
                }
            }
            writeContents(f, com.getBlob(name).getText());
        }
    }

    private boolean checkIfCommitExists(String id) {
        File[] commits = COMMIT_FOLDER.listFiles();
        for (int i = 0; i < commits.length; i++) {
            if (commits[i].getName().equals(id)) {
                return true;
            }
        }
        return false;
    }
    private void deleteOtherFiles(ArrayList<String> trackedFiles) {
        TrackedFiles currFiles = TrackedFiles.fromFile();
        Iterator<String> iter = currFiles.gettFiles().iterator();
        while (iter.hasNext()) {
            if (!trackedFiles.contains(iter.next())) {
                currFiles.removeFile(iter.next());
            }
        }
    }

    private String updateShortenedID(String id) {
        if (id.length() < 10) {
            File[] files = COMMIT_FOLDER.listFiles();
            for (int i = 0; i < files.length; i++) {
                String temp = files[i].getName();
                if (temp.substring(0, id.length()).equals(id)) {
                    return temp;
                }
            }
        }
        return id;
    }


    public void checkOutBranch(String name, Boolean delBranch) {
        if (name.equals(readContentsAsString(HEAD))) {
            printErrorMessage("No need to checkout the current branch.");
        }

        File b = join(BRANCHES, name);
        if (!b.exists()) {
            printErrorMessage("No such branch exists.");
        }
        File head = join(BRANCHES, readContentsAsString(HEAD));
        String headID = readContentsAsString(head);

        Commit curr = Commit.fromFile(headID);
        Commit given = Commit.fromFile(readContentsAsString(b));
        lookForUntracked(curr, given);

        compareTrackedBtwnBranches(curr, given);

        for (String key : given.getBlobs().keySet()) {
            File f = join(CWD, key);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException excp) {
                    throw new IllegalArgumentException(excp.getMessage());
                }
            }
            writeContents(f, given.getBlobs().get(key).getText());
        }
        StagingArea stage = StagingArea.fromFile();
        stage.clearStages();
        stage.saveStage();

        if (delBranch) {
            b.delete();
        } else {
            writeContents(HEAD, name);
        }
    }

    private void compareTrackedBtwnBranches(Commit curr, Commit givenCom) {
        TreeMap<String, Blob> temp = curr.getBlobs();
        for (String key : temp.keySet()) {
            File f = join(CWD, key);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    /** private void checkIfUntracked(Commit curr) {
     TreeMap<String, Blob> headBlobs = curr.getBlobs();
     TrackedFiles inCWD = TrackedFiles.fromFile();
     for (String fileName : inCWD.gettFiles()) {
     if (headBlobs.containsKey())
     }
     }
     */
    /** @Source: geeks for geeks - google search :D */
    public void globalLog() {
        File[] f = COMMIT_FOLDER.listFiles();
        for (int i = 0; i < f.length; i++) {
            Commit c = Commit.fromFile(f[i].getName());
            System.out.println("===");
            System.out.println("commit " + c.getID());
            System.out.println("Date: " + c.getDate());
            System.out.println(c.getMessage() + "\n");
        }
    }

    public void find(String file) {
        Boolean found = false;
        File[] files = COMMIT_FOLDER.listFiles();
        for (int i = 0; i < files.length; i++) {
            Commit com = Commit.fromFile(files[i].getName());
            if (com.matchWithMessage(file)) {
                System.out.println(com.getID());
                found = true;
            }
        }
        if (!found) {
            printErrorMessage("Found no commit with that message.");
        }
    }

    // returns ID of the commit of where the head branch is
    private String findHeadID() {
        File activeBranch = join(BRANCHES, readContentsAsString(HEAD));
        return readContentsAsString(activeBranch);
    }

    public void status() {
        StagingArea stage = StagingArea.fromFile();
        TreeMap<String, Blob> stageAdd = stage.getStageAddition();
        TreeMap<String, String> stageRem = stage.getStageRemoval();

        System.out.println("=== Branches ===");
        File[] branches = BRANCHES.listFiles();

        for (int i = branches.length - 1; i >= 0; i--) {
            File b = branches[i];
            if (b.getName().equals(readContentsAsString(HEAD))) {
                System.out.print("*");
            }
            System.out.println(b.getName());
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String name : stageAdd.keySet()) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String n : stageRem.keySet()) {
            System.out.println(n);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void branch(String name) {
        File b = join(BRANCHES, name);
        if (!b.exists()) {
            try {
                b.createNewFile();
            } catch (IOException excp) {
                throw new IllegalArgumentException(excp.getMessage());
            }
            File headCommit = join(BRANCHES, readContentsAsString(HEAD));
            writeContents(b, readContentsAsString(headCommit));
        } else {
            printErrorMessage("A branch with that name already exists.");
        }
    }

    // moves the head branch to the latest commit
    private void updateHead(String id) {
        File active = join(BRANCHES, readContentsAsString(HEAD));
        writeContents(active, id);
    }

    public void rmBranch(String name) {
        File b = join(BRANCHES, name);
        if (!b.exists()) {
            printErrorMessage("A branch with that name does not exist.");
        } else if (readContentsAsString(HEAD).equals(name)) {
            printErrorMessage("Cannot remove the current branch.");
        }
        b.delete();
    }

    /** checks out all files tracked by the given commit
     * removes tracked files that are not present in that commit
     * also moves the current branch's head to that commit node
     * [commit id] may be abbreviated as for checkout
     * staging area is cleared
     * command is essentially checkout of an arbitracy commit that
     *  also changes the current branch head */
    public void reset(String id) {
        id = updateShortenedID(id);
        if (!checkIfCommitExists(id)) {
            printErrorMessage("No commit with that id exists.");
        }
        File temp = join(BRANCHES, "temp");
        try {
            temp.createNewFile();
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
        Utils.writeContents(temp, id);
        checkOutBranch("temp", true);
        updateHead(id);
    }

    private void lookForUntracked(Commit curr, Commit given) {
        for (String key : given.getBlobs().keySet()) {
            File f = join(CWD, key);
            if (!curr.getBlobs().containsKey(key) && f.exists()) {
                printErrorMessage("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
    }

    public void merge(String other) {
        StagingArea stage = StagingArea.fromFile();
        if (!stage.getStageAddition().isEmpty() || !stage.getStageRemoval().isEmpty()) {
            printErrorMessage("You have uncommitted changes.");
        }
        File oth = join(BRANCHES, other);
        File curr = join(BRANCHES, readContentsAsString(HEAD));

        if (!oth.exists()) {
            printErrorMessage("A branch with that name does not exist.");
        }
        if (other.equals(readContentsAsString(HEAD))) {
            printErrorMessage("Cannot merge a branch with itself.");
        }

        String oCommitID = readContentsAsString(oth);
        String cCommitID = readContentsAsString(curr);

        Commit oCommit = Commit.fromFile(oCommitID);
        Commit cCommit = Commit.fromFile(cCommitID);

        String splitID = lookForSplit(oCommit, cCommit);
        Commit split = Commit.fromFile(splitID);

        /** 1. modified in other but not head (head -> other)
         *  2. modified in head but not other (other -> head)
         *  3. modified in other AND head (same/diff way) (conflict)
         *  4. not in split nor other, but in head -> head
         *  5. not in split nor head, but in other -> other
         *  6. unmodified in head but not present in other -> remove
         *  7. unmofidied in other but not present in head -> remain removes
         */

        lookForUntracked(cCommit, oCommit);

        if (checkForConflict(oCommit, cCommit, split)) {
            printErrorMessage("Encountered a merge conflict.");
        } else {
            String msg = "Merged " + readContentsAsString(HEAD) + " with " + other + ".";
            commit(msg);
        }
    }

    private boolean checkForConflict(Commit curr, Commit other, Commit split) {
        TreeMap<String, Blob> cBlobs = curr.getBlobs();
        TreeMap<String, Blob> oBlobs = other.getBlobs();
        TreeMap<String, Blob> sBlobs = split.getBlobs();
        boolean result = false;

        for (String key : oBlobs.keySet()) {
            String cText = "";
            String oText = "";
            String sText = "";

            if (cBlobs.containsKey(key)) {
                cText = cBlobs.get(key).getText();
            }
            if (oBlobs.containsKey(key)) {
                oText = oBlobs.get(key).getText();
            }
            if (sBlobs.containsKey(key)) {
                sText = sBlobs.get(key).getText();
            }

            if (!cBlobs.containsKey(key)) {
                if (!sBlobs.containsKey(key)) {
                    makeFile(key, oText);
                    add(key);
                } else if (!sText.equals(oText)) {
                    result = true;
                    makeFile(key, mergeHelper(cText, oText));
                }
            } else if (cBlobs.containsKey(key) && sBlobs.containsKey(key) && sText.equals(cText)) {
                makeFile(key, oText);
                add(key);
            }
        }
        for (String name : cBlobs.keySet()) {
            String cText = "";
            if (cBlobs.containsKey(name)) {
                cText = cBlobs.get(name).getText();
            }
            String sText = "";
            if (sBlobs.containsKey(name)) {
                sText = sBlobs.get(name).getText();
            }
            String oText = "";
            if (oBlobs.containsKey(name)) {
                oText = oBlobs.get(name).getText();
            }
            if (sBlobs.containsKey(name) && !oBlobs.containsKey(name)) {
                if (sText.equals(cText)) {
                    rm(name);
                } else {
                    result = true;
                    makeFile(name, mergeHelper(cText, oText));
                }
            } else if (sBlobs.containsKey(name) && oBlobs.containsKey(name)) {
                if (!sText.equals(oText) && !cText.equals(oText)) {
                    result = true;
                    makeFile(name, mergeHelper(cText, oText));
                }
            } else if (!sBlobs.containsKey(name) && oBlobs.containsKey(name)
                    && !cText.equals(oText)) {
                result = true;
                makeFile(name, mergeHelper(cText, oText));
            }
        }
        return result;
    }

    private void makeFile(String name, String contents) {
        File f = join(CWD, name);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException excp) {
                throw new IllegalArgumentException(excp.getMessage());
            }
        }
        writeContents(f, contents);
    }

    private String lookForSplit(Commit oCommit, Commit cCommit) {
        // find split point and even out any uneven branches
        Commit cTemp1 = Commit.fromFile(cCommit.getID());
        int currBranchLength = 0;
        while (cTemp1.getParent() != null) {
            currBranchLength++;
            cTemp1 = Commit.fromFile(cTemp1.getParent());
        }

        Commit oTemp1 = Commit.fromFile(oCommit.getID());
        int oBranchLength = 0;
        while (oTemp1.getParent() != null) {
            oBranchLength++;
            oTemp1 = Commit.fromFile(oTemp1.getParent());
        }

        // chops of the uneven branches: oCommit and cCommit
        if (currBranchLength > oBranchLength) {
            cTemp1 = Commit.fromFile(cCommit.getID());
            int diff = currBranchLength - oBranchLength;
            while (diff != 0) {
                cTemp1 = Commit.fromFile(cTemp1.getParent());
                diff--;
            }
            cCommit = cTemp1;
            if (oCommit.getID() == cTemp1.getID()) {
                printErrorMessage("Given branch is an ancestor of the current branch.");
            }
        } else {
            int diff = oBranchLength - currBranchLength;
            oTemp1 = Commit.fromFile(oCommit.getID());
            while (diff != 0) {
                oTemp1 = Commit.fromFile(oTemp1.getParent());
                diff--;
            }
            oCommit = oTemp1;
            if (oTemp1.getID() == cCommit.getID()) {
                checkOutBranch(oCommit.getID(), false); // checks out to the other branch
                printErrorMessage("Current branch fast-forwarded.");
            }
        }

        String cParent = oCommit.getParent();
        String oParent = cCommit.getParent();
        while (!cParent.equals(oParent)) {
            cParent = Commit.fromFile(cParent).getID();
            oParent = Commit.fromFile(oParent).getID();
        }
        return cParent;
    }

    private String mergeHelper(String curr, String other) {
        return "<<<<<<< HEAD\n" + curr + "\n=======\n" + other + "\n>>>>>>>\n";
    }

}
