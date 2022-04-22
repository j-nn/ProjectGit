package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Jenny Nguyen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */


    public static void main(String[] args) {
        if (args.length < 1) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        gitlet.Repository bloop = new gitlet.Repository();
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                bloop.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                validateHasDirectory();
                bloop.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                validateHasDirectory();
                bloop.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                validateHasDirectory();
                bloop.status();
                break;
            case "checkout":
                validateHasDirectory();
                if (args.length == 3) {
                    bloop.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    bloop.checkoutCommit(args[1], args[3]);
                } else if (args.length == 2) {
                    bloop.checkOutBranch(args[1], false);
                } else {
                    validateNumArgs(args, 0); // will automatically return an error message
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                validateHasDirectory();
                bloop.merge(args[1]);
                break;
            default:
                Utils.message("No command with that name exists.");
                System.exit(0);

        }

    }

    private static void validateNumArgs(String[] args, int num) {
        if (args.length != num) {
            gitlet.Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void validateHasDirectory() {
        if (!gitlet.Repository.GITLET_DIR.exists()) {
            gitlet.Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }


    /**  Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message initial commit (just like that,
     * with no punctuation). It will have a single branch: master, which initially
     * points to this initial commit, and master will be the current branch.
     * Since the initial commit in all repositories created by Gitlet will have
     * exactly the same content, it follows that all repositories will
     * automatically share this commit (they will all have the same UID)
     * and all commits in all repositories will trace back to it. */

}
