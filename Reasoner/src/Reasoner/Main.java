package Reasoner;

import InferenceTree.*;
import JSONHandler.CreateOntologyFromJson;
import JSONHandler.JsonParser;
import JSONHandler.JsonWriter;
import RDFGraphManipulations.ChangeInformation;
import RDFGraphManipulations.EncodeMethod;
import RDFGraphManipulations.GetInformation;
import RDFGraphManipulations.ScaledIntegerEncoding;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args){
        String filePath = "C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\TestFiles\\gfo-1.0.owl";
        String rulePath = "src\\RulesTimeStep.txt";
        String tracePath = "OutputFiles\\traceTest";
        String preProcessingPath = "src\\PreprocessingTimeStep.txt";

        ReasonerLogic.runTimeStepReasoner(filePath, rulePath, preProcessingPath, tracePath);

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
}