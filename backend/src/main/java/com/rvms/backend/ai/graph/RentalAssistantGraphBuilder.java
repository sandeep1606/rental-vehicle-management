package com.rvms.backend.ai.graph;

import com.rvms.backend.ai.graph.nodes.*;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Builds and compiles the 7-node LangGraph4j workflow that powers the AI assistant:
 *
 * <pre>
 *                                   +-> extractRentalSearchCriteria -> searchAvailableVehicles -> recommendVehicle -+
 * START -> classifyIntent -(route)-+-> searchCustomerWithLRU ------------------------------------------------------+-> finalResponse -> END
 *                                   +-> answerPolicyQuestion -------------------------------------------------------+
 *                                   +-> (general, no extra node) --------------------------------------------------+
 * </pre>
 *
 * NOTE: written against org.bsc.langgraph4j:langgraph4j-core 1.2.x. This is a smaller,
 * fast-moving library — if a different version is pinned in pom.xml and a method
 * signature differs, this is the only class in the app that touches the langgraph4j API,
 * so it's the only place that would need adjusting.
 */
@Component
@RequiredArgsConstructor
public class RentalAssistantGraphBuilder {

    private final ClassifyIntentNode classifyIntentNode;
    private final ExtractRentalSearchCriteriaNode extractRentalSearchCriteriaNode;
    private final SearchCustomerWithLruNode searchCustomerWithLruNode;
    private final SearchAvailableVehiclesNode searchAvailableVehiclesNode;
    private final AnswerPolicyQuestionNode answerPolicyQuestionNode;
    private final RecommendVehicleNode recommendVehicleNode;
    private final FinalResponseNode finalResponseNode;

    public CompiledGraph<RentalAssistantState> build() throws GraphStateException {
        StateGraph<RentalAssistantState> graph = new StateGraph<>(RentalAssistantState::new)
                .addNode("classifyIntent", node_async(classifyIntentNode))
                .addNode("extractRentalSearchCriteria", node_async(extractRentalSearchCriteriaNode))
                .addNode("searchCustomerWithLRU", node_async(searchCustomerWithLruNode))
                .addNode("searchAvailableVehicles", node_async(searchAvailableVehiclesNode))
                .addNode("answerPolicyQuestion", node_async(answerPolicyQuestionNode))
                .addNode("recommendVehicle", node_async(recommendVehicleNode))
                .addNode("finalResponse", node_async(finalResponseNode))

                .addEdge(START, "classifyIntent")
                .addConditionalEdges("classifyIntent", edge_async(this::routeAfterClassify), Map.of(
                        "search", "extractRentalSearchCriteria",
                        "customer", "searchCustomerWithLRU",
                        "policy", "answerPolicyQuestion",
                        "general", "finalResponse"
                ))
                .addEdge("extractRentalSearchCriteria", "searchAvailableVehicles")
                .addEdge("searchAvailableVehicles", "recommendVehicle")
                .addEdge("recommendVehicle", "finalResponse")
                .addEdge("searchCustomerWithLRU", "finalResponse")
                .addEdge("answerPolicyQuestion", "finalResponse")
                .addEdge("finalResponse", END);

        return graph.compile();
    }

    private String routeAfterClassify(RentalAssistantState state) {
        return IntentRouter.route(state.intent().orElse(IntentType.GENERAL));
    }
}
