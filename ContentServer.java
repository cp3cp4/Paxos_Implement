import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class ContentServer {
    private final ExecutorService Proposer;
    private final ExecutorService Accpetor;
    private final ExecutorService Learner;
    public List<ContentServer> CSlist;
    //used to store the current committed version number of this server
    private int CurrentPromisedVersionID = Integer.MIN_VALUE;
    //Used to store the current proposal version number of this server
    private int CurrentProposeVersionID = Integer.MIN_VALUE;
    //Used to store the currently accepted version number of this server
    private int CurrentAcceptedVersionID = Integer.MIN_VALUE;
    //Used to store the final agreed server number
    private int Final_CSnum = -1;


    //    private AtomicInteger VersionID = new AtomicInteger(-1)
    // Version number control, Integer shared by all servers
    private static int versionID = 0;


    private int serverNum;
    private File file;
    private int minDelay;
    private int maxDelay;
    public Boolean ProposeState;

    public int getServerNum() {
        return serverNum;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int GetCurrentProposeVersionID() {
        return this.CurrentProposeVersionID;
    }

    public void SetCurrentProposeVersionID(int ID) {
        this.CurrentProposeVersionID = ID;
    }

    public int GetCurrentPromisedVersionID() {
        return this.CurrentPromisedVersionID;
    }

    public void SetCurrentPromisedVersionID(int ID) {
        this.CurrentPromisedVersionID = ID;
    }

    public void SetCurrentAcceptedVersionID(int ID) {
        this.CurrentAcceptedVersionID = ID;
    }

    //intialize servers and create local files
    //make the propose thread wait
    public ContentServer(int serverNum, List<ContentServer> CSlist, int mindelay, int maxDelay) {
        this.serverNum = serverNum;
        this.minDelay = mindelay;
        this.maxDelay = maxDelay;
        //this.file = new File("CS"+serverNum+".txt");
        this.CSlist = CSlist;
        this.Proposer = Executors.newSingleThreadExecutor();
        this.Accpetor = Executors.newCachedThreadPool();
        this.Learner = Executors.newCachedThreadPool();
        this.Proposer.submit(this::UpdateProposerCtrl);
        //Point to this file if it exists in the root directory
        String filepath = "ContentSever" + serverNum + ".txt";
        //create file object
        File file = new File(filepath);
        if (file.exists()){
            this.file = file;
        }
        else{
            try {
                file.createNewFile();
                this.file = file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void UpdateProposerCtrl() {

        //Three stages are required, proposing, accepting, and learning.

        //Only single threads are allowed to use
        synchronized (this.Proposer) {
            try {
                this.Proposer.wait();
                this.ProposeState = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.UpdatePropose();
    }

    //if any servers starting updating files then notify the propose thread
    public void StartUpdatePropose() {
        synchronized (this.Proposer) {
            this.Proposer.notify();
            this.ProposeState = true;
        }
    }

    private void UpdatePropose() {

        //Update the existing version ID of the server and synchronize it to the latest version ID+1
        this.SetCurrentProposeVersionID(GetNewVersionID());

        System.out.println("ContentServer: CS["+this.serverNum+"] Start a propose with VisionID == "+this.GetCurrentProposeVersionID());

        List<PromiseMessage> PM = new ArrayList<>();
        List<ContentServer> allCS = this.CSlist;
        Random r = new Random();
        // Randomize the order of the list, disrupt the list
        allCS = new ArrayList<>(allCS);
        Collections.shuffle(allCS,r);
        int HalfLength = allCS.size()/2;
        System.out.println("CS.size="+HalfLength);
        List<ContentServer> CS = new HalfSelect().overHalf(allCS,this);

        // Exclude yourself from communicating with yourself.
        for (ContentServer c : CS) {

            // The 2 here needs to be changed to the PREPARE version number
            PM.add(prepare(c, this.GetCurrentProposeVersionID()));

            System.out.println("CS" + this.serverNum + ":already found ->" + c.serverNum + "send propose");
        }

        /*for(PromiseMessage p : PM){
            if(p.isCSstate()){

                System.out.println("CS"+this.serverNum);
                this.UpdateProposerCtrl();
            }
            if(!p.isResult()){
                System.out.println(p.getContentServerNum()+"F");
                this.UpdatePropose();
            }
        }*/

        boolean allPromised = PM.stream().allMatch(PromiseMessage::isResult);

        System.out.println("ContentServer: CS[" + serverNum + "] Initiated a majority of proposals, proposed results:" + allPromised);
        if(!allPromised){
            for(PromiseMessage p : PM){
                if(p.isCSstate()){
                    this.UpdateProposerCtrl();
                }
            }
            this.UpdatePropose();
        }else {
            //Start accept phase for Vote
            List<AcceptMessage> AM = new ArrayList<>();
            for (ContentServer c : CS) {

                //Exclude yourself from communicating with yourself.
                AM.add(Vote(c, this.GetCurrentProposeVersionID()));

                System.out.println("CS" + this.serverNum + ": campaign for votes CS ->" + c.serverNum + "send propose");
            }

            boolean allAccepted = AM.stream().allMatch(AcceptMessage::isResult);System.out.println("ContentServer: CS[" + serverNum + "]  vote result  " + allAccepted);

            if(!allAccepted){
                for(AcceptMessage a : AM){
                    if(a.isCSstate()){
                        this.UpdateProposerCtrl();
                    }
                }
                this.UpdatePropose();
            }else{
                //AM all agree, get more than half vote, notify other ConentServer to learn, complete a consensus.
                for(ContentServer c : CSlist){
                    //learn
                    Learn(c,this.GetCurrentProposeVersionID());
                }

            }

        }

    }

    public PromiseMessage prepare(ContentServer contentServer, int UpdateProposerVersion) {
        Future<PromiseMessage> PMfuture = contentServer.Accpetor.submit(() -> {

            try{
                System.out.println("cs"+contentServer.getServerNum()+"==sleep");
                sleep(getRandomDelay(contentServer.minDelay,contentServer.maxDelay));
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(contentServer.serverNum == 0 && this.serverNum == 1){
                return new PromiseMessage(false, contentServer.CurrentPromisedVersionID, this.serverNum,false);
            }
            if(contentServer.serverNum == 1 && this.serverNum == 0 && contentServer.ProposeState){
                return new PromiseMessage(false, contentServer.CurrentPromisedVersionID, this.serverNum,false);
            }

            if(this.Final_CSnum!=-1){
                // check if the final version has been determined -1 is undetermined, greater than 0 is determined
                return new PromiseMessage(false, contentServer.CurrentPromisedVersionID, this.serverNum,true);
            }

            if(contentServer.GetCurrentPromisedVersionID()<0){
                System.out.println("Initialize ContentServer: CS[" + contentServer.serverNum + "]  accept the propose from CS["+this.serverNum+"] with VersionID = " + UpdateProposerVersion);
                // Update the current committed version number of that server
                contentServer.SetCurrentPromisedVersionID(UpdateProposerVersion);
                return new PromiseMessage(true, contentServer.CurrentPromisedVersionID, this.serverNum,false);
            } else if(contentServer.CurrentPromisedVersionID>=UpdateProposerVersion){
                return new PromiseMessage(false, contentServer.CurrentPromisedVersionID, this.serverNum,false);
            }else {

                System.out.println("ContentServer: CS[" + contentServer.serverNum + "]  accept the propose from CS["+this.serverNum+"] with VersionID = " + UpdateProposerVersion);
                // Update the current committed version number of that server
                contentServer.SetCurrentPromisedVersionID(UpdateProposerVersion);

                return new PromiseMessage(true, contentServer.CurrentPromisedVersionID, this.serverNum,false);
            }

        });

        try {
            //if(PMfuture.isDone()) {  // check weather all the future submission has done the task and return the result.
            //    return PMfuture.get();
            //}  else {
                return PMfuture.get(900, TimeUnit.MILLISECONDS);
            //}
        } catch (Exception e) {
            return new PromiseMessage(false, this.CurrentPromisedVersionID, this.serverNum,false);
        }
    }


    public AcceptMessage Vote(ContentServer contentServer, int UpdateProposerVersion) {
        Future<AcceptMessage> PMfuture = contentServer.Accpetor.submit(() -> {

            try{
                sleep(getRandomDelay(contentServer.minDelay,contentServer.maxDelay));
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(this.Final_CSnum!=-1){
                // check if the final version has been determined -1 is undetermined, greater than 0 is determined
                return new AcceptMessage(false, contentServer.CurrentAcceptedVersionID, this.serverNum,true);
            }

            if(contentServer.CurrentAcceptedVersionID>=UpdateProposerVersion){
                return new AcceptMessage(false, contentServer.CurrentAcceptedVersionID, this.serverNum,false);
            }else {

                System.out.println("ContentServer: CS[" + contentServer.serverNum + "] make a Vote to CS["+this.serverNum+"] with VersionID = " + UpdateProposerVersion);
                // Update the accepted version number of that server
                contentServer.SetCurrentAcceptedVersionID(UpdateProposerVersion);

                return new AcceptMessage(true, contentServer.CurrentAcceptedVersionID, this.serverNum,false);
            }

        });

        try {
            //if(PMfuture.isDone()) {  // check weather all the future submission has done the task and return the result.
            //    return PMfuture.get();
            //}  else {
            return PMfuture.get(900, TimeUnit.MILLISECONDS);
            //}
        } catch (Exception e) {
            return new AcceptMessage(false, this.CurrentAcceptedVersionID, this.serverNum,false);
        }
    }

    public synchronized void Learn(ContentServer contentServer,int learnVersionID){
        contentServer.Learner.submit(() -> {
            contentServer.Final_CSnum = this.serverNum;
            contentServer.SetCurrentPromisedVersionID(learnVersionID);
            contentServer.SetCurrentAcceptedVersionID(learnVersionID);
            //read the contents of this.file and write it to contentServer.file
            new FileTools().fileCover(this.file,contentServer.file);

            System.out.println("ContentServer: CS[" + contentServer.serverNum + "] Complete local updates");
        });
    }



    private int GetNewVersionID(){
//        return VersionID.incrementAndGet();
        return versionID++;
    }

    private int getRandomDelay(int min, int max){
        return (int)(1 + Math.random() * (max - min + 1));
    }

}
