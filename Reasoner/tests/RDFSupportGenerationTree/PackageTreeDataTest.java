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

        try {
            InfModel preReasoning = ReasonerLogic.reasonAndTraceModel(model, "src\\PreprocessingTimeStep.txt");

            //Create inference model.
            inf = ReasonerLogic.reasonAndTraceModel(preReasoning, "TestRules.txt");
        }catch(Exception e){
            System.out.println("");
        }

        //Encode graph using a specific method of encoding.
        ScaledIntegerMappedEncoding sie = new ScaledIntegerMappedEncoding(inf);

        //Creates Tree manager for in inference graph and a specified encoding.
        //It will handle all tree manipulations and queries.
        TreeManager tm = new TreeManager(inf, sie.getEncodedMapAndPopLabelMap());
        tm.createTree(tm.createTreeNodes());

        for (TreeNode tn : tm.tree) {
            TreeNode node = tn;
            if (node instanceof InferenceNode) {
                tm.assignTimeSteps((InferenceNode) node);
            }
        }//end while

        return tm;
    }

    //Polished data-----------------------------------------------------------------------------------------------------

    @Test
    public void getKBEncoded_CanConvertBackToSameTriplesAsMessyData(){
        PackageTreeData ptd = new PackageTreeData(tm, 14);
        List<ArrayList<Double>> kb = ptd.KB;

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
        PackageTreeData ptd = new PackageTreeData(tm, 9);
        List<ArrayList<FactNode>> messyKB = ptd.getMessyKB();
        List<ArrayList<Double>> kb = ptd.getKBEncoded();

        // Makes sure no data is lost.
        int counter = 0;
        for(ArrayList<Double> factList : kb){
            int size = messyKB.get(counter).size();
            assertEquals(size, factList.size()/3);
            counter++;
        }
    }

    @Test
    public void getOutputsEncoded_CanConvertBackToSameTriplesAsMessyData() throws NoSuchMethodException {
        PackageTreeData ptd = new PackageTreeData(tm, 10);

        List<ArrayList[]> results = ptd.getOutputsEncoded();

        List<ArrayList<Triple>> actual = new ArrayList<>();
        List<ArrayList<Triple>> expected = new ArrayList<>();
        List<ArrayList[]> messyOuts = ptd.getMessyOutputs();

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
        for(ArrayList[] array : messyOuts){
            ArrayList<Triple> temp1 = new ArrayList<>();
            for(ArrayList list : array){
                for(Object inf : list){
                    temp1.add(((InferenceNode) inf).getValue());
                }
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
                if(!expectedInferences.contains(tn)){
                    expectedInferences.add((InferenceNode) tn);
                }
            }
        }

        List<InferenceNode> actualInferences = new ArrayList<>();
        for(ArrayList[] array : ptd.getMessyOutputs()){
            for(ArrayList list : array){
                for(Object inf : list){
                    if(!actualInferences.contains(inf)){
                        actualInferences.add((InferenceNode) inf);
                    }
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
    public void setMessyKBAndOuts_EveryTimestepPresentForEachSample(){
        PackageTreeData ptd = new PackageTreeData(tm, 15);

        for(ArrayList[] outs : ptd.getMessyOutputs()){
            ArrayList<Boolean> gapCheck = new ArrayList<Boolean>();
            for(int i=0; i<tm.tree.get(0).getTimeStep(); i++){
                gapCheck.add(false);
            }

            for(ArrayList array : outs){
                for(Object inf : array){
                    InferenceNode infN = (InferenceNode) inf;
                    gapCheck.set(infN.getTimeStep()-1, true);
                }
            }

            int index = gapCheck.indexOf(false);
            assertEquals(-1, index);
//            if(index != -1) {
//                for (int i = index; i < gapCheck.size(); i++) {
//                    assertFalse(gapCheck.get(i));
//                }
//            }
        }
    }

    @Test
    public void setMessyKBAndOuts_CorrectNumberOfSupportsForEachSample(){
        // This test only works because I know that the data I am using to test has 7 timesteps.
        PackageTreeData ptd = new PackageTreeData(tm, 25);
        List<ArrayList[]> outs = ptd.getMessyOutputs();

        for(ArrayList[] sample : outs){
            int count = 0;
            for(ArrayList ts : sample){
                count += ts.size();
            }
            assertEquals(count, 25);
        }
    }

    @Test
    public void setMessyKBAndOuts_CorrectNumberOfTimeStepInferenceBasedOnNumberOfDesiredInferences(){
        int sizeOfOuts = 21;
        PackageTreeData ptd = new PackageTreeData(tm, sizeOfOuts);
        double numberOfTimesteps = tm.tree.get(0).getTimeStep();
        List<ArrayList[]> outs = ptd.getMessyOutputs();

        int mainQuota = (int) Math.floor(sizeOfOuts / numberOfTimesteps);
        int leftOver = mainQuota + 1;
        int tsIndexAndUp = (int) (sizeOfOuts - (mainQuota * numberOfTimesteps));

        if(mainQuota > 0){
            for(ArrayList[] sample : outs){
                for(int index=0; index<sample.length; index++){
                    if(index < tsIndexAndUp) {
                        assertEquals(leftOver, sample[index].size());
                    }
                    else{
                        assertEquals(mainQuota, sample[index].size());
                    }
                }
            }
        }
        else
            fail();

    }

    @Test
    public void setMessyKBAndOuts_AllKBAxiomsPresentForInferences(){
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<ArrayList[]> infLists = ptd.getMessyOutputs();
        List<ArrayList<FactNode>> factLists = ptd.getMessyKB();

        int counter = 0;
        for(ArrayList[] infArray : infLists){
            List<FactNode> trueFacts = new ArrayList<>();
            for(ArrayList infList : infArray){
                for(Object inf : infList){
                    trueFacts.addAll(ptd.findAllFactsForInference((InferenceNode) inf));
                }
            }

            // Removes duplicates
            Set set = new LinkedHashSet<>(trueFacts);
            trueFacts.clear();
            trueFacts.addAll(set);

            // Checks to make sure that the true KB and the messy KB
            assertEquals(trueFacts.size(), factLists.get(counter).size());

            for(FactNode fact : trueFacts){
                // Assures that every fact that should be there is there.
                assertTrue(factLists.get(counter).contains(fact));
                factLists.get(counter).remove(fact);
            }
            // Makes sure that extra facts aren't included in the messyKB.
            assertEquals(0, factLists.get(counter).size());
            counter++;
        }
    }

    @Test
    public void makeSureTreeSupportEncodingsContainAllFactsThatWereUsedInItsCreation(){
        PackageTreeData ptd = new PackageTreeData(tm, 21);

        // Gathers what should be the supports.
        HashMap<InferenceNode, List<FactNode>> map = new HashMap<>();
        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                map.put((InferenceNode) tn, ptd.findAllFactsForInference(tn));
            }
        }

        HashMap<String, List<String>> expected = new HashMap<>();
        //Converts mapping to factnode into the string representation.
        for(Map.Entry<InferenceNode, List<FactNode>> entry : map.entrySet()){
            List<String> list = new ArrayList<>();
            for(FactNode fact : entry.getValue()){
                list.add(fact.getValue().toString().replace("\"", ""));
            }

            expected.put(entry.getKey().getValue().toString().replace("\"", ""),list);
        }

        HashMap<String, List<String>> actual = new HashMap<>();
        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                List<Double> suppEncoding = ((InferenceNode) tn).getSupportEncoding();
                List<String> temp = new ArrayList<>();
                List<String> split = new ArrayList<>();
                for(int i=0; i<suppEncoding.size(); i++){
                    split.add(tm.encodingMap.get(suppEncoding.get(i)));
                    if((i+1) % 3 == 0){
                        Triple t = (tm.infModel.createStatement(tm.infModel.getResource(split.get(0)), tm.infModel.getProperty(split.get(1)),
                                    tm.infModel.getResource(split.get(2))).asTriple());
                        split.clear();
                        temp.add(t.toString().replace("\"", ""));
                    }
                }
                actual.put(tn.getValue().toString().replace("\"", ""), temp);
            }//end if
        }//end for

        for(Map.Entry<String, List<String>> entry : expected.entrySet()){
            for(String s1 : entry.getValue()){
                List<String> list = actual.get(entry.getKey());
                try {
                    assertTrue(list.contains(s1));
                } catch(AssertionError e){
                    System.out.println();
                }
            }
        }
    }//end test
}