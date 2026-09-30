package edu.rutmiit.demo.demorest.storage;

import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {

    public final Map<Long, MatchResponse> matches =
            new ConcurrentHashMap<>();

    public final AtomicLong matchSequence = new AtomicLong(0);
}