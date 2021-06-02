package JenaBuiltins;

import IOHandler.ReasonerLoggingCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.Builtin;
import org.apache.jena.reasoner.rulesys.RuleContext;

public class Log_Supports_For_One_Input implements Builtin {
    @Override
    public String getName() {
        return "logSupports_1Input";
    }

    @Override
    public String getURI() {
        return null;
    }

    @Override
    public int getArgLength() {
        //Will take in one triple and one inferred triple.
        return 6;
    }

    @Override
    public boolean bodyCall(Node[] nodes, int i, RuleContext ruleContext) {
        return false;
    }

    @Override
    public void headAction(Node[] nodes, int i, RuleContext ruleContext) {
        // Take nodes and make triples out of them.
        Triple t1 = new Triple(nodes[0], nodes[1], nodes[2]);
        Triple inf = new Triple(nodes[3], nodes[4], nodes[5]);

        // Pass triples to static ReasonerLoggingCoordinator class.
        try {
            ReasonerLoggingCoordinator.logTriples(t1, inf);
        }
        catch(Exception e){
            System.out.println("Error in builtin");
        }
    }

    @Override
    public boolean isSafe() {
        return false;
    }

    @Override
    public boolean isMonotonic() {
        return false;
    }
}
