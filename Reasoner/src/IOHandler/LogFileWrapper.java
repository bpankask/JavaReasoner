package IOHandler;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which wraps a File object and allows the create of the file to be postponed
 * until sure if it should be created.
 */
public class LogFileWrapper {

    private String destFolder;
    static String sourceFileName;
    private int iterStep;
    public List<String> supportStatements;

    public LogFileWrapper(int step){
        this.destFolder = "OutputFiles\\";
        this.iterStep = step;
        this.supportStatements = new ArrayList<String>();
    }

    /**
     * Actually creates the file and writes all info to it.
     */
    public void finalizeFile(){
        try {
            File f = new File(destFolder + createFileName());

            FileWriter fw = new FileWriter(f, true);
            fw.append("Reasoner_State: " + iterStep);
            for(String stm : supportStatements){
                fw.append("\n" + stm);
            }
            fw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Checks to see if a statement is already in a file.
     * @param stm
     * @return
     */
    public boolean containsStm(String stm){
        if(supportStatements.contains(stm)) return true;

        else return false;
    }

    /**
     * Adds a support statement to the list of supports if it isn't already there.
     * @param stm
     */
    public void addSupportStm(String stm){
        if(!containsStm(stm)){
            supportStatements.add(stm);
        }
    }

    /**
     * Creates a file name based on state of this class.
     * @return
     */
    private String createFileName(){
        return sourceFileName + "_step_" + iterStep + ".txt";
    }
}
