package RDFSupportGenerationTree;

import RDFGraphManipulations.ScaledIntegerMappedEncoding;
import Reasoner.ReasonerLogic;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;

public class PackageTreeDataTest {

    private static TreeManager tm = setUp();

    private static TreeManager setUp(){
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read("C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\TestFiles\\gfo-1.0.owl");

        InfModel inf = null;

        try{
            inf = ReasonerLogic.reasonAndTraceModel(model, "TestRules.txt");
        }
        catch(Exception e){e.printStackTrace();}

        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(inf);

        TreeManager treem = new TreeManager(inf, sie.getEncodedMap());
        treem.createTree(treem.createTreeNodes());
        return treem;
    }

//    @Test
//    public void getKB_IfTreeNotCreatedThenShouldReturnNull(){
//        PackageTreeData ptd = new PackageTreeData(tm);
//        assertEquals(null, ptd.getKB(10));
//    }
//
//    @Test
//    public void getKB_IfTreeCreatedThenShouldNotReturnNull(){
//        PackageTreeData ptd = new PackageTreeData(tm);
//        tm.createTree(tm.createTreeNodes());
//        assertFalse(ptd.getKB(10).equals(null));
//    }
//
//    @Test
//    public void getKB_LengthOfEachVectorShouldBeTheSame(){
//        PackageTreeData ptd = new PackageTreeData(tm);
//        tm.createTree(tm.createTreeNodes());
//        List<ArrayList<Double>> kb = ptd.getKB(1);
//        int size = kb.get(0).size();
//        for(ArrayList<Double> vect : kb){
//            assertEquals(size, vect.size());
//        }
//    }
//
//    @Test
//    public void getKB_ListReturnedIsComplete(){
//        PackageTreeData ptd = new PackageTreeData(tm);
//
//        List<TreeNode> tree = tm.createTree(tm.createTreeNodes());
//
//        List<ArrayList<Double>> kb = ptd.getKB(2);
//        List<Double> actual = new ArrayList<Double>();
//
//        List<Double> expected = new ArrayList<Double>();
//        for(TreeNode node : tree){
//            if(node instanceof FactNode){
//                expected.addAll(node.getEncoding());
//            }
//        }
//        for(ArrayList<Double> list: kb){
//            actual.addAll(list);
//        }
//
//        assertArrayEquals(expected.toArray(), actual.toArray());
//    }

    @Test
    public void getVectorMap_AllVectorsAreTheSame(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<HashMap<Double, String>> mapList = ptd.getVectorMap(10);

        for(int i=0; i<mapList.size()-1; i++){
            assertEquals(mapList.get(i), mapList.get(i+1));
        }
    }

    @Test
    public void getVectorMap_NumOfVectorMapsIsCorrect(){

        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<HashMap<Double, String>> mapList = ptd.getVectorMap(14);

        assertEquals(14, mapList.size());
    }

    @Test
    public void assignFactsToInferenceNodes_MapShouldContainAllInferencesAndOnlyInferences() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = PackageTreeData.class.getDeclaredMethod("assignFactsToInferenceNodes");
        method.setAccessible(true);

        PackageTreeData ptd = new PackageTreeData(tm, 20);
        HashMap<InferenceNode, List<FactNode>> map  = (HashMap<InferenceNode, List<FactNode>>) method.invoke(ptd);

