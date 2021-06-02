package JenaBuiltins;

import IOHandler.ReasonerLoggingCoordinator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.Builtin;
import org.apache.jena.reasoner.rulesys.RuleContext;

public class Log_Supports_For_Two_Inputs implements Builtin {
    @Override
    public String getName() {
        return "logSupports_2Inputs";
    }

    @Override
    public String getURI() {
        return null;
    }

    @Override
    public int getArgLength() {
        // Will take in two triples and one inferred triple.
        return 9;
    }

    @Override
    public boolean bodyCall(Node[] nodes, int i, RuleContext ruleContext) {
        return false;
    }

    @Override
    public void headAction(Node[] nodes, int i, RuleContext ruleContext) {
        // Take nodes and make triples out of them.
        Triple t1 = new Triple(nodes[0], nodes[1], nodes[2]);
        Triple t2 = new Triple(nodes[3], nodes[4], nodes[5]);
        Triple inf = new Triple(nodes[6], nodes[7], nodes[8]);

        // Pass triples to static ReasonerLoggingCoordinator class.
        try {
            ReasonerLoggingCoordinator.logTriples(t1, t2, inf);
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
