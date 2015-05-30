package net.openhft.chronicle.engine2.pubsub;

import net.openhft.chronicle.engine2.api.*;
import net.openhft.chronicle.engine2.session.LocalSession;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by peter on 29/05/15.
 */
public class SimpleSubscription<E> implements Subscription {
    private final Set<Subscriber<E>> subscribers = new CopyOnWriteArraySet<>();

    @Override
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    @Override
    public <E> void registerSubscriber(RequestContext rc, Subscriber<E> subscriber) {
        subscribers.add((Subscriber) subscriber);
    }

    @Override
    public <T, E> void registerTopicSubscriber(RequestContext rc, TopicSubscriber<T, E> subscriber) {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void unregisterSubscriber(RequestContext rc, Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void unregisterTopicSubscriber(RequestContext rc, TopicSubscriber subscriber) {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void registerDownstream(RequestContext rc, Subscription subscription) {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void unregisterDownstream(RequestContext rc, Subscription subscription) {
        throw new UnsupportedOperationException("todo");
    }

    public void notifyMessage(E e) {
        subscribers.forEach(s -> s.onMessage(e));
    }

    @Override
    public View forSession(LocalSession session, Asset asset) {
        SimpleSubscription<E> ss = new SimpleSubscription<>();
        registerDownstream(RequestContext.requestContext(), ss);
        return ss;
    }
}