package edu.rutmiit.demo.grpcanalytics.service;

import edu.rutmiit.demo.grpc.AnalyzeMatchRequest;
import edu.rutmiit.demo.grpc.MatchAnalysisResponse;
import edu.rutmiit.demo.grpc.MatchAnalyticsGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchAnalyticsServiceImpl
        extends MatchAnalyticsGrpc.MatchAnalyticsImplBase {

    private static final Logger log =
            LoggerFactory.getLogger(MatchAnalyticsServiceImpl.class);

    @Override
    public void analyzeMatch(AnalyzeMatchRequest request, StreamObserver<MatchAnalysisResponse> responseObserver) {
        log.info("Получен запрос на анализ матча {}", request.getMatchId());

        String leader;

        if (request.getHomeScore() > request.getAwayScore()) {
            leader = request.getHomeTeamName();
        } else if (request.getAwayScore() > request.getHomeScore()) {
            leader = request.getAwayTeamName();
        } else {
            leader = "Ничья";
        }

        int homeScore = request.getHomeScore();
        int awayScore = request.getAwayScore();

        int goalDifference = Math.abs(homeScore - awayScore);


        MatchAnalysisResponse response =
                MatchAnalysisResponse.newBuilder()
                        .setMatchId(request.getMatchId())
                        .setLeader(leader)
                        .setGoalDifference(goalDifference)
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}