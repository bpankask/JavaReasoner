package RDFSupportGenerationTree;

import RDFGraphManipulations.ScaledIntegerMappedEncoding;
import Reasoner.ReasonerLogic;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
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
        for (TreeNode tn : treem.tree) {
            TreeNode node = tn;
            if (node instanceof InferenceNode) {
                treem.assignTimeStepsAndSuppEncoding((InferenceNode) node);
            }
        }//end while
        return treem;
    }

    //Polished data-----------------------------------------------------------------------------------------------------
    @Test
    public void getKBEncoded_LengthOfEachVectorShouldBeTheSame(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        List<ArrayList<Double>> kb = ptd.getKBEncoded();
        int size = kb.get(0).size();
        for(ArrayList<Double> vect : kb){
            assertEquals(size, vect.size());
        }
    }

    @Test
    public void getKBEncoded_CanConvertBackToSameTriplesAsMessyData(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        List<ArrayList<Double>> kb = ptd.getKBEncoded();

        List<ArrayList<Triple>> actual = new ArrayList<ArrayList<Triple>>();
        for(ArrayList<Double> encodedSample : kb){
            ArrayList<Triple> temp = new ArrayList<>();
            List<String> split = new ArrayList<String>();

            for(int i=1; i<= encodedSample.size(); i++){
                String tString = tm.encodingMap.get(encodedSample.get(i-1));
                split.add(tString);
                if(i % 3 == 0){
                    temp.add(tm.infModel.createStatement(tm.infModel.getResource(split.get(0)), tm.infModel.getProperty(split.get(1)),
                            tm.infModel.getResource(split.get(2))).asTriple());
                    split.clear();
                }
            }
            actual.add(temp);
            temp.clear();
        }

        List<ArrayList<Triple>> expected = new ArrayList<ArrayList<Triple>>();
        for(ArrayList<FactNode> sample : ptd.getMessyKB()){
            ArrayList<Triple> temp = new ArrayList<>();
            for(FactNode fact : sample){
                temp.add(fact.getValue());
            }
            expected.add(temp);
            temp.clear();
        }

        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void getKBEncoded_SameLengthAsMessyData(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        List<ArrayList<FactNode>> messyKB = ptd.getMessyKB();
        List<ArrayList<Double>> kb = ptd.getKBEncoded();

        int size = messyKB.get(0).size();

        for(ArrayList<Double> factList : kb){
            assertEquals(size, factList.size()/3);
        }
    }

    @Test
    public void getOutputsEncoded_CanConvertBackToSameTriplesAsMessyData() throws NoSuchMethodException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<ArrayList[]> results = ptd.getOutputsEncoded();

        List<ArrayList<Triple>> actual = new ArrayList<>();
        List<ArrayList<Triple>> expected = new ArrayList<>();
        List<ArrayList<InferenceNode>> messyOuts = ptd.getMessyOutputs();

        //Each sample split into timesteps
        for(int j=0; j<results.size(); j++){
            ArrayList<Triple> temp = new ArrayList<>();
            //Each timestep list
            for(ArrayList list : results.get(j)){
                //Each double in a timestep
                List<String> split = new ArrayList<>();
                for(int i=1; i<=list.size(); i++){
                    String tString = tm.encodingMap.get(list.get(i-1));
                    split.add(tString);
                    if(i % 3 == 0){
                        temp.add(tm.infModel.createStatement(tm.infModel.getResource(split.get(0)), tm.infModel.getProperty(split.get(1)),
                                tm.infModel.getResource(split.get(2))).asTriple());
                        split.clear();
                    }
                }
            }
            actual.add(temp);
        }

        //Gets expected values
        for(ArrayList<InferenceNode> list : messyOuts){
            ArrayList<Triple> temp1 = new ArrayList<>();
            for(InferenceNode inf : list){
                temp1.add(inf.getValue());
            }
            expected.add(temp1);
        }

        for(int i=0; i<expected.size(); i++) {
            for (Triple t : expected.get(i)) {
                boolean cont = false;
                for(Triple t1 : actual.get(i)){
                    if(t1.toString().equals(t.toString())){
                        cont = true;
                    }
                }
                assertTrue(cont);
            }
        }

        for(int i=0; i<actual.size(); i++) {
            for (Triple t : actual.get(i)) {
                boolean cont = false;
                for(Triple t1 : expected.get(i)){
                    if(t1.toString().equals(t.toString())){
                        cont = true;
                    }
                }
                assertTrue(cont);
            }
        }
    }

    @Test
    public void separateTimeSteps_AllTimeStepArraysAreSameLength() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        Method method = PackageTreeData.class.getDeclaredMethod("separateTimeSteps", new Class[]{List.class});
        method.setAccessible(true);

        List<ArrayList[]> results = (List<ArrayList[]>) method.invoke(ptd, ptd.getMessyOutputs());
        int maxTimeStep = tm.tree.get(0).getTimeStep();
        for(ArrayList[] arr : results){
            assertEquals(maxTimeStep, arr.length);
        }
    }

    @Test
    public void separateTimeSteps_TimeStepIsCorrectForPositionInArray() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        Method method = PackageTreeData.class.getDeclaredMethod("separateTimeSteps", new Class[]{List.class});
        method.setAccessible(true);

        List<ArrayList[]> results = (List<ArrayList[]>) method.invoke(ptd, ptd.getMessyOutputs());
        int maxTimeStep = tm.tree.get(0).getTimeStep();
        for(ArrayList[] timeStepArray : results){
            for(int i=0; i<timeStepArray.length; i++){
                for(Object obj : timeStepArray[i]){
                    InferenceNode inf = (InferenceNode) obj;
                    assertEquals(i+1, inf.getTimeStep());
                }
            }
        }
    }

    @Test
    public void separateTimeSteps_SameInferencesAsMessyData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        Method method = PackageTreeData.class.getDeclaredMethod("separateTimeSteps", new Class[]{List.class});
        method.setAccessible(true);

        List<ArrayList[]> results = (List<ArrayList[]>) method.invoke(ptd, ptd.getMessyOutputs());
        for(int j=0; j<results.size(); j++){
            ArrayList<InferenceNode> actual = new ArrayList<>();
            for(int i=0; i<results.get(j).length; i++){
                for(Object obj : results.get(j)[i]){
                    InferenceNode inf = (InferenceNode) obj;
                    assertTrue(ptd.getMessyOutputs().get(j).contains(inf));
                    actual.add(inf);
                }
            }
            assertEquals(ptd.getMessyOutputs().get(j).size(), actual.size() );
        }
    }

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
    public void findAllFactsForInference_CheckAgainstSyntheticData() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

    //Messy Data Tests--------------------------------------------------------------------------------------------------
    @Test
    public void setMessyKBAndOuts_MessyDataSamplesAreSameSize(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        assertEquals(ptd.getMessyKB().size(), ptd.getMessyOutputs().size());
    }

    @Test
    public void setMessyKBAndOuts_MakeSureAllInferencesArePresentInMessyData(){
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
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        for(ArrayList<InferenceNode> outs : ptd.getMessyOutputs()){
            ArrayList<Boolean> gapCheck = new ArrayList<Boolean>();
            for(int i=0; i<tm.tree.get(0).getTimeStep(); i++){
                gapCheck.add(false);
            }

            for(InferenceNode inf : outs){
                gapCheck.set(inf.getTimeStep()-1, true);
            }

            int index = gapCheck.indexOf(false);
            if(index != -1) {
                for (int i = index; i < gapCheck.size(); i++) {
                    assertFalse(gapCheck.get(i));
                }
            }
        }
    }

    @Test
    public void setMessyKBAndOuts_AllKBAxiomsPresentForInferences(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<ArrayList<InferenceNode>> infLists = ptd.getMessyOutputs();
        List<ArrayList<FactNode>> factLists = ptd.getMessyKB();

        int counter = 0;
        for(ArrayList<InferenceNode> infList : infLists){
            List<FactNode> tempFacts = new ArrayList<>();
            for(InferenceNode inf : infList){
                tempFacts.addAll(ptd.getMessyInfToFactsMap().get(inf));
            }
            for(FactNode fact : tempFacts){
                assertTrue(factLists.get(counter).contains(fact));
            }
            counter++;
        }
    }

    //Helper Methods----------------------------------------------------------------------------------------------------

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
                tm.assignTimeStepsAndSuppEncoding((InferenceNode) node);
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
        Class[] args = new Class[6];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;
        args[5] = boolean.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(true, method.invoke(pd, rawInferences, tempFacts, timeStepAdded, index, 20, false));
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
                tm.assignTimeStepsAndSuppEncoding((InferenceNode) node);
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

        Class[] args = new Class[6];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;
        args[5] = boolean.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(true, method.invoke(pd, rawInferences, tempFacts, timeStepAdded, index, 20, false));
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
                tm.assignTimeStepsAndSuppEncoding((InferenceNode) node);
            }
        }//end while
        int index = 10;

        List<Boolean> desiredTimesteps = new ArrayList<Boolean>();
        for(int i=0; i<8; i++){
            desiredTimesteps.add(true);
        }
        desiredTimesteps.add(false);

        Class[] args = new Class[6];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;
        args[5] = boolean.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(true, method.invoke(pd, rawInferences, tempFacts, desiredTimesteps, index, 20, true));
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
                tm.assignTimeStepsAndSuppEncoding((InferenceNode) node);
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

        Class[] args = new Class[6];
        args[0] = List.class;
        args[1] = List.class;
        args[2] = List.class;
        args[3] = int.class;
        args[4] = int.class;
        args[5] = boolean.class;

        Method method = PackageTreeData.class.getDeclaredMethod("shouldAddFacts", args);
        method.setAccessible(true);

        assertEquals(false, method.invoke(pd, rawInferences, tempFacts, timeStepAdded, index, 20, true));
    }

    @Test
    public void makeSureTreeSupportEncodingsContainAllFactsThatWereUsedInItsCreation(){
        tm.createTree(tm.createTreeNodes());
        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                tm.assignTimeStepsAndSuppEncoding(tn);
            }
        }


    }
}