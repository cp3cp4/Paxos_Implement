import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;
/*
* basic function test
* one content server update file
* */
public class test1 {
    public static void main(String[] argv){
        List<ContentServer> cslist = new ArrayList<>();

        //create file needed by test1
        File test1File = new File("test1.txt");


        ContentServer cs0 = new ContentServer(0,cslist,300,800);
        ContentServer cs1 = new ContentServer(1,cslist,300,800);
        ContentServer cs2 = new ContentServer(2,cslist,300,800);
        ContentServer cs3 = new ContentServer(3,cslist,300,800);
        ContentServer cs4 = new ContentServer(4,cslist,300,800);
        ContentServer cs5 = new ContentServer(5,cslist,300,800);

     

        Collections.addAll(cslist,cs0,cs1,cs2,cs3,cs4,cs5);

        //cover test1file to cs0.txt
        new FileTools().fileCover(test1File,cs0.getFile());
        cs0.StartUpdatePropose();

        while (cs0.ProposeState) {
            try {
                sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        while (cs0.ProposeState == false) {

            for (ContentServer cs : cslist) {
                if (!(new FileTools().isSameFile(cs.getFile(), test1File))) {
                    System.out.println("Test fail because CS" + cs.getServerNum() + "has the different content");
                    System.exit(0);
                }
            }
            System.out.println("Test succeed");
            System.exit(0);
        }


    }
}
