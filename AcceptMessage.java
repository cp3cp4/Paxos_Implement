import java.io.File;

public class AcceptMessage {

    private boolean result = false;
    private int AcceptVersionID = Integer.MIN_VALUE;
    private int FinalServerNum;
    //Whether the server has finalized the final version.
    private boolean CSstate = false;
    private File file;


    //Structure of the transmitted data
    public AcceptMessage(boolean result, int acceptVersionID, int finalServerNum, boolean CSstate) {
        this.result = result;
        this.AcceptVersionID = acceptVersionID;
        this.FinalServerNum = finalServerNum;
        this.CSstate = CSstate;
        //this.file = file;
    }

    public boolean isResult() {
        return this.result;
    }

    public boolean isCSstate() {
        return this.CSstate;
    }

}