        int counter = 0;
        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                counter++;
            }
        }

        assertEquals(counter,map.size());
    }

    @Test
    public void findAllFactsForInference_CheckAgainstSintheticData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Create Synthetic tree of nodes
        Model model = ModelFactory.createDefaultModel();
        Triple t1 = model.createStatement(model.createResource("s1"), model.createProperty("p1"),
                model.createResource("l1")).asTriple();
        Triple t2 = model.createStatement(model.createResource("s2"), model.createProperty("p2"),
                model.createResource("l2")).asTriple();
        Triple t3 = model.createStatement(model.createResource("s3"), model.createProperty("p3"),
                model.createResource("l3")).asTriple();
        Triple t4 = model.createStatement(model.createResource("s4"), model.createProperty("p4"),
                model.createResource("l4")).asTriple();
        Triple t5 = model.createStatement(model.createResource("s5"), model.createProperty("p5"),
                model.createResource("l5")).asTriple();

        Triple f1 = model.createStatement(model.createResource("f1"), model.createProperty("fp1"),
                model.createResource("ff1")).asTriple();
        Triple f2 = model.createStatement(model.createResource("f2"), model.createProperty("fp2"),
                model.createResource("ff2")).asTriple();
        Triple f3 = model.createStatement(model.createResource("f3"), model.createProperty("fp3"),
                model.createResource("ff3")).asTriple();
        Triple f4 = model.createStatement(model.createResource("f4"), model.createProperty("fp4"),
                model.createResource("ff4")).asTriple();

        List<Triple> match1 = new ArrayList<Triple>();
        match1.add(t2);
        match1.add(t3);
        List<Triple> match2 = new ArrayList<Triple>();
        match2.add(t4);
        match2.add(f1);
        List<Triple> match3 = new ArrayList<Triple>();
        match3.add(t5);
        List<Triple> match4 = new ArrayList<Triple>();
        match4.add(f2);
        match4.add(f3);
        List<Triple> match5 = new ArrayList<Triple>();
        match5.add(f4);

        RuleDerivation rd1 = new RuleDerivation(null,t1,match1,null);
        RuleDerivation rd2 = new RuleDerivation(null,t2,match2,null);
        RuleDerivation rd3 = new RuleDerivation(null,t3,match3,null);
        RuleDerivation rd4 = new RuleDerivation(null,t4,match4,null);
        RuleDerivation rd5 = new RuleDerivation(null,t5,match5,null);

        InferenceNode inf1 = new InferenceNode(rd1, model);
        InferenceNode inf2 = new InferenceNode(rd2, model);
        InferenceNode inf3 = new InferenceNode(rd3, model);
        InferenceNode inf4 = new InferenceNode(rd4, model);
        InferenceNode inf5 = new InferenceNode(rd5, model);

        FactNode fact1 = new FactNode(f1);
        FactNode fact2 = new FactNode(f2);
        FactNode fact3 = new FactNode(f3);
        FactNode fact4 = new FactNode(f4);

        inf1.support1 = inf2;
        inf1.support2 = inf3;

        inf2.support1 = inf4;
        inf2.support2 = fact1;

        inf3.support1 = inf5;

        inf4.support1 = fact2;
        inf4.support2 = fact3;

        inf5.support1 = fact4;

        List<FactNode> expected = new ArrayList<>();
        expected.add(fact2);
        expected.add(fact3);
        expected.add(fact1);
        expected.add(fact4);

        //Use actual method on synthetic data
        Method method = PackageTreeData.class.getDeclaredMethod("findAllFactsForInference", TreeNode.class);
        method.setAccessible(true);
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        List<FactNode> actual = (List<FactNode>) method.invoke(ptd, inf1);

        //Check for same results
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void setMessyKBAndOuts_MessyDatasAreSameSize(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        assertEquals(ptd.getMessyKB().size(), ptd.getMessyOutputs().size());
    }

    @Test
    public void setMessyKBAndOuts_MakeSureAllInferencesArePresentInMessyData(){
        for (TreeNode tn : tm.tree) {
            TreeNode node = tn;
            if (node instanceof InferenceNode) {
                tm.assignTimeStepsAndEncoding((InferenceNode) node);
            }
        }//end while

        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<InferenceNode> expectedInferences = new ArrayList<>();
        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                expectedInferences.add((InferenceNode) tn);
            }
        }

        List<InferenceNode> actualInferences = new ArrayList<>();
        for(ArrayList<InferenceNode> list : ptd.getMessyOutputs()){
            for(InferenceNode inf : list){
                if(!actualInferences.contains(inf)){
                    actualInferences.add(inf);
                }
            }
        }

        for(InferenceNode node : expectedInferences){
            assertTrue(actualInferences.contains(node));
            actualInferences.remove(node);
        }

        assertEquals(0, actualInferences.size());
    }

    @Test
    public void setMessyKBAndOuts_NoGapsInInferenceTimeSteps(){

    }

