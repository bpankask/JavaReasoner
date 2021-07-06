package RDFSupportGenerationTree;

import java.util.Comparator;

/**
 * Used to sort a list of TreeNodes so that the nodes with the greatest timestep count are in front.
 */
public class SortByTimeStep implements Comparator<TreeNode> {
    @Override
    public int compare(TreeNode o1, TreeNode o2) {
        if(o1.getTimeStep() > o2.getTimeStep()){
            return -1;
        }
        if(o1.getTimeStep() < o2.getTimeStep()){
            return 1;
        }
        return 0;
    }
}
