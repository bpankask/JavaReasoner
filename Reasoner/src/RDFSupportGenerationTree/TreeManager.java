package RDFSupportGenerationTree;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.rulesys.RuleDerivation;

import java.util.*;

/**
 * Entry point class for working with a support tree. Creates and manipulates a tree like structure
 * of inferences and facts from the KB.
 */
public class TreeManager {

    public InfModel infModel;
    public Model baseModel;
    public List<TreeNode> tree = null;
    public HashMap<Double, String> encodingMap;

    public TreeManager(InfModel inf){
        this.infModel = inf;
        baseModel = inf.getRawModel();
    }

    public InfModel getInfModelUsed() {
        return infModel;
    }

    /**
     * Creates a table with triples as strings in keys and TreeNodes as values.  The nodes are not connected after
     * calling this method.
     * @return
     */
    public Hashtable<String,TreeNode> createTreeNodes() {

        //List of tableOfNodes nodes which will be linked together later.
        Hashtable<String,TreeNode> tableOfNodes = new Hashtable<String, TreeNode>();

        try {
            //Gets all statements/triples in the inference model.
            for (StmtIterator i = infModel.listStatements(); i.hasNext(); ) {

                Statement s = i.nextStatement();

                //If statement is a fact then it won't have derivation.
                if( baseModel.contains(s)){
                    FactNode fn = new FactNode(s.asTriple());
                    //fn.setEncoding(encodeStatement(s));
                    tableOfNodes.put(s.asTriple().toString(), fn);
                }
                else {
                    //Gets all the Derivations from a given statement/triple.
                    for (Iterator id = infModel.getDerivation(s); id.hasNext(); ) {

                        //Creates node from derivation/inference.
                        RuleDerivation deriv = (RuleDerivation) id.next();
                        InferenceNode node = new InferenceNode(deriv, infModel);
                        //node.setEncoding(encodeStatement(s));

                        if (!tableOfNodes.contains(node.toString())) {
                            tableOfNodes.put(node.toString(), node);
                        }
                        else {
                            throw new Exception("One key exists with potentially two values.  Check it out.");
                        }
                    }//end for
                }//end else
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableOfNodes;
    }

    /**
     * Links inference nodes to their supporting nodes creating a tree like structure.
     * @param tree
     */
    public List<TreeNode> createTree(Hashtable<String, TreeNode> tree){

        Enumeration e = tree.elements();

        while(e.hasMoreElements()){

            //Gets node to be linked.
            TreeNode node = (TreeNode) e.nextElement();

            //Must be an inference node to contain supports.
            if(node instanceof InferenceNode){
                InferenceNode infNode = (InferenceNode) node;

                //Finds the other nodes to support the current node.
                String test = infNode.matchList.get(0).toString();
                infNode.support1 = tree.get(test);
                if(infNode.matchList.get(1) != null) {
                    infNode.support2 = tree.get(infNode.matchList.get(1).toString());
                }
            }
        }//end while
        this.tree = new ArrayList<TreeNode>(tree.values());
        return new ArrayList<TreeNode>(tree.values());
    }

    /**
     * Recursive method which sets the step in a sequence in which a inference was derived during reasoning.
     * This may not have been the actual time it happened but timeStep is a created measure to show when the information
     * was available to create that particular inference.
     * @param node
     * @return
     */
    public int assignTimeSteps(TreeNode node){
        if(node.getTimeStep() > 0)
            return node.getTimeStep() + 1;

        if(node instanceof FactNode)
            return 1;

        InferenceNode infNode = (InferenceNode) node;
        int v1 = 0;
        v1 += assignTimeSteps(infNode.support1);

        int v2 = 0;
        if(infNode.support2 != null){
            v2 += assignTimeSteps(infNode.support2);
        }

//        if(infNode.getSupportEncoding() == null){
//            infNode.setSupportEncoding();
//        }

        if(v1 >= v2){
            infNode.setTimeStep(v1);
            return v1 + 1;
        }
        else{
            infNode.setTimeStep(v2);
            return v2 + 1;
        }
    }

    /**
     * Creates appropriate TreeNode which is a fact and updates its encoding field.
     * @param s
     * @return
     */
    private List<Double> encodeStatement(Statement s){

        double subjectEnc = 0;
        double predEnc = 0;
        double objectEnc = 0;

        for(Map.Entry<Double, String> entry : encodingMap.entrySet()){
            //It is a concept if positive.
            if(entry.getKey() > 0){
                if(entry.getValue().equals(s.getSubject().toString())){
                    subjectEnc = entry.getKey();
                }
                if(entry.getValue().equals(s.getObject().toString())){
                    objectEnc = entry.getKey();
                }
            }
            //It is a role if negative.
            if(entry.getKey() < 0){
                if( entry.getValue().equals(s.getPredicate().toString())){
                    predEnc = entry.getKey();
                }
            }
        }

        List<Double> encoding = new ArrayList<Double>();
        encoding.add(subjectEnc);
        encoding.add(predEnc);
        encoding.add(objectEnc);

        return encoding;
    }

    /**
     * Separates the tree by time step value.
     * @return Map from time step number to list of tree node.
     */
    public HashMap<Integer, ArrayList<InferenceNode>> seperateByTimestep(){
        HashMap<Integer, ArrayList<InferenceNode>> timesteps = new HashMap<>();
        for(TreeNode tn : this.tree){
            if(tn instanceof InferenceNode){
                int ts = tn.getTimeStep();
                if(timesteps.containsKey(ts)){
                    timesteps.get(ts).add((InferenceNode)tn);
                }
                else{
                    timesteps.put(ts, new ArrayList<>());
                    timesteps.get(ts).add((InferenceNode)tn);
                }
            }
        }
        return timesteps;
    }

//    /**
//     * Gets encoded supporting statements for this TreeManagers Inference Graph.  Each array in the list has shape
//     * (numMaxReasoningSteps, ???)
//     * @return
//     */
//    public List<Double[][]> getSupports(){
//
//    }

//    /**
//     * Gets encoded supporting statements for this TreeManagers Inference Graph.  Each array in the list has shape
//     * (numMaxReasoningSteps, ???)
//     * @return
//     */
//    public List<Double[][]> getOutputs(){
//
//    }
}
