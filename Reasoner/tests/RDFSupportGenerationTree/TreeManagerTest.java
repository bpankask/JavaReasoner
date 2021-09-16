package RDFSupportGenerationTree;

import RDFGraphManipulations.ScaledIntegerMappedEncoding;
import Reasoner.ReasonerLogic;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TreeManagerTest {

    private static TreeManager tm;

    @Before
    public void setUp(){
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read("C:\\Users\\Brayden Pankaskie\\Desktop\\JavaReasoner\\Reasoner\\tests\\gfo-1.0.owl");

        InfModel inf = null;

        try{
            inf = ReasonerLogic.reasonAndTraceModel(model, "TestRules.txt");
        }
        catch(Exception e){e.printStackTrace();}

        tm = new TreeManager(inf);
    }

    @Test
    public void createTreeNodes_AllTriplesInInfGraphAreConvertedToNodes(){
        Hashtable<String, TreeNode> nodeTable = tm.createTreeNodes();
        int tableSize = nodeTable.size();

        StmtIterator iter = tm.getInfModelUsed().listStatements();
        while(iter.hasNext()){
            Triple t = iter.nextStatement().asTriple();

            //Checks to make sure everything in Inference Model is in the table.
            assertTrue(nodeTable.containsKey(t.toString()));

            //Checks to see if key and value matches correctly.
            assertEquals(t, nodeTable.get(t.toString()).getValue());
            tableSize -= 1;
        }

        //Makes sure that there is not more in the table then there is in the inf graph.
        assertEquals(0, tableSize);
    }

    @Test
    public void createTreeNodes_AllFactKBTriplesAreFactNodes(){
        Hashtable<String, TreeNode> nodeTable = tm.createTreeNodes();

        StmtIterator iter = tm.getInfModelUsed().getRawModel().listStatements();
        while(iter.hasNext()){
            Triple t = iter.nextStatement().asTriple();

            //Checks to see if key and value matches correctly.
            assertTrue(nodeTable.get(t.toString()) instanceof FactNode);
        }
    }

    @Test
    public void createTreeNodes_AllInferenceTriplsAreInferenceNodesInGraph(){
        Hashtable<String, TreeNode> nodeTable = tm.createTreeNodes();

        StmtIterator iter = tm.getInfModelUsed().getDeductionsModel().listStatements();
        while(iter.hasNext()){
            Triple t = iter.nextStatement().asTriple();

            //Checks to see if key and value matches correctly.
            assertTrue(nodeTable.get(t.toString()) instanceof InferenceNode);
        }
    }

    @Test
    public void createTree_SupportsAreSameAsMatchList(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        List<Triple> testSupports = new ArrayList<>();
        List<Triple> actualSupports = new ArrayList<>();

        for(TreeNode tn : tree){
            if(tn instanceof InferenceNode){

                InferenceNode in = (InferenceNode) tn;

                testSupports.add(in.support1.getValue());
                if(in.support2 != null){
                    testSupports.add(in.support2.getValue());
                }
                else{
                    Triple t = null;
                    testSupports.add(t);
                }

                actualSupports.add(in.matchList.get(0));
                if(in.matchList.get(1) != null){
                    actualSupports.add(in.matchList.get(1));
                }
                else{
                    Triple t = null;
                    actualSupports.add(t);
                }
            }
        }//end for

        assertArrayEquals(actualSupports.toArray(), testSupports.toArray());
    }

    @Test
    public void createTree_AllSupportsAreTreeNodes(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());
        List<Boolean> supportsInTreeInfo = new ArrayList<>();

        for(TreeNode tn : tree){
            if(tn instanceof InferenceNode){

                InferenceNode in = (InferenceNode) tn;

                supportsInTreeInfo.add(tree.contains(in.support1));
                if(in.support2 != null){
                    supportsInTreeInfo.add(tree.contains(in.support2));
                }
            }
        }

        boolean test = supportsInTreeInfo.contains(false);
        assertFalse(test);
    }

    @Test
    public void assignTimeSteps_AllInferenceNodesHaveTimestepGreaterThanZero(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        //Assign time steps for each tree node.
        for (TreeNode tn : tree) {
            if (tn instanceof InferenceNode) {
                tm.assignTimeSteps((InferenceNode) tn);
            }
        }//end while

        for(TreeNode tn : tree){
            if(tn instanceof InferenceNode){
                assertTrue(tn.getTimeStep() > 0);
            }
        }

    }

    @Test
    public void assignTimeSteps_InferenceNodesHaveTimestepsThatAreGreaterThanMaxSupportTimestepByOne(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        //Assign time steps for each tree node.
        for (TreeNode tn : tree) {
            if (tn instanceof InferenceNode) {
                tm.assignTimeSteps((InferenceNode) tn);
            }
        }//end while

        for(TreeNode tn : tree){
            if (tn instanceof InferenceNode) {
                int supp1 = ((InferenceNode) tn).support1.getTimeStep();
                int supp2 = 0;
                if(((InferenceNode) tn).support2 != null){
                    supp2 = ((InferenceNode) tn).support2.getTimeStep();
                }

                assertEquals(Math.max(supp1, supp2)+1, tn.getTimeStep());
            }
        }
    }

    @Test
    public void seperateByTimestep_CorrectSplitBetweenTimeStepLists(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        //Assign time steps for each tree node.
        for (TreeNode tn : tree) {
            if (tn instanceof InferenceNode) {
                tm.assignTimeSteps((InferenceNode) tn);
            }
        }//end while

        HashMap<Integer, ArrayList<InferenceNode>> map = tm.seperateByTimestep();

        for(Map.Entry<Integer, ArrayList<InferenceNode>> entry : map.entrySet()){
            for(InferenceNode in : entry.getValue()){
                assertEquals((int)entry.getKey(), in.getTimeStep());
            }
        }
    }

    @Test
    public void seperateByTimestep_AllInferencesPresent(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        //Assign time steps for each tree node.
        for (TreeNode tn : tree) {
            if (tn instanceof InferenceNode) {
                tm.assignTimeSteps((InferenceNode) tn);
            }
        }//end while

        HashMap<Integer, ArrayList<InferenceNode>> map = tm.seperateByTimestep();

        int countTree = 0;
        for(TreeNode tn : tree){
            if(tn instanceof InferenceNode){
                assertTrue(map.get(tn.getTimeStep()).contains(tn));
                countTree++;
            }
        }

        int countMap = 0;
        for(Map.Entry<Integer, ArrayList<InferenceNode>> entry : map.entrySet()){
            countMap += entry.getValue().size();
        }

        assertEquals(countTree, countMap);
    }
}//end class