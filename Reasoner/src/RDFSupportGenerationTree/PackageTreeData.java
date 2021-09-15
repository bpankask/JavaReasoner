package RDFSupportGenerationTree;

import org.apache.jena.graph.Triple;

import java.util.*;

/**
 * Collects and organizes tree data for serialization and later training of a deep model.
 */
public class PackageTreeData {

    private TreeManager tm;
    private List<ArrayList<FactNode>> messyKB = null;
    private List<ArrayList[]> messyOutputs = null;
    private List<ArrayList[]> messySupports = null;

    public List<ArrayList<Double>> KB = null;
    public List<ArrayList[]> Outputs = null;
    public List<ArrayList[]> Supports = null;
    public List<HashMap<String, Integer>[]> labelToIRIMaps = null;
    public double[] encodingInfo = null;

    public PackageTreeData(TreeManager tm, int sizeOfOuts){
        this.tm = tm;
        this.tm.tree.sort(new SortByTimeStep());
        setMessyData(sizeOfOuts);
        this.encodingInfo = getEncodingInfo();
        encodeAllData(messyKB, messyOutputs, messySupports);
    }

    /**
     * Sets messy data fields by separating tree nodes into appropriate lists.
     * @param numOfOutputTriples
     */
    private void setMessyData(int numOfOutputTriples){
        HashMap<Integer, ArrayList<InferenceNode>> tsToInf = tm.seperateByTimestep();

        HashMap<Integer, Integer> quotaMap = calculateQuotaForEachTS(tsToInf, numOfOutputTriples);

        // Calculates how many iterations of complete sample data will be created.
        int longestTimestepIndex = 3;
        for(int x=1; x<=tsToInf.size(); x++){
            if(tsToInf.get(longestTimestepIndex).size() < tsToInf.get(x).size()){
                longestTimestepIndex = x;
            }
        }
        int numSamples = (tsToInf.get(longestTimestepIndex).size() / quotaMap.get(longestTimestepIndex)) + 1;

        // Maps time step with lists that represent all the samples for that timestep.  Later to be combined to form
        // complete sample data.
        HashMap<Integer, List> mapFromTimeStepToSamples = new HashMap<>();
        HashMap<Integer, List> mapFromTimeStepToKB = new HashMap<>();

        // Loop for each timestep.
        for(int i=1; i<=quotaMap.size(); i++){
            List<ArrayList<InferenceNode>> allSamplesForOuputAtTimeStep = new ArrayList<>();
            List<ArrayList<FactNode>> allSamplesForKBAtTimeStep = new ArrayList<>();

            int index = 0;

            // Loop for number of samples
            for(int k=0; k<numSamples; k++) {
                ArrayList<InferenceNode> triplesForOutsAtTimeStep = new ArrayList<>();
                ArrayList<FactNode> triplesForKBAtTimeStep = new ArrayList<>();

                for (int j = 1; j <= quotaMap.get(i); j++) {
                    if(index == tsToInf.get(i).size()){
                        index = 0;
                    }
                    triplesForOutsAtTimeStep.add(tsToInf.get(i).get(index));
                    triplesForKBAtTimeStep.addAll(findAllFactsForInference(tsToInf.get(i).get(index)));
                    index++;
                }

                allSamplesForOuputAtTimeStep.add(triplesForOutsAtTimeStep);
                allSamplesForKBAtTimeStep.add(triplesForKBAtTimeStep);
            }
            mapFromTimeStepToSamples.put(i, allSamplesForOuputAtTimeStep);
            mapFromTimeStepToKB.put(i, allSamplesForKBAtTimeStep);
        }
        this.messyOutputs = createSamplesFromOutTimesteps(mapFromTimeStepToSamples);
        this.messySupports = createSamplesFromSuppTimesteps(mapFromTimeStepToKB);
        //This will remove duplicates and squish kb into one big array vs separating by timestep.
        this.messyKB = createSamplesFromKBTimesteps(mapFromTimeStepToKB);
    }

    /**
     * Finds the most IRIs used in any sample for the predicates place as well as the sub. and obj. of a triple.  This is used to scale an
     * encoding into numbers between 0 and 1 or -1 and 0.
     * @return Array starting with max IRIs used in sub. and obj. followed by predicate IRIs.
     */
    public double[] getEncodingInfo(){
        int maxOuterIRI = 0;
        int maxInnerIRI = 0;

        List<ArrayList<FactNode>> kb = this.messyKB;
        for(ArrayList<FactNode> sample : kb){
            List<String> uniqueOuter = new ArrayList<>();
            List<String> uniqueInner = new ArrayList<>();

            for(FactNode factN : sample){
                Triple fact = factN.getValue();
                if(!uniqueOuter.contains(fact.getSubject().toString())){
                    uniqueOuter.add(fact.getSubject().toString());
                }
                if(!uniqueOuter.contains(fact.getObject().toString())){
                    uniqueOuter.add(fact.getObject().toString());
                }
                if(!uniqueInner.contains(fact.getPredicate().toString())){
                    uniqueInner.add(fact.getPredicate().toString());
                }
            }

            if(maxOuterIRI < uniqueOuter.size()){
                maxOuterIRI = uniqueOuter.size();
            }

            if(maxInnerIRI < uniqueInner.size()){
                maxInnerIRI = uniqueInner.size();
            }
        }
        return new double[] {maxOuterIRI, maxInnerIRI};
    }

