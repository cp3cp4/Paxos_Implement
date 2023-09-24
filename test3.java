import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Thread.sleep;

/*
* Content Server failure test
* set content server2 a higher delay
* */
public class test3 {

    public static void main(String[] argv){
        List<ContentServer> cslist = new ArrayList<>();

        //create file needed by test2
        File test2FileA = new File("test3A.txt");
        File test2FileB = new File("test3B.txt");


        ContentServer cs0 = new ContentServer(0,cslist,400,800);
        ContentServer cs1 = new ContentServer(1,cslist,400,800);
        ContentServer cs2 = new ContentServer(2,cslist,5000,8000);
        ContentServer cs3 = new ContentServer(3,cslist,400,2000);
        ContentServer cs4 = new ContentServer(4,cslist,400,1000);
        ContentServer cs5 = new ContentServer(5,cslist,400,1000);
        ContentServer cs6 = new ContentServer(6,cslist,400,1000);

        /*CSlist.add(CS0);
        CSlist.add(CS1);
        CSlist.add(CS2);
        CSlist.add(CS3);
        CSlist.add(CS4);
        CSlist.add(CS5);*/

        Collections.addAll(cslist,cs0,cs1,cs2,cs3,cs4,cs5,cs6);

        //cover test1file to cs0.txt
        new FileTools().fileCover(test2FileA,cs0.getFile());
        new FileTools().fileCover(test2FileB,cs1.getFile());
        cs0.StartUpdatePropose();
        cs1.StartUpdatePropose();

        while (cs0.ProposeState || cs1.ProposeState) {
            try {
                sleep(50000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        while (cs0.ProposeState == false && cs1.ProposeState == false) {

            int count = 0;
            for (ContentServer cs : cslist) {
                if (new FileTools().isSameFile(cs.getFile(), test2FileA)) {
                    count++;
                }
            }
            //succeed
            if (count == cslist.size()){
                System.out.println("CS has submitted the final version. All files of all content server have been updated  =====Succeed=====");
                System.exit(0);
            }
            else {

                for (ContentServer cs : cslist) {
                    if (new FileTools().isSameFile(cs.getFile(), test2FileB)) {
                        count++;
                    }
                }
                //succeed
                if (count == cslist.size()){
                    System.out.println("CS has submitted the final version. Files of all content servers have been updated  =====Succeed=====");
                    System.exit(0);
                }
                else {
                    System.out.println(count);
                    System.out.println("CS has submitted the final version. Files of all content servers have been updated  =====Succeed=====");
                    System.exit(0);
                }
            }

        }


    }
}