//    @Test
//    public void setMessyKBAndOuts_AllKGAxiomsPresentForInferences(){
//        for (TreeNode tn : tm.tree) {
//            TreeNode node = tn;
//            if (node instanceof InferenceNode) {
//                tm.assignTimeStepsAndEncoding((InferenceNode) node);
//            }
//        }
//
//        PackageTreeData ptd = new PackageTreeData(tm, 20);
//
//        List<ArrayList<InferenceNode>> infLists = ptd.getMessyOutputs();
//        List<ArrayList<FactNode>> factLists = ptd.getMessyKB();
//
//        int counter = 0;
//        for(ArrayList<InferenceNode> infList : infLists){
//            List<FactNode> tempFacts = new ArrayList<>();
//            for(InferenceNode inf : infList){
//                tempFacts.addAll(ptd.getMessyInfToFactsMap().get(inf));
//            }
//            for(FactNode fact : tempFacts){
//                assertTrue(factLists.get(counter).contains(fact));
//            }
//            counter++;
//        }
//    }

    @Test
    public void shouldAddFacts_CaseWhereDesiredTimeStepAndTSAtIndexAreEqual() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //List inferences, List<Boolean> timeStepAdded, int index
        PackageTreeData pd = new PackageTreeData(tm, 20);
        Method assignFactsToInferenceNodes = PackageTreeData.class.getDeclaredMethod("assignFactsToInferenceNodes");
        assignFactsToInferenceNodes.setAccessible(true);
        HashMap<InferenceNode, List<FactNode>> messyMap = (HashMap<InferenceNode, List<FactNode>>) assignFactsToInferenceNodes.invoke(pd);

        List rawInferences = new ArrayList(Arrays.asList(messyMap.keySet().toArray()));
        Iterator tempIter = pd.getMessyInfToFactsMap().values().iterator();
        List<List<FactNode>> tempFacts = new ArrayList<>();
        tempIter.forEachRemaining(fact -> tempFacts.add((List<FactNode>) fact));
        for (Object tn : rawInferences) {
            TreeNode node = (TreeNode) tn;
            if (node instanceof InferenceNode) {
                tm.assignTimeStepsAndEncoding((InferenceNode) node);
            }
        }//end while
        int index = 0;
        while(index < rawInferences.size()){
            InferenceNode inf = (InferenceNode) rawInferences.get(index);
            if(inf.getTimeStep() == 2){
                break;
            }
            index++;
        }

        List<Boolean> timeStepAdded = new ArrayList<Boolean>();
        timeStepAdded.add(true);
        for(int i=0; i<4; i++){
            timeStepAdded.add(false);
        }
        Class[] args = new Class[5];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(true, method.invoke(pd, rawInferences, tempFacts, timeStepAdded, index, 20));
    }

    @Test
    public void shouldAddFacts_CaseWhereEveryTimeStepIsFilled() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //List inferences, List<Boolean> timeStepAdded, int index
        PackageTreeData pd = new PackageTreeData(tm, 20);
        Method assignFactsToInferenceNodes = PackageTreeData.class.getDeclaredMethod("assignFactsToInferenceNodes");
        assignFactsToInferenceNodes.setAccessible(true);
        HashMap<InferenceNode, List<FactNode>> messyMap = (HashMap<InferenceNode, List<FactNode>>) assignFactsToInferenceNodes.invoke(pd);

        List rawInferences = new ArrayList(Arrays.asList(messyMap.keySet().toArray()));
        Iterator tempIter = pd.getMessyInfToFactsMap().values().iterator();
        List<List<FactNode>> tempFacts = new ArrayList<>();
        tempIter.forEachRemaining(fact -> tempFacts.add((List<FactNode>) fact));
        for (Object tn : rawInferences) {
            TreeNode node = (TreeNode) tn;
            if (node instanceof InferenceNode) {
                tm.assignTimeStepsAndEncoding((InferenceNode) node);
            }
        }//end while
        int index = 0;
        while(index < rawInferences.size()){
            InferenceNode inf = (InferenceNode) rawInferences.get(index);
            if(inf.getTimeStep() == 2){
                break;
            }
            index++;
        }

        List<Boolean> timeStepAdded = new ArrayList<Boolean>();
        timeStepAdded.add(true);
        for(int i=0; i<4; i++){
            timeStepAdded.add(true);
        }

        Class[] args = new Class[5];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(true, method.invoke(pd, rawInferences, tempFacts, timeStepAdded, index, 20));
    }

    @Test
    public void shouldAddFacts_CaseWhereDesiredValueIsNotInTheList() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //List inferences, List<Boolean> desiredTimesteps, int index
        PackageTreeData pd = new PackageTreeData(tm, 20);
        Method assignFactsToInferenceNodes = PackageTreeData.class.getDeclaredMethod("assignFactsToInferenceNodes");
        assignFactsToInferenceNodes.setAccessible(true);
        HashMap<InferenceNode, List<FactNode>> messyMap = (HashMap<InferenceNode, List<FactNode>>) assignFactsToInferenceNodes.invoke(pd);

        List rawInferences = new ArrayList(Arrays.asList(messyMap.keySet().toArray()));
        Iterator tempIter = pd.getMessyInfToFactsMap().values().iterator();
        List<List<FactNode>> tempFacts = new ArrayList<>();
        tempIter.forEachRemaining(fact -> tempFacts.add((List<FactNode>) fact));
        for (Object tn : rawInferences) {
            TreeNode node = (TreeNode) tn;
            if (node instanceof InferenceNode) {
                tm.assignTimeStepsAndEncoding((InferenceNode) node);
            }
        }//end while
        int index = 10;

        List<Boolean> desiredTimesteps = new ArrayList<Boolean>();
        for(int i=0; i<8; i++){
            desiredTimesteps.add(true);
        }
        desiredTimesteps.add(false);

        Class[] args = new Class[5];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(true, method.invoke(pd, rawInferences, tempFacts, desiredTimesteps, index, 20));
    }

    @Test
    public void shouldAddFacts_CaseWhereDesiredTimeStepIsInListJustNotAtIndex() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //List inferences, List<Boolean> timeStepAdded, int index
        PackageTreeData pd = new PackageTreeData(tm, 20);
        Method assignFactsToInferenceNodes = PackageTreeData.class.getDeclaredMethod("assignFactsToInferenceNodes");
        assignFactsToInferenceNodes.setAccessible(true);
        HashMap<InferenceNode, List<FactNode>> messyMap = (HashMap<InferenceNode, List<FactNode>>) assignFactsToInferenceNodes.invoke(pd);

        List rawInferences = new ArrayList(Arrays.asList(messyMap.keySet().toArray()));
        Iterator tempIter = pd.getMessyInfToFactsMap().values().iterator();
        List<List<FactNode>> tempFacts = new ArrayList<>();
        tempIter.forEachRemaining(fact -> tempFacts.add((List<FactNode>) fact));
        for (Object tn : rawInferences) {
            TreeNode node = (TreeNode) tn;
            if (node instanceof InferenceNode) {
                tm.assignTimeStepsAndEncoding((InferenceNode) node);
            }
        }//end while
        int index = 0;
        while(index < rawInferences.size()){
            InferenceNode inf = (InferenceNode) rawInferences.get(index);
            if(inf.getTimeStep() == 3){
                break;
            }
            index++;
        }

        List<Boolean> timeStepAdded = new ArrayList<Boolean>();
        timeStepAdded.add(false);
        for(int i=0; i<2; i++){
            timeStepAdded.add(true);
        }

        Class[] args = new Class[5];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(false, method.invoke(pd, rawInferences, tempFacts, timeStepAdded, index, 20));
    }
}