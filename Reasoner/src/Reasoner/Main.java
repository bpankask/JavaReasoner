package Reasoner;

import JSONHandler.CreateOntologyFromJson;
import JSONHandler.JsonParser;
import JSONHandler.JsonWriter;
import RDFGraphManipulations.ChangeInformation;
import RDFGraphManipulations.GetInformation;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.util.List;

public class Main {

    public static void main(String[] args){



    }//end main

    /**
     * Gets files in specified folder
     * @param folderName
     * @return
     */
    public static File[] getFiles(String folderName) {
        File folder = new File(folderName);
        File[] files = folder.listFiles();
        return files;
    }

    /**
     * Runs reasoner over data stored in owl files.
     */
    public static void runRegularReasonerOnOwl(){
        //gets files in specified folder
        File[] files = getFiles("C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\real_world_ontology_input\\real_world_ontology_working_data");

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
                    InfModel axiomAddedModel = TracingReasoner.reasonAndTraceModel(model, "Reasoner\\RDF_RDFS_Axioms.txt");

                    //Creates inference model
                    InfModel infModel = TracingReasoner.reasonAndTraceModel(axiomAddedModel, "Reasoner\\Rules.txt");

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
        File[] files = getFiles("C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\TestFiles");

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

                InfModel infModel = TracingReasoner.reasonAndTrace(ontModel, "Reasoner\\Rules.txt");

                //creates output file
                String outputFileName = "OutputFiles/" + file.getName().toString();

                /*//creates noisy triples
                List<String> correctTriples = Arrays.asList(ParseMethods.getOriginalTriplesFromOntology(ontModel));
                List<String> invalidTriples = new ArrayList<>();
                TracingReasoner.makeNWrongTriples(jp.getSubjects(), jp.getObjects(), jp.getPredicates(),
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
}
