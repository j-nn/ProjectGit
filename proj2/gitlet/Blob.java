package gitlet;
import java.io.File;
import java.io.Serializable;

/** Contents of a file
 * Different versions of a file
 */
public class Blob implements Serializable {
    private String text;
    private String name;

    Blob(File f, String n) {
        text = gitlet.Utils.readContentsAsString(f);
        name = n;
    }

    public String getText() {
        return text;
    }

    public String getName() {

        return name;
    }

}
