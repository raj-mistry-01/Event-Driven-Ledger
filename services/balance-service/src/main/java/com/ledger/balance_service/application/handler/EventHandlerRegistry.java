package com.ledger.balance_service.application.handler;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventHandlerRegistry {

    private final Map<Integer, EventHandler> handlerMap = new HashMap<>();

    public EventHandlerRegistry(List<EventHandler> handlers) {

        for (EventHandler handler : handlers) {
            handlerMap.put(handler.supportedEventType(), handler);
        }
    }

    public EventHandler getHandler(int eventType) {

        EventHandler handler = handlerMap.get(eventType);

        if (handler == null) {
            throw new IllegalStateException("No handler found for event type: " + eventType);
        }

        return handler;
    }

}