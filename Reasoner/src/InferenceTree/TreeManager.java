package InferenceTree;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.rulesys.RuleDerivation;

import java.util.*;

public class TreeManager {

    private InfModel infModel;
    private Model baseModel;
    private List<TreeNode> tree = null;
    public HashMap<Double, String> encodingMap;

    public TreeManager(InfModel inf, HashMap<Double, String> map){
        this.infModel = inf;
        baseModel = inf.getRawModel();
        this.encodingMap = map;
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
                    fn.setEncoding(encodeStatement(s));
                    tableOfNodes.put(s.asTriple().toString(), fn);
                }
                else {
                    //Gets all the Derivations from a given statement/triple.
                    for (Iterator id = infModel.getDerivation(s); id.hasNext(); ) {

                        //Creates node from derivation/inference.
                        RuleDerivation deriv = (RuleDerivation) id.next();
                        InferenceNode node = new InferenceNode(deriv, infModel);
                        node.setEncoding(encodeStatement(s));

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
    public int assignTimeStepsAndEncoding(TreeNode node){
        if(node.getTimeStep() > 0)
            return node.getTimeStep() + 1;

        if(node instanceof FactNode)
            return 1;

        InferenceNode infNode = (InferenceNode) node;
        int v1 = 0;
        v1 += assignTimeStepsAndEncoding(infNode.support1);

        int v2 = 0;
        if(infNode.support2 != null){
            v2 += assignTimeStepsAndEncoding(infNode.support2);
        }

        if(infNode.getSupportEncoding() == null){
            infNode.setSupportEncoding();
        }

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
     * Gets the KB encoded vectors for this TreeManagers knowledge graph.
     * @param numStmPerSample Approximate number of desired triples per sample.  Will decrease or increase to make even number of samples.
     * @return
     */
    public List<ArrayList<Double>> getKB(int numStmPerSample){
        if(this.tree != null){
            long sizeKB = baseModel.size();
            while(sizeKB % numStmPerSample != 0){
                numStmPerSample--;
            }

            List<ArrayList<Double>> kB = new ArrayList<ArrayList<Double>>();
            ArrayList<Double> curList = new ArrayList<Double>();

            for(int i=0; i<this.tree.size(); i++){
                if(tree.get(i) instanceof FactNode){
                    curList.addAll(tree.get(i).getEncoding());
                }
                if(curList.size()/3 == numStmPerSample){
                    kB.add(curList);
                    curList = new ArrayList<Double>();
                }
            }
            return kB;
        }
        else{
            return null;
        }
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

    /**
     * Gets a mapping from a double to its corresponding string which is a subject, predicate, or object of a triple.
     * @return
     */
    public List<HashMap<Double, String>> getVectorMap(int listLength){
        List<HashMap<Double, String>> listMap = new ArrayList<>();
        for(int i=0; i<listLength; i++){
            listMap.add((HashMap) encodingMap);
        }
        return listMap;
    }
}
