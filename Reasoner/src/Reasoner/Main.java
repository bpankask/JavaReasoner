package Reasoner;

import java.io.*;

public class Main {

    public static void main(String[] args){
        String filePath = "C:\\Users\\Brayden Pankaskie\\Desktop\\LabStuff\\real_world_ontology_input\\real_world_ontology_working_data\\dbpedia_2015.owl";
        String rulePath = "src\\RulesTimeStep.txt";
        String tracePath = "OutputFiles\\dbpedia_2015_16_8.json";
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