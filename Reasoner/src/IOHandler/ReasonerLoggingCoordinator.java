package IOHandler;

import org.apache.jena.graph.Triple;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

/**
 * Class to write inferred triple statements and their supporting triples to files.
 */
public abstract class ReasonerLoggingCoordinator {
    //All the files with logging information are wrapped.
    static Stack<LogFileWrapper> fileStack = new Stack<>();
    static int fileCount = 0;

    /**
     * Logs the triples in the base knowledge graph in string format. Used when
     * new knowledge graph is being processed.
     * @param list
     */
    public static void logBaseKG(List<Triple> list, String sourceFileName) {
        //Clears everything for new ontology.
        fileStack.clear();
        fileCount = 0;

        try {
            LogFileWrapper f = new LogFileWrapper(fileCount);
            f.sourceFileName = sourceFileName;

            for (Triple t : list) {
                f.addSupportStm("\n" + t.toString());
            }

            //Add file to the stack.
            fileStack.push(f);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sets up file for next step in the reasoner.
     */
    public static void newIterationStep() {
        //Finalizes the previous file wrapper because if at this state then it is good to go.
        LogFileWrapper prev = fileStack.pop();
        prev.finalizeFile();
        fileStack.push(prev);

        try {
            LogFileWrapper f = new LogFileWrapper(fileCount+=1);
            fileStack.push(f);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Checks previous log file for any duplicates and then writes new supporting statements to a file wrapper.
     * @param t1
     * @param t2
     * @param inf
     * @throws IOException
     */
    public static void logTriples(Triple t1, Triple t2, Triple inf) throws IOException {
        LogFileWrapper prevF = fileStack.peek();
        LogFileWrapper f = fileStack.pop();

        String stm = t1.toString() + " + " + t2.toString() + " = " + inf.toString();

        if(!prevF.containsStm(stm)){
            f.addSupportStm("\n" + stm);
        }

        fileStack.push(f);
    }

    /**
     * Checks previous log file for any duplicates and then writes new supporting statements to a file wrapper.
     * @param t1
     * @param inf
     */
    public static void logTriples(Triple t1, Triple inf){
        LogFileWrapper prevF = fileStack.peek();
        LogFileWrapper f = fileStack.pop();

        String stm = t1.toString() +  " = " + inf.toString();

        if(!prevF.containsStm(stm)){
            f.addSupportStm("\n" + stm);
        }

        fileStack.push(f);
    }
}