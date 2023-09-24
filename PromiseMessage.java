import java.io.File;

public class PromiseMessage {

    private boolean result = false;
    private int PromiseVersionID =Integer.MIN_VALUE;
    private int contentServerNum;
    //Whether the server has finalized the final version.
    private boolean CSstate = false;


    //structure of data when transmitting
    public PromiseMessage(boolean result, int promiseVersionID, int contentServerNum,boolean CSstate) {
        this.result = result;
        PromiseVersionID = promiseVersionID;
        this.contentServerNum = contentServerNum;
        this.CSstate = CSstate;
    }

    public boolean isResult() {
        return result;
    }

    public boolean isCSstate() {
        return CSstate;
    }
}
