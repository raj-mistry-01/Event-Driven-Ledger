package com.ledger.history_service.infrastructure.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

public class MicrosecondInstantDeserializer
        extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(
            JsonParser parser,
            DeserializationContext context) throws IOException {

        long micros = parser.getLongValue();

        long seconds = micros / 1_000_000;
        long nanos = (micros % 1_000_000) * 1_000;

        return Instant.ofEpochSecond(seconds, nanos);
    }
}