package InferenceTree;

import RDFGraphManipulations.ScaledIntegerMappedEncoding;
import Reasoner.ReasonerLogic;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;

public class TreeManagerTest {

    private static TreeManager tm;

    @Before
    public void setUp(){
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read("testGraph.ttl");

        InfModel inf = null;

        try{
            inf = ReasonerLogic.reasonAndTraceModel(model, "TestRules.txt");
        }
        catch(Exception e){e.printStackTrace();}

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(inf);

        tm = new TreeManager(inf, sie.getEncodedMap());
    }

    @Test
    public void createTreeNodes_TestThatAllTriplesInInfGraphAreConvertedToNodes(){
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
    public void createTree_TestSupportsAreSameAsMatchList(){
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
    public void createTree_TestMakesSureAllSupportsAreActuallyInTree(){
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
    public void encodeStatement_FactEncodingIsCorrectForMapAndActualValue(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());
        HashMap<Double, String> map = tm.encodingMap;

        for(TreeNode node : tree){
            if(node instanceof FactNode){
                List<Double> encoding = node.getEncoding();

                String expected = map.get(encoding.get(0)) + " " + map.get(encoding.get(1)) + " " + map.get(encoding.get(2));
                String actual = node.getValue().getSubject().toString() + " " +
                        node.getValue().getPredicate().toString() + " " + node.getValue().getObject().toString().replaceAll("\"", "");
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void encodeStatement_InfEncodingIsCorrectForMapAndActualValue(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());
        HashMap<Double, String> map = tm.encodingMap;

        for(TreeNode node : tree){
            if(node instanceof InferenceNode){
                List<Double> encoding = node.getEncoding();

                String expected = map.get(encoding.get(0)) + " " + map.get(encoding.get(1)) + " " + map.get(encoding.get(2));
                String actual = node.getValue().getSubject().toString() + " " +
                        node.getValue().getPredicate().toString() + " " + node.getValue().getObject().toString().replaceAll("\"", "");
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void assignTimeStepsAndEncoding_CorrectInferenceNodeSupportEncodingConcatenation(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        List<Double> actual = new ArrayList<>();
        List<Double> expected = new ArrayList<>();

        for(TreeNode tn : tree){
            TreeNode node = tn;
            if(node instanceof InferenceNode){
                tm.assignTimeStepsAndEncoding((InferenceNode)node);
            }
        }//end while

        for(TreeNode node : tree){

            if(node instanceof InferenceNode){
                InferenceNode infNode = (InferenceNode) node;

                if(infNode.support1 instanceof FactNode){
                    expected.addAll(infNode.support1.getEncoding());
                }
                else{
                    InferenceNode in = (InferenceNode) infNode.support1;
                    expected.addAll(in.getSupportEncoding());
                }

                if(infNode.support2 != null){
                    if(infNode.support2 instanceof FactNode){
                        expected.addAll(infNode.support2.getEncoding());
                    }
                    else{
                        InferenceNode in = (InferenceNode) infNode.support2;
                        expected.addAll(in.getSupportEncoding());
                    }
                }

                actual.addAll(((InferenceNode) node).getSupportEncoding());
            }
        }

        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void getKB_IfTreeNotCreatedThenShouldReturnNull(){
        assertEquals(null, tm.getKB(10));
    }

    @Test
    public void getKB_IfTreeCreatedThenShouldNotReturnNull(){
        tm.createTree(tm.createTreeNodes());
        assertFalse(tm.getKB(10).equals(null));
    }

    @Test
    public void getKB_LengthOfEachVectorShouldBeTheSame(){
        tm.createTree(tm.createTreeNodes());
        List<ArrayList<Double>> kb = tm.getKB(1);
        int size = kb.get(0).size();
        for(ArrayList<Double> vect : kb){
            assertEquals(size, vect.size());
        }
    }

    @Test
    public void getKB_ListReturnedIsComplete(){
        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

        List<ArrayList<Double>> kb = tm.getKB(2);
        List<Double> actual = new ArrayList<Double>();

        List<Double> expected = new ArrayList<Double>();
        for(TreeNode node : tree){
            if(node instanceof FactNode){
                expected.addAll(node.getEncoding());
            }
        }
        for(ArrayList<Double> list: kb){
            actual.addAll(list);
        }

        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void getVectorMap_AllVectorsAreTheSame(){
        tm.createTree(tm.createTreeNodes());
        List<HashMap<Double, String>> mapList = tm.getVectorMap(10);

        for(int i=0; i<mapList.size()-1; i++){
            assertEquals(mapList.get(i), mapList.get(i+1));
        }
    }

    @Test
    public void getVectorMap_NumOfVectorMapsIsCorrect(){
        tm.createTree(tm.createTreeNodes());
        List<HashMap<Double, String>> mapList = tm.getVectorMap(14);

        assertEquals(14, mapList.size());
    }

}//end class