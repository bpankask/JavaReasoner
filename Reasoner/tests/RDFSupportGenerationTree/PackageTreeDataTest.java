package RDFSupportGenerationTree;

import RDFGraphManipulations.ScaledIntegerMappedEncoding;
import Reasoner.ReasonerLogic;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.RuleDerivation;
import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
        TreeManager tm = new TreeManager(inf);
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
    public void getKBEncoded_CanConvertBackToSameTriplesAsMessyData() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 14);
        Field f = PackageTreeData.class.getDeclaredField("messyKB");
        f.setAccessible(true);
        List messyKB = (List) f.get(ptd);

        List<ArrayList<Double>> kb = ptd.KB;

        List<ArrayList<Triple>> actual = new ArrayList<ArrayList<Triple>>();
        // Converts encoded kb into triples for comparison with actual kb triples in messy data.
        int sampleCount = -1;
        for(ArrayList<Double> encodedSample : kb){
            sampleCount++;
            ArrayList<Triple> temp = new ArrayList<>();
            List<String> split = new ArrayList<String>();

            for(int i=1; i<= encodedSample.size(); i++){
                double encodedIRI = encodedSample.get(i-1);
                int labelIRI = 0;
                //Converts encoding to label.
                if(encodedIRI < 0){
                    labelIRI = (int) (encodedIRI * ptd.encodingInfo[1]);
                    HashMap<Integer, String> map = reverseMap(ptd.labelToIRIMaps.get(sampleCount)[1]);
                    split.add(map.get(labelIRI));
                }
                else{
                    labelIRI = (int) (encodedIRI * ptd.encodingInfo[0]);
                    HashMap<Integer, String> map = reverseMap(ptd.labelToIRIMaps.get(sampleCount)[0]);
                    split.add(map.get(labelIRI));
                }

                if(i % 3 == 0){
                    try {
                        temp.add(tm.infModel.createStatement(tm.infModel.getResource(split.get(0)), tm.infModel.getProperty(split.get(1)),
                                tm.infModel.getResource(split.get(2))).asTriple());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    split.clear();
                }
            }
            actual.add(temp);
            temp.clear();
        }

        List<ArrayList<Triple>> expected = new ArrayList<ArrayList<Triple>>();
        for(Object obj : messyKB){
            ArrayList<FactNode> sample = (ArrayList) obj;
            ArrayList<Triple> temp = new ArrayList<>();
            for(FactNode fact : sample){
                temp.add(fact.getValue());
            }
            expected.add(temp);
            temp.clear();
        }

        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    private HashMap<Integer, String> reverseMap(HashMap<String, Integer> map){
        HashMap<Integer, String> rev = new HashMap<>();
        for(Map.Entry<String, Integer> entry : map.entrySet()){
            rev.put(entry.getValue(), entry.getKey());
        }
        return rev;
    }

    @Test
    public void getKBEncoded_SameLengthAsMessyData() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 9);
        Field f = PackageTreeData.class.getDeclaredField("messyKB");
        f.setAccessible(true);
        List mKB = (List) f.get(ptd);
        List<ArrayList<FactNode>> messyKB = mKB;
        List<ArrayList<Double>> kb = ptd.KB;

        // Makes sure no data is lost.
        int counter = 0;
        for(ArrayList<Double> factList : kb){
            int size = messyKB.get(counter).size();
            assertEquals(size, factList.size()/3);
            counter++;
        }
    }

    @Test
    public void getOutputsEncoded_CanConvertBackToSameTriplesAsMessyData() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 10);

        List<ArrayList[]> results = ptd.Outputs;

        List<ArrayList<Triple>> actual = new ArrayList<>();
        List<ArrayList<Triple>> expected = new ArrayList<>();

        Field f = PackageTreeData.class.getDeclaredField("messyOutputs");
        f.setAccessible(true);
        List<ArrayList[]> messyOuts = (List<ArrayList[]>) f.get(ptd);

        //Each sample split into timesteps
        for(int j=0; j<results.size(); j++){
            ArrayList<Triple> temp = new ArrayList<>();
            //Each timestep list
            for(ArrayList list : results.get(j)){
                //Each double in a timestep
                List<String> split = new ArrayList<>();
                for(int i=1; i<=list.size(); i++){
                    double encodedIRI = (double) list.get(i-1);
                    int labelIRI = 0;
                    //Converts encoding to label.
                    if(encodedIRI < 0){
                        labelIRI = (int) (encodedIRI * ptd.encodingInfo[1]);
                        HashMap<Integer, String> map = reverseMap(ptd.labelToIRIMaps.get(j)[1]);
                        split.add(map.get(labelIRI));
                    }
                    else{
                        labelIRI = (int) (encodedIRI * ptd.encodingInfo[0]);
                        HashMap<Integer, String> map = reverseMap(ptd.labelToIRIMaps.get(j)[0]);
                        split.add(map.get(labelIRI));
                    }

                    if(i % 3 == 0){
                        try {
                            temp.add(tm.infModel.createStatement(tm.infModel.getResource(split.get(0)), tm.infModel.getProperty(split.get(1)),
                                    tm.infModel.getResource(split.get(2))).asTriple());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
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
    public void getSupportsEncoded_CanConvertBackToSameTriplesAsMessyData() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 10);

        List<ArrayList[]> results = ptd.Supports;

        List<ArrayList<String>> actual = new ArrayList<>();
        List<ArrayList<String>> expected = new ArrayList<>();

        Field f = PackageTreeData.class.getDeclaredField("messySupports");
        f.setAccessible(true);
        List<ArrayList[]> messySupports = (List<ArrayList[]>) f.get(ptd);

        //Get Triples from encoding.
        //Each sample split into timesteps
        for(int j=0; j<results.size(); j++){
            ArrayList<String> temp = new ArrayList<>();
            //Each timestep list
            for(ArrayList list : results.get(j)){
                //Each double in a timestep
                List<String> split = new ArrayList<>();
                for(int i=1; i<=list.size(); i++){
                    double encodedIRI = (double) list.get(i-1);
                    int labelIRI = 0;
                    //Converts encoding to label.
                    if(encodedIRI < 0){
                        labelIRI =  (int) Math.round(encodedIRI * ptd.encodingInfo[1]);
                        HashMap<Integer, String> map = reverseMap(ptd.labelToIRIMaps.get(j)[1]);
                        split.add(map.get(labelIRI));
                    }
                    else{
                        labelIRI =  (int) Math.round(encodedIRI * ptd.encodingInfo[0]);
                        HashMap<Integer, String> map = reverseMap(ptd.labelToIRIMaps.get(j)[0]);
                        split.add(map.get(labelIRI));
                    }

                    if(i % 3 == 0){
                        temp.add(tm.infModel.createStatement(tm.infModel.getResource(split.get(0)), tm.infModel.getProperty(split.get(1)),
                                    tm.infModel.getResource(split.get(2))).asTriple().toString());
                        split.clear();
                    }
                }
            }
            actual.add(temp);
        }

        //Gets expected values
        for(ArrayList[] array : messySupports){
            ArrayList<String> temp1 = new ArrayList<>();
            for(ArrayList list : array){
                for(Object fact : list){
                    temp1.add(((FactNode) fact).getValue().toString());
                }
            }
            expected.add(temp1);
        }


        assertEquals(expected.size(), actual.size());
        for(int i=0; i<expected.size(); i++) {
            assertArrayEquals(expected.get(i).toArray(), actual.get(i).toArray());
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

    @Test
    public void allEncodedDataHasSameSampleSizes(){
        PackageTreeData ptd = new PackageTreeData(tm, 14);
        assertEquals(ptd.KB.size(), ptd.Outputs.size(), ptd.Supports.size());
    }

    //Messy Data Tests--------------------------------------------------------------------------------------------------

    @Test
    public void setMessyData_MessyDataSamplesAreSameSize() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);
        Field f = PackageTreeData.class.getDeclaredField("messyKB");
        f.setAccessible(true);
        List mKB = (List) f.get(ptd);

        Field f1 = PackageTreeData.class.getDeclaredField("messyOutputs");
        f1.setAccessible(true);
        List<ArrayList[]> messyOuts = (List<ArrayList[]>) f1.get(ptd);

        assertEquals(mKB.size(), messyOuts.size());
    }

    @Test
    public void setMessyData_MakeSureAllInferencesArePresentInMessyData() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        List<InferenceNode> expectedInferences = new ArrayList<>();
        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                if(!expectedInferences.contains(tn)){
                    expectedInferences.add((InferenceNode) tn);
                }
            }
        }

        Field f1 = PackageTreeData.class.getDeclaredField("messyOutputs");
        f1.setAccessible(true);
        List<ArrayList[]> messyOuts = (List<ArrayList[]>) f1.get(ptd);

        List<InferenceNode> actualInferences = new ArrayList<>();
        for(ArrayList[] array : messyOuts){
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
    public void setMessyData_EveryTimestepPresentForEachSample() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 15);

        Field f1 = PackageTreeData.class.getDeclaredField("messyOutputs");
        f1.setAccessible(true);
        List<ArrayList[]> messyOuts = (List<ArrayList[]>) f1.get(ptd);

        for(ArrayList[] outs : messyOuts){
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
    public void setMessyData_CorrectNumberOfOutsForEachSample() throws NoSuchFieldException, IllegalAccessException {
        // This test only works because I know that the data I am using to test has 7 timesteps.
        PackageTreeData ptd = new PackageTreeData(tm, 25);

        Field f1 = PackageTreeData.class.getDeclaredField("messyOutputs");
        f1.setAccessible(true);
        List<ArrayList[]> messyOuts = (List<ArrayList[]>) f1.get(ptd);

        List<ArrayList[]> outs = messyOuts;

        for(ArrayList[] sample : outs){
            int count = 0;
            for(ArrayList ts : sample){
                count += ts.size();
            }
            assertEquals(count, 25);
        }
    }

    @Test
    public void setMessyData_CorrectNumberOfTimeStepInferenceBasedOnNumberOfDesiredInferences() throws NoSuchFieldException, IllegalAccessException {
        int sizeOfOuts = 21;
        PackageTreeData ptd = new PackageTreeData(tm, sizeOfOuts);
        double numberOfTimesteps = tm.tree.get(0).getTimeStep();

        Field f1 = PackageTreeData.class.getDeclaredField("messyOutputs");
        f1.setAccessible(true);
        List<ArrayList[]> outs = (List<ArrayList[]>) f1.get(ptd);


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
    public void setMessyData_AllKBAxiomsPresentForInferences() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        Method m = PackageTreeData.class.getDeclaredMethod("findAllFactsForInference", TreeNode.class);
        m.setAccessible(true);

        Field f = PackageTreeData.class.getDeclaredField("messyKB");
        f.setAccessible(true);
        List<ArrayList<FactNode>> factLists = (List<ArrayList<FactNode>>) f.get(ptd);

        Field f1 = PackageTreeData.class.getDeclaredField("messyOutputs");
        f1.setAccessible(true);
        List<ArrayList[]> infLists = (List<ArrayList[]>) f1.get(ptd);

        int counter = 0;
        for(ArrayList[] infArray : infLists){
            List<FactNode> trueFacts = new ArrayList<>();
            for(ArrayList infList : infArray){
                for(Object inf : infList){
                    trueFacts.addAll((ArrayList<FactNode>) m.invoke(ptd, (InferenceNode) inf));
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
    public void setMessyData_SupportsOnlyHaveFactNodesContainedInCorrospondingKBSample() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 20);

        Field f = PackageTreeData.class.getDeclaredField("messyKB");
        f.setAccessible(true);
        List<ArrayList<FactNode>> kb = (List<ArrayList<FactNode>>) f.get(ptd);

        Field f1 = PackageTreeData.class.getDeclaredField("messySupports");
        f1.setAccessible(true);
        List<ArrayList[]> supp = (List<ArrayList[]>) f1.get(ptd);

        for(int sampleIndex=0; sampleIndex < supp.size(); sampleIndex++){
            ArrayList<FactNode> kbSample = kb.get(sampleIndex);
            ArrayList[] suppSample = supp.get(sampleIndex);

            for(ArrayList ts : suppSample){
                for(Object obj : ts){
                    assertTrue(kbSample.contains(obj));
                }
            }
        }
    }

    @Test
    public void setMessyData_SuppSamplesHaveNoDuplicatesWithinTimsteps() throws NoSuchFieldException, IllegalAccessException {
        PackageTreeData ptd = new PackageTreeData(tm, 14);

        Field f1 = PackageTreeData.class.getDeclaredField("messySupports");
        f1.setAccessible(true);
        List<ArrayList[]> supp = (List<ArrayList[]>) f1.get(ptd);

        for(int sampleIndex=0; sampleIndex < supp.size(); sampleIndex++){
            ArrayList[] suppSample = supp.get(sampleIndex);

            for(ArrayList ts : suppSample){
                for(int i=-1; i<ts.size()-1; i++){
                    assertTrue(ts.indexOf(ts.get(i+1)) > i);
                }
            }
        }
    }

    //Other Methods ----------------------------------------------------------------------------------------------------

    @Test
    public void getIRIEncodingMaps_SyntheticDataWithKnownValues() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        PackageTreeData ptd = new PackageTreeData(tm, 7);

        Model model = ModelFactory.createDefaultModel();
        FactNode t1 = new FactNode(model.createStatement(model.createResource("s1"), model.createProperty("p1"),
                model.createResource("l1")).asTriple());
        FactNode t2 = new FactNode(model.createStatement(model.createResource("s2"), model.createProperty("s1"),
                model.createResource("l2")).asTriple());
        FactNode t3 = new FactNode(model.createStatement(model.createResource("s3"), model.createProperty("p3"),
                model.createResource("l3")).asTriple());
        FactNode t4 = new FactNode(model.createStatement(model.createResource("s4"), model.createProperty("p4"),
                model.createResource("l4")).asTriple());
        FactNode t5 = new FactNode(model.createStatement(model.createResource("s5"), model.createProperty("p5"),
                model.createResource("l5")).asTriple());

        List synKB = new ArrayList();
        synKB.add(t1);
        synKB.add(t2);
        synKB.add(t3);

        ArrayList[] synOuts = new ArrayList[]{new ArrayList(), new ArrayList()};
        synOuts[0].add(t4);
        synOuts[1].add(t5);

        Method method = PackageTreeData.class.getDeclaredMethod("getIRIEncodingMaps", List.class, ArrayList[].class);
        method.setAccessible(true);


        HashMap<String, Integer>[] maps = (HashMap<String, Integer>[]) method.invoke(ptd, synKB, synOuts);

        assertEquals(maps[0].size(), 10);
        assertEquals(maps[1].size(), 5);

        Collection<Integer> val = maps[0].values();
        for(int i=1; i<=10; i++){
            assertTrue(val.contains(i));
        }

        Collection<Integer> val1 = maps[1].values();
        for(int i=-1; i>=-5; i--){
            assertTrue(val1.contains(i));
        }


        List<String> expectedOuter = Arrays.asList("s1", "s2", "s3", "s4", "s5", "l1", "l2", "l3", "l4", "l5");
        List<String> expectedInner = Arrays.asList("s1", "p1", "p3", "p4", "p5");

        Set<String> val2 = maps[0].keySet();
        for(String s : expectedOuter){
            val2.contains(s);
        }

        Set<String> val12 = maps[1].keySet();
        for(String s : expectedInner){
            assertTrue(val12.contains(s));
        }
    }

    @Test
    public void createSamplesFromKBTimesteps_CorrectSampleCreationAndRemovalOfDuplicates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = PackageTreeData.class.getDeclaredMethod("createSamplesFromKBTimesteps", HashMap.class);
        method.setAccessible(true);
        PackageTreeData ptd = new PackageTreeData(tm, 7);

        HashMap<Integer, List> map = new HashMap<>();
        map.put(1, Arrays.asList(Arrays.asList("1", "22"), Arrays.asList("8", "10")));
        map.put(2, Arrays.asList(Arrays.asList("2", "22"), Arrays.asList("9", "7")));
        map.put(3, Arrays.asList(Arrays.asList("3", "33"), Arrays.asList("10", "9")));

        List l = (List) method.invoke(ptd, map);

        List trueList0 = Arrays.asList("1", "22", "2", "3", "33");
        List trueList1 = Arrays.asList("7", "8", "9", "10");

        assertEquals(trueList0.size(), ((List) l.get(0)).size());
        assertEquals(trueList1.size(), ((List) l.get(1)).size());

        for(Object obj : trueList0){
            assertTrue(((List) l.get(0)).contains(obj));
        }

        for(Object obj : trueList1){
            assertTrue(((List) l.get(1)).contains(obj));
        }
    }

    @Test
    public void createSamplesFromOutTimesteps_CorrectSampleCreationAndRemovalOfDuplicates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = PackageTreeData.class.getDeclaredMethod("createSamplesFromOutTimesteps", HashMap.class);
        method.setAccessible(true);
        PackageTreeData ptd = new PackageTreeData(tm, 7);

        HashMap<Integer, List> map = new HashMap<>();
        map.put(1, Arrays.asList(Arrays.asList("1", "11"), Arrays.asList("111", "1111")));
        map.put(2, Arrays.asList(Arrays.asList("2", "22"), Arrays.asList("222", "2222")));
        map.put(3, Arrays.asList(Arrays.asList("3", "33"), Arrays.asList("333", "3333")));

        List<ArrayList[]> l = (List<ArrayList[]>) method.invoke(ptd, map);

        ArrayList[] trueSample0 = new ArrayList[3];
        trueSample0[0] = new ArrayList(Arrays.asList("1", "11"));
        trueSample0[1] = new ArrayList(Arrays.asList("2", "22"));
        trueSample0[2] = new ArrayList(Arrays.asList("3", "33"));

        ArrayList[] trueSample1 = new ArrayList[3];
        trueSample1[0] = new ArrayList(Arrays.asList("111", "1111"));
        trueSample1[1] = new ArrayList(Arrays.asList("222", "2222"));
        trueSample1[2] = new ArrayList(Arrays.asList("333", "3333"));

        List<ArrayList[]> trueSamples = Arrays.asList(trueSample0, trueSample1);

        assertArrayEquals(trueSamples.toArray(), l.toArray());

    }

    @Test
    public void createSamplesFromSuppTimesteps_CorrectSampleCreationAndRemovalOfDuplicates() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = PackageTreeData.class.getDeclaredMethod("createSamplesFromSuppTimesteps", HashMap.class);
        method.setAccessible(true);
        PackageTreeData ptd = new PackageTreeData(tm, 7);

        HashMap<Integer, List> map = new HashMap<>();
        map.put(1, Arrays.asList(Arrays.asList("1", "11", "11"), Arrays.asList("111", "1111")));
        map.put(2, Arrays.asList(Arrays.asList("2", "22"), Arrays.asList("222", "2222")));
        map.put(3, Arrays.asList(Arrays.asList("3", "33"), Arrays.asList("333", "3333", "333")));

        List<ArrayList[]> l = (List<ArrayList[]>) method.invoke(ptd, map);

        ArrayList[] trueSample0 = new ArrayList[3];
        trueSample0[0] = new ArrayList(Arrays.asList("1", "11"));
        trueSample0[1] = new ArrayList(Arrays.asList("2", "22"));
        trueSample0[2] = new ArrayList(Arrays.asList("3", "33"));

        ArrayList[] trueSample1 = new ArrayList[3];
        trueSample1[0] = new ArrayList(Arrays.asList("111", "1111"));
        trueSample1[1] = new ArrayList(Arrays.asList("222", "2222"));
        trueSample1[2] = new ArrayList(Arrays.asList("333", "3333"));

        List<ArrayList[]> trueSamples = Arrays.asList(trueSample0, trueSample1);

        assertArrayEquals(trueSamples.toArray(), l.toArray());

    }
}