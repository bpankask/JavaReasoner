package RDFSupportGenerationTree;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Collects and organizes tree data for serialization and later training of a deep model.
 */
public class PackageTreeData {

    private TreeManager tm;
    private List<ArrayList<FactNode>> messyKB = null;
    private List<ArrayList[]> messyOutputs = null;

    public PackageTreeData(TreeManager tm, int sizeOfOuts){
        this.tm = tm;
        this.tm.tree.sort(new SortByTimeStep());

        setMessyKBAndOuts(sizeOfOuts);
    }

    /**
     * Gets final kb which is encoded and ready to be passed to an RNN type model.
     * @return Final KB data.
     */
    public List<ArrayList<Double>> getKBEncoded(){
        List<ArrayList<Double>> kb = new ArrayList<ArrayList<Double>>();

        for(ArrayList<FactNode> factsSample : this.messyKB){
            ArrayList<Double> tempSample = new ArrayList<Double>();
            for(FactNode fact : factsSample){
                tempSample.addAll(fact.getEncoding());
            }
            kb.add(tempSample);
        }
        return kb;
    }

    /**
     * Gets final outputs which are encoded and ready to be passed to an RNN type model.
     * @return Polished data.
     */
    public List<ArrayList[]> getOutputsEncoded(){
        return encodeMessyOutputs(this.messyOutputs);
    }

    public List<ArrayList[]> getSupportsEncoded(){
        return encodeMessySupports(this.messyOutputs);
    }

    /**
     * Gets a mapping from a double to its corresponding string which is a subject, predicate, or object of a triple.
     * @return Mapping from encoding to IRI.
     */
    public HashMap<Double, String> getVectorMap(){
         return tm.encodingMap;
    }

    public List<ArrayList<FactNode>> getMessyKB() {
        return messyKB;
    }

    public List<ArrayList[]> getMessyOutputs() {
        return messyOutputs;
    }

    /**
     * Converts list of inferences seperated by timestep into the correct encoding.
     * @param messyD List of messy InferenceNode.
     * @return Encoded messy data.
     */
    private List<ArrayList[]> encodeMessyOutputs(List<ArrayList[]> messyD){
        List<ArrayList[]> returnable = new ArrayList<>();

        for(ArrayList[] infList : messyD){

            //Sets up array for time steps.
            ArrayList[] encodedTimeStepArray = new ArrayList[infList.length];
            for(int i=0; i<encodedTimeStepArray.length; i++){
                encodedTimeStepArray[i] = new ArrayList();
            }

            //List for each timestep of infList
            int counter = 0;
            for(ArrayList timeSteps : infList){
                ArrayList<Double> temp = new ArrayList<>();
                //Encoding each inf
                for(Object obj : timeSteps){
                    temp.addAll( ((InferenceNode) obj).getEncoding() );
                }
                encodedTimeStepArray[counter].addAll(temp);
                counter++;
            }
            returnable.add(encodedTimeStepArray);
        }
        return returnable;
    }

    private List<ArrayList[]> encodeMessySupports(List<ArrayList[]> messyD){
        List<ArrayList[]> returnable = new ArrayList<>();

        for(ArrayList[] sample : messyD){

            //Sets up array for time steps.
            ArrayList[] encodedTimeStepArray = new ArrayList[sample.length];
            for(int i=0; i<encodedTimeStepArray.length; i++){
                encodedTimeStepArray[i] = new ArrayList();
            }

            //List for each timestep of sample
            int counter = 0;
            for(ArrayList timeSteps : sample){
                ArrayList<Double> temp = new ArrayList<>();
                //Encoding each inf
                for(Object obj : timeSteps){
                    temp.addAll( ((InferenceNode) obj).getSupportEncoding());
                }
                encodedTimeStepArray[counter].addAll(temp);
                counter++;
            }
            returnable.add(encodedTimeStepArray);
        }
        return returnable;
    }

    private void setMessyKBAndOuts(int numOfOutputTriples){
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

                System.out.println("PackageData: Sample: " + k);
            }
            mapFromTimeStepToSamples.put(i, allSamplesForOuputAtTimeStep);
            mapFromTimeStepToKB.put(i, allSamplesForKBAtTimeStep);
        }
        this.messyOutputs = createSamplesFromTimesteps(mapFromTimeStepToSamples);
        this.messyKB = createSamplesFromKBTimesteps(mapFromTimeStepToKB);
    }
    
    private List<ArrayList[]> createSamplesFromTimesteps(HashMap<Integer, List> listToMerge){
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

    private List createSamplesFromKBTimesteps(HashMap<Integer, List> listToMerge){
        List allSamples = new ArrayList<>();

        for(int i=0; i<listToMerge.get(1).size(); i++){
            ArrayList sample = new ArrayList<>();
            for(Map.Entry<Integer, List> entry : listToMerge.entrySet()){
                sample.addAll((List) entry.getValue().get(i));
            }
            // Removes duplicates.
            Set set = new LinkedHashSet<>(sample);
            sample.clear();
            sample.addAll(set);

            allSamples.add(sample);
        }

        return allSamples;
    }

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
    public List<FactNode> findAllFactsForInference(TreeNode tn){
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
}