    /**
     * Gets final kb which is encoded and ready to be passed to an RNN type model.
     * @return Final KB data.
     */
    private ArrayList<Double> encodedMessyKB(HashMap<String, Integer>[] lableMap, ArrayList<FactNode> sample){
        ArrayList<Double> tempSample = new ArrayList<Double>();
        for (FactNode fn : sample) {
            double s = lableMap[0].get(fn.getValue().getSubject().toString()) / encodingInfo[0];
            double p = lableMap[1].get(fn.getValue().getPredicate().toString()) / encodingInfo[1];
            double o = lableMap[0].get(fn.getValue().getObject().toString()) / encodingInfo[0];
            tempSample.add(s);
            tempSample.add(p);
            tempSample.add(o);
        }
        return tempSample;
    }

    /**
     * Uses a label map and scaling info to convert one sample of supp or output TreeNode data into a real number value.
     * @param labelMap Maps from an integer label to corresponding string IRI.
     * @param sample Single sample to be encoded.
     * @return
     */
    private ArrayList[] encodeMessyOutsOrSupp(HashMap<String, Integer>[] labelMap, ArrayList[] sample){
        //Sets up array for time steps.
        ArrayList[] encodedTimeStepArray = new ArrayList[sample.length];
        for (int i = 0; i < encodedTimeStepArray.length; i++) {
            encodedTimeStepArray[i] = new ArrayList();
        }

        //List for each timestep of sample
        int counter = 0;
        for (ArrayList timeStep : sample) {
            ArrayList<Double> temp = new ArrayList<>();
            //Encoding each inf
            for (Object obj : timeStep) {
                double s = labelMap[0].get(((TreeNode) obj).getValue().getSubject().toString()) / encodingInfo[0];
                double p = labelMap[1].get(((TreeNode) obj).getValue().getPredicate().toString()) / encodingInfo[1];
                double o = labelMap[0].get(((TreeNode) obj).getValue().getObject().toString()) / encodingInfo[0];

                temp.add(s);
                temp.add(p);
                temp.add(o);
            }
            encodedTimeStepArray[counter].addAll(temp);
            counter++;
        }
        return encodedTimeStepArray;
    }

    /**
     * Takes all the messy data in TreeNode form and converts to appropriate scaled encoding.
     * @param mKB
     * @param mOuts
     * @param mSupp
     */
    private void encodeAllData(List<ArrayList<FactNode>> mKB, List<ArrayList[]> mOuts, List<ArrayList[]> mSupp){
        List<HashMap<String, Integer>[]> mapList = new ArrayList<>();

        List<ArrayList<Double>> encodedKb = new ArrayList<>();
        List<ArrayList[]> encodedOuts = new ArrayList<>();
        List<ArrayList[]> encodedSupp = new ArrayList<>();

        // Loops through each sample.
        for(int i=0; i<mKB.size(); i++){
            HashMap<String, Integer>[] labelMap = getIRIEncodingMaps(mKB.get(i), mOuts.get(i));
            encodedKb.add(encodedMessyKB(labelMap, mKB.get(i)));
            encodedOuts.add(encodeMessyOutsOrSupp(labelMap, mOuts.get(i)));
            encodedSupp.add(encodeMessyOutsOrSupp(labelMap, mSupp.get(i)));
            mapList.add(labelMap);
        }

        this.KB = encodedKb;
        this.Outputs = encodedOuts;
        this.Supports = encodedSupp;
        this.labelToIRIMaps = mapList;
    }

    /**
     * Selects one row from each timestep list and merges them into one sample.
     * @param listToMerge
     * @return
     */
    private List<ArrayList[]> createSamplesFromOutTimesteps(HashMap<Integer, List> listToMerge){
        List allSamples = new ArrayList<>();

        for(int i=0; i<listToMerge.get(1).size(); i++){
            ArrayList<ArrayList> sample = new ArrayList<>();
            for(Map.Entry<Integer, List> entry : listToMerge.entrySet()) {
                ArrayList tsI = new ArrayList<>((List) entry.getValue().get(i));
                sample.add(tsI);
            }
            allSamples.add(sample.toArray(ArrayList[]::new));
        }

        return allSamples;
    }

