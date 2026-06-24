package edu.rutmiit.demo.grpcenrichment.listener;

import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.MatchEvent;
import edu.rutmiit.demo.grpc.AnalyzeMatchRequest;
import edu.rutmiit.demo.grpc.MatchAnalysisResponse;
import edu.rutmiit.demo.grpc.MatchAnalyticsGrpc;
import edu.rutmiit.demo.grpcenrichment.config.RabbitMQConfig;
import edu.rutmiit.demo.grpcenrichment.publisher.EnrichmentEventPublisher;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Слушатель событий book.created из RabbitMQ.
 *
 * Десериализация — ручная (как в audit-service), потому что EventEnvelope<T>
 * является generic-типом, и Jackson не может определить конкретный подтип T.
 */
@Component
public class MatchEnrichmentListener {

    private final MatchAnalyticsGrpc.MatchAnalyticsBlockingStub stub;
    private final EnrichmentEventPublisher publisher;

    public MatchEnrichmentListener(
            MatchAnalyticsGrpc.MatchAnalyticsBlockingStub stub,
            EnrichmentEventPublisher publisher
    ) {
        this.stub = stub;
        this.publisher = publisher;
    }

    @RabbitListener(
            queues = RabbitMQConfig.ENRICHMENT_QUEUE
    )
    public void handleGoalScored(
            EventEnvelope<MatchEvent.GoalScored> envelope
    ) {

        MatchEvent.GoalScored event =
                envelope.payload();

        AnalyzeMatchRequest request =
                AnalyzeMatchRequest.newBuilder()
                        .setMatchId(event.matchId())
                        .setHomeScore(event.homeScore())
                        .setAwayScore(event.awayScore())
                        .build();

        MatchAnalysisResponse response =
                stub.analyzeMatch(request);

        MatchEvent.Enriched enriched =
                new MatchEvent.Enriched(
                        response.getMatchId(),
                        response.getLeader(),
                        response.getGoalDifference()
                );

        publisher.publishEnriched(enriched);
    }
}
