package RDFSupportGenerationTree;

import org.apache.jena.graph.Triple;

import java.util.List;

/**
 * Interface which sets the standard for a node in a tree like structure created from linking inference with
 * their supporting evidence.
 */
public interface TreeNode {
    Triple getValue();
    int getTimeStep();
    void setTimeStep(int x);
    List<Double> getEncoding();
}
