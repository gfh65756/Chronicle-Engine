package net.openhft.chronicle.engine2.api.map;

import net.openhft.chronicle.engine2.api.Subscriber;
import net.openhft.chronicle.engine2.api.View;

/**
 * Created by peter on 30/05/15.
 */
public interface KeySubscriber<K> extends Subscriber<K>, View {
}
