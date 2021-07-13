package Reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import RDFSupportGenerationTree.*;
import JSONHandler.*;
import JenaBuiltins.*;
import RDFGraphManipulations.ChangeInformation;
import RDFGraphManipulations.MapEncoding;
import RDFGraphManipulations.GetInformation;
import RDFGraphManipulations.ScaledIntegerMappedEncoding;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.ReasonerVocabulary;


public class ReasonerLogic {

    /**
     * Method to run reasoner which traces inference supports and keeps track of which time step the inference was
     * formed in.
     * @param filePath
     * @param rulePath
     * @param storageFilePath
     */
    public static InfModel[] runTimeStepReasoner(String filePath, String rulePath, String preProcessingPath, String storageFilePath) {

        InfModel preReasoning = null;
        InfModel inf = null;

        //Create empty ont model.
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        //Reads in the ontology at specified location.
        ontModel.read(filePath);

        try {
            //Adds pre-reasoning information to graph such as axioms and Datatype information.
            preReasoning = ReasonerLogic.reasonAndTraceModel(ontModel, preProcessingPath);

            //Create inference model.
            inf = ReasonerLogic.reasonAndTraceModel(preReasoning, rulePath);

            //Encode graph using a specific method of encoding.
            MapEncoding sie = new ScaledIntegerMappedEncoding(inf);

            //Creates Tree manager for in inference graph and a specified encoding.
            //It will handle all tree manipulations and queries.
            TreeManager tm = new TreeManager(inf, sie.getEncodedMap());

            //a infTree from a hashtable of treeNodes.
            List<TreeNode> tree = tm.createTree(tm.createTreeNodes());

            //Assign time steps for each tree node.
            for (TreeNode tn : tree) {
                TreeNode node = tn;
                if (node instanceof InferenceNode) {
                    tm.assignTimeSteps((InferenceNode) node);
                }
            }//end while

            //Wraps up all the data for serialization.
            PackageTreeData ptd = new PackageTreeData(tm, 25);

            //Creates Serializer object to serialize data in a particular json format.
            JsonSerializer js = new SerializeDeepReasonerData(ptd.getKBEncoded(), ptd.getSupportsEncoded(),
                    ptd.getOutputsEncoded(), ptd.getVectorMap(ptd.getKBEncoded().size()));

            js.writeJson(storageFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        InfModel[] arr = new InfModel[]{preReasoning, inf};
        return arr;
    }

    /**
     * Runs reasoner over data stored in owl files.
     */
    public static void runRegularReasonerOnOwl(){
        //gets files in specified folder
        File[] files = Main.getFiles("C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\real_world_ontology_input\\real_world_ontology_working_data");

        for(int i=0; i<files.length; i++) {

            //create empty ont model
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

            int count = 0;
            //input file to be parsed
            File file = files[i];
            try {
                //reads in the ontology at specified location.
                ontModel.read(file.toString());

                //Removes unwanted triples
                ChangeInformation.delCommmentTriples(ontModel);

                //Breaks it up into 1000 triple models
                List<OntModel> miniKGs = ChangeInformation.createMiniKGs(ontModel, 1000);

                for(OntModel model: miniKGs){

                    /*
                      Adds axioms to the ontology that is passed to final inference model so that
                      these axioms are not in the final deductions model.
                    */
                    InfModel axiomAddedModel = ReasonerLogic.reasonAndTraceModel(model, "ReasonerLogic\\RDF_RDFS_Axioms.txt");

                    //Creates inference model
                    InfModel infModel = ReasonerLogic.reasonAndTraceModel(axiomAddedModel, "ReasonerLogic\\Rules.txt");

                    //creates output file
                    count++;
                    String outputFileName = "OutputFiles/" + FileNameUtils.getBaseName(file.getName().toString()) + "_" + count + ".json";

                    JsonWriter.writeToJson(model.getNsPrefixURI(""), model.getNsPrefixMap(), GetInformation.getOriginalTriplesFromOntology(model),
                            GetInformation.getReasonedTriplesFromOntology(infModel), outputFileName);

                    System.out.println("Correctly ran " + outputFileName);

                }
            } catch (Exception e) {
                System.out.println("Error with file: " + files[i] + "-----" + e);
            }

            count = 0;
        }//end for
    }

    /**
     * Runs reasoner over data stored in json format.
     */
    public static void runRegularReasonerOnJson(){

        //gets files in specified folder
        File[] files = Main.getFiles("C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\TestFiles");

        for(int i=0; i<files.length; i++) {

            //create empty ont model
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

            //input file to be parsed
            File file = files[i];

            try {
                //gets file and creates parsed json date
                JsonParser jp = new JsonParser(file);

                //creates models
                CreateOntologyFromJson.createOntology(jp, ontModel);

                InfModel infModel = ReasonerLogic.reasonAndTraceModel(ontModel, "ReasonerLogic\\Rules.txt");

                //creates output file
                String outputFileName = "OutputFiles/" + file.getName().toString();

                /*//creates noisy triples
                List<String> correctTriples = Arrays.asList(ParseMethods.getOriginalTriplesFromOntology(ontModel));
                List<String> invalidTriples = new ArrayList<>();
                ReasonerLogic.makeNWrongTriples(jp.getSubjects(), jp.getObjects(), jp.getPredicates(),
                        correctTriples,invalidTriples, 5);
                */

                JsonWriter.writeToJson(jp.getOntologyName(), jp.getPrefixMap(), GetInformation.getOriginalTriplesFromOntology(ontModel),
                        GetInformation.getReasonedTriplesFromOntology(infModel), outputFileName);

                System.out.println("Correctly ran " + files[i]);

            } catch (Exception e) {
                System.out.println("Error with file: " + files[i] + "-----" + e);
            }
        }//end for loop
    }

    /**
     * Method to create custom reasoner, reason over ontology, and record the trace of every rule producing a new triple
     * @param model
     * @param ruleFile
     * @throws FileNotFoundException
     */
    public static InfModel reasonAndTraceModel(Model model, String ruleFile) throws FileNotFoundException {

        //register for custom method used in rule file
        BuiltinRegistry.theRegistry.register(new Property_Check());

        //load rules
        List<Rule> rules = Rule.rulesFromURL(ruleFile);

        //creates reasoner with custom rules and enables tracing
        org.apache.jena.reasoner.Reasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(true);
        reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, Boolean.TRUE);

        //creates an inference model using custom reasoner and the read in ontology model
        //contains the original KG and inferences
        InfModel inf = ModelFactory.createInfModel(reasoner, model);

        return inf;
    }

    /**
     * Method to make n number of invalid triples
     * @param allSubjects
     * @param allObjects
     * @param allPredicates
     * @param correctTriples
     * @param invalidTriples
     * @param n
     */
    public static void makeNWrongTriples(String[] allSubjects, String[] allObjects, String[] allPredicates,
                                         List<String> correctTriples, List<String> invalidTriples, int n){

        //create list of subjects and objects available to pick at random
        List<String> list = new ArrayList(Arrays.asList(allSubjects));
        list.addAll(Arrays.asList(allObjects));
        String[] allSubAndObj = list.toArray(new String[list.size()]);

        while(invalidTriples.size() < n){
            makeWrongTriple(allSubAndObj, allPredicates, correctTriples, invalidTriples);
        }
    }

    /**
     * Creates noisy triples using parts of existing triples
     * @param allSubAndObj
     * @param allPredicates
     * @param correctTriples
     * @param invalidTriples
     */
    private static void makeWrongTriple(String[] allSubAndObj, String[] allPredicates,
                                        List<String> correctTriples, List<String> invalidTriples){

        Random rand = new Random();

        //builds potential invalid triple
        StringBuilder sb = new StringBuilder();
        String subject = allSubAndObj[rand.nextInt(allSubAndObj.length)];
        String predicate = allPredicates[rand.nextInt(allPredicates.length)];
        String object = allSubAndObj[rand.nextInt(allSubAndObj.length)];

        sb.append(subject + " ");
        sb.append(predicate + " ");
        sb.append(object);

        if(!correctTriples.contains(sb.toString())){
            invalidTriples.add(sb.toString());
        }
    }
}