    /**
     * Selects one row from each timestep list and merges them into one sample.
     * @param listToMerge
     * @return
     */
    private List<ArrayList[]> createSamplesFromSuppTimesteps(HashMap<Integer, List> listToMerge){
        List allSamples = new ArrayList<>();

        for(int i=0; i<listToMerge.get(1).size(); i++){
            ArrayList<ArrayList> sample = new ArrayList<>();
            for(Map.Entry<Integer, List> entry : listToMerge.entrySet()) {
                ArrayList tsI = new ArrayList<>((List) entry.getValue().get(i));
                //Removes duplicates from a timestep internally but duplicates in different time steps will remain.
                Set set = new LinkedHashSet<>(tsI);
                tsI.clear();
                tsI.addAll(set);
                sample.add(tsI);
            }
            allSamples.add(sample.toArray(ArrayList[]::new));
        }

        return allSamples;
    }

    /**
     * Selects one row from each timestep list and merges them into one sample.
     * @param listToMerge
     * @return
     */
    private List createSamplesFromKBTimesteps(HashMap<Integer, List> listToMerge){
        List allSamples = new ArrayList<>();

        for(int i=0; i<listToMerge.get(1).size(); i++){
            ArrayList sample = new ArrayList<>();
            for(Map.Entry<Integer, List> entry : listToMerge.entrySet()){
                sample.addAll((List) entry.getValue().get(i));
            }
            // Removes duplicates from entire sample.
            Set set = new LinkedHashSet<>(sample);
            sample.clear();
            sample.addAll(set);

            allSamples.add(sample);
        }

        return allSamples;
    }

    /**
     * Determines how many Inference nodes from each time step category to include in messy output.
     * @param tsToInf
     * @param numOfOutputTriples
     * @return
     */
    private HashMap<Integer, Integer> calculateQuotaForEachTS(HashMap<Integer, ArrayList<InferenceNode>> tsToInf, int numOfOutputTriples){
        HashMap<Integer, Integer> tsToQuota = new HashMap<>();
        // Calculate how many of each ts inference to include
        int mainQuota = numOfOutputTriples / tsToInf.size();
        int partialQuota = numOfOutputTriples - mainQuota * tsToInf.size();

        for(int i=1; i <= tsToInf.size(); i++){
            if(i <= partialQuota){
                tsToQuota.put(i, mainQuota + 1);
            }
            else{
                tsToQuota.put(i, mainQuota);
            }
        }

        return tsToQuota;
    }

    /**
     * Recursive method to find all facts from a KB which were used to create an inference.
     * @param tn
     * @return
     */
    private List<FactNode> findAllFactsForInference(TreeNode tn){
        List<FactNode> supp1 = new ArrayList<>();
        List<FactNode> supp2 = new ArrayList<>();

        if(tn instanceof FactNode){
            supp1.add((FactNode) tn);
            return supp1;
        }
        else if(tn instanceof InferenceNode){
            supp1.addAll(findAllFactsForInference(((InferenceNode) tn).support1));
            if(((InferenceNode) tn).support2 != null){
                supp2.addAll(findAllFactsForInference(((InferenceNode) tn).support2));
            }
        }

        supp1.addAll(supp2);
        return supp1;
    }

    /**
     * Provides a map from every unique IRI in both parameters and maps them to an integer label.
     * @param kbSample
     * @param outSample
     * @return Array where first value is map for outer parts of a triple, and second value is for IRIs used as predicate.
     */
    private HashMap<String, Integer>[] getIRIEncodingMaps(List kbSample, ArrayList[] outSample){
        HashMap<String, Integer> outerMap = new HashMap<>();
        HashMap<String, Integer> innerMap = new HashMap<>();
        int innerCount = 0;
        int outerCount = 0;
        for(ArrayList ts : outSample){
            for(Object tn : ts){
                Triple t = ((TreeNode) tn).getValue();
                if(!outerMap.containsKey(t.getSubject().toString())){
                    outerMap.put(t.getSubject().toString(), outerCount += 1);
                }
                if(!outerMap.containsKey(t.getObject().toString())){
                    outerMap.put(t.getObject().toString(), outerCount += 1);
                }
                if(!innerMap.containsKey(t.getPredicate().toString())){
                    innerMap.put(t.getPredicate().toString(), innerCount -= 1);
                }
            }
        }

        for(Object tn : kbSample){
            Triple t = ((TreeNode) tn).getValue();
            if(!outerMap.containsKey(t.getSubject().toString())){
                outerMap.put(t.getSubject().toString(), outerCount += 1);
            }
            if(!outerMap.containsKey(t.getObject().toString())){
                outerMap.put(t.getObject().toString(), outerCount += 1);
            }
            if(!innerMap.containsKey(t.getPredicate().toString())){
                innerMap.put(t.getPredicate().toString(), innerCount -= 1);
            }
        }

        return new HashMap[] {outerMap, innerMap};
    }
}