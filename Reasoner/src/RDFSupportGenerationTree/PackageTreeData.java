package RDFSupportGenerationTree;

import java.util.*;

/**
 * Collects and organizes tree data for serialization and later training of a deep model.
 */
public class PackageTreeData {

    private TreeManager tm;
    private List<ArrayList<FactNode>> messyKB = null;
    private List<ArrayList<InferenceNode>> messyOutputs = null;
    private HashMap<InferenceNode, List<FactNode>> messyInfToFactsMap;
    private int timeStepIndexChecker;

    public PackageTreeData(TreeManager tm, int sizeOfKBs){
        this.tm = tm;
        this.tm.tree.sort(new SortByTimeStep());
        timeStepIndexChecker = this.tm.tree.get(0).getTimeStep()+1;
        messyInfToFactsMap = assignFactsToInferenceNodes();
        setMessyKBAndOuts(sizeOfKBs);
        //vectorMap = getVectorMap(1);
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

//    public List<Double[][]> getOutputsEncoded(){
//        List<Double[][]> outs = new ArrayList<Double[][]>();
//
//        for(ArrayList<InferenceNode> outSample : this.messyOutputs){
//            List<InferenceNode> os = (List<InferenceNode>) outSample;
//
//        }
//    }

    /**
     * Gets a mapping from a double to its corresponding string which is a subject, predicate, or object of a triple.
     * @param listLength
     * @return
     */
    public List<HashMap<Double, String>> getVectorMap(int listLength){
        List<HashMap<Double, String>> listMap = new ArrayList<>();
        for(int i=0; i<listLength; i++){
            listMap.add((HashMap) tm.encodingMap);
        }
        return listMap;
    }

    public List<ArrayList<FactNode>> getMessyKB() {
        return messyKB;
    }

    public List<ArrayList<InferenceNode>> getMessyOutputs() {
        return messyOutputs;
    }

    public HashMap<InferenceNode, List<FactNode>> getMessyInfToFactsMap(){
        return messyInfToFactsMap;
    }

    /**
     * Sets the PackageTreeData messy data fields which can be converted to encoded polished data.
     * @param sizeOfKBs
     */
    private void setMessyKBAndOuts(int sizeOfKBs){
        List<ArrayList<FactNode>> kb = new ArrayList<ArrayList<FactNode>>();
        List<ArrayList<InferenceNode>> outputs = new ArrayList<ArrayList<InferenceNode>>();

        List inferences = new ArrayList(Arrays.asList(messyInfToFactsMap.keySet().toArray()));
        Iterator iter = messyInfToFactsMap.values().iterator();
        List<List<FactNode>> facts = new ArrayList<>();
        iter.forEachRemaining(fact -> facts.add((List<FactNode>) fact));

        ArrayList<FactNode> tempKB = new ArrayList<FactNode>();
        ArrayList<InferenceNode> tempOuts = new ArrayList<InferenceNode>();

        List<Boolean> timeStepAdded = new ArrayList<Boolean>();
        //Makes sure that first node represents highest timestep.
        for(int i=0; i<timeStepIndexChecker-1; i++){
            timeStepAdded.add(false);
        }

        int kbSizeTest = kb.size();
        int counter = 0;

        //Loop until all inferences are included.
        while(inferences.size() > 0 || tempKB.size() != 0){
            //Case where more KB axioms can be added.
            if(tempKB.size() < sizeOfKBs){
                boolean found = false;
                for(int i=0; i<facts.size(); i++){
                    int factSize = facts.get(i).size();
                    if(tempKB.size() + factSize <= sizeOfKBs && ((InferenceNode)inferences.get(i)).getTimeStep() < timeStepIndexChecker){
                        if(shouldAddFacts(inferences, facts, timeStepAdded, i, sizeOfKBs-tempKB.size(), false)){
                            tempKB.addAll(facts.get(i));
                            tempOuts.add((InferenceNode) inferences.get(i));
                            tempKB = removeDuplicates(tempKB);
                            tempOuts = removeDuplicates(tempOuts);
                            facts.remove(i);
                            inferences.remove(i);
                            found = true;
                            break;
                        }
                    }
                }//end for
                //Special case where there is no list of facts small enough to put into tempKB.  Must look through entire
                //list because one may have existed but was deleted in previous operation.
                if(!found){
                    List tempInf = new ArrayList(Arrays.asList(messyInfToFactsMap.keySet().toArray()));
                    Iterator tempIter = messyInfToFactsMap.values().iterator();
                    List<List<FactNode>> tempFacts = new ArrayList<>();
                    tempIter.forEachRemaining(fact -> tempFacts.add((List<FactNode>) fact));
                    for(int i=0; i<tempFacts.size(); i++){
                        if(tempKB.size() + tempFacts.get(i).size() <= sizeOfKBs && ((InferenceNode)tempInf.get(i)).getTimeStep() < timeStepIndexChecker){
                            if(shouldAddFacts(tempInf, tempFacts, timeStepAdded, i, sizeOfKBs-tempKB.size(), true)){
                                tempKB.addAll(tempFacts.get(i));
                                tempOuts.add((InferenceNode) tempInf.get(i));
                                int temp = tempKB.size();
                                removeDuplicates(tempKB);
                                removeDuplicates(tempOuts);
                                if(temp == tempKB.size()){
                                    break;
                                }
                            }
                        }
                    }//end for
                }
            }//end if
            //Case if tempKB is just the right size to be added.
            if(tempKB.size() == sizeOfKBs){
                kb.add(tempKB);
                outputs.add(tempOuts);
                tempKB = new ArrayList<FactNode>();
                tempOuts = new ArrayList<InferenceNode>();
                resetTimeStepCounter(timeStepAdded);
                timeStepIndexChecker = tm.tree.get(0).getTimeStep()+1;
            }
            if(kb.size() == kbSizeTest)
                counter++;
            else{
                counter = 0;
            }

            if(counter > 20)
                System.out.println("NOOO");
            kbSizeTest = kb.size();

        }//end main loop

        messyKB = kb;
        messyOutputs = outputs;
    }

    /**
     * Helper to set all values of a list to false indicating that those timesteps are still needed for sample it represents.
     * @param timeStepAdded
     */
    private void resetTimeStepCounter(List<Boolean> timeStepAdded){
        for(int i=0; i<timeStepAdded.size(); i++){
            timeStepAdded.set(i, false);
        }
    }

    /**
     *
     * @param list
     * @param <T>
     * @return
     */
    //From: https://www.geeksforgeeks.org/how-to-remove-duplicates-from-arraylist-in-java/#:~:text=Hence%20LinkedHashSet%20is%20the%20best%20option%20available%20as,insertion%20order.%20Get%20the%20ArrayList%20with%20duplicate%20values.
    private <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }

    /**
     * Logic to determine if kb facts should be added based on the corresponding inferences.  Seeks to ensure that each
     * sample contains a sequence of timesteps without gaps and as close to the max timestep as is available.
     * @param inferences Available inferences to look through.
     * @param timeStepAdded Records which timesteps are still needed.
     * @param index The current index in the inferences.
     * @return Whether or not the kb facts should be added.
     */
    private boolean shouldAddFacts(List inferences, List<List<FactNode>> facts, List<Boolean> timeStepAdded, int index, int size, boolean fullList){
        //True if all necessary value are filled so any value will work.
        if(!timeStepAdded.contains(false)){
            return true;
        }
        //True if this inference will work for this sample.
        else if(((InferenceNode) inferences.get(index)).getTimeStep() == (timeStepAdded.indexOf(false)+1)){
            timeStepAdded.set(timeStepAdded.indexOf(false), true);
            return true;
        }
        //True if there is a good value in the list farther down the line so don't add axiom now.
        else if(listContainsObjectWithDesiredTimeStep(inferences, facts,timeStepAdded.indexOf(false)+1, size)){
            return false;
        }
        //If it gets to this it.
        else{
            if(fullList){
                timeStepIndexChecker = timeStepAdded.indexOf(false);
                for(int i=timeStepIndexChecker; i<timeStepAdded.size(); i++){
                    timeStepAdded.set(i, true);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Determines if an inference exists with a specified timestep.
     * @param inferences
     * @param desiredTime
     * @return
     */
    private boolean listContainsObjectWithDesiredTimeStep(List inferences, List<List<FactNode>> facts, int desiredTime, int size){
        for(int i=0; i<inferences.size(); i++){
            InferenceNode inf = (InferenceNode) inferences.get(i);
            if(inf.getTimeStep() == desiredTime){
                if(facts.get(i).size() <= size){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Stores each inference in a tree as the key of a map.  Maps from inferences to the list of all facts used to
     * make that inference.
     * @return
     */
    private HashMap<InferenceNode, List<FactNode>> assignFactsToInferenceNodes(){
        HashMap<InferenceNode, List<FactNode>> map = new HashMap<InferenceNode, List<FactNode>>();

        for(TreeNode tn : tm.tree){
            if(tn instanceof InferenceNode){
                map.put((InferenceNode) tn, findAllFactsForInference(tn));
            }
        }
        return map;
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
}