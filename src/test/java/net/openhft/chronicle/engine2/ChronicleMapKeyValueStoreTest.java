package net.openhft.chronicle.engine2;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.engine2.api.Asset;
import net.openhft.chronicle.engine2.api.map.KeyValueStore;
import net.openhft.chronicle.engine2.api.map.MapEvent;
import net.openhft.chronicle.engine2.api.map.MapEventListener;
import net.openhft.chronicle.engine2.api.map.MapView;
import net.openhft.chronicle.engine2.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine2.map.VanillaMapView;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static net.openhft.chronicle.engine2.Chassis.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by daniel on 28/05/15.
 */
public class ChronicleMapKeyValueStoreTest {
    public static final String NAME = "chronmapkvstoretests";
    private static Map<String, Factor> map;

    @BeforeClass
    public static void createMap() throws IOException {

        resetChassis();
        Function<Bytes, Wire> writeType = TextWire::new;

        viewTypeLayersOn(MapView.class, "map directly to KeyValueStore", KeyValueStore.class);
        registerFactory("", KeyValueStore.class, (context, asset, underlyingSupplier) -> new ChronicleMapKeyValueStore(context.wireType(writeType), asset));

        map = acquireMap(NAME, String.class, Factor.class);
        KeyValueStore mapU = ((VanillaMapView) map).underlying();
        assertEquals(ChronicleMapKeyValueStore.class, mapU.getClass());

        //just in case it hasn't been cleared up last time
        map.clear();
    }

    @Test
    public void test() {
        AtomicInteger success = new AtomicInteger();
        MapEventListener<String, Factor> listener = new MapEventListener<String, Factor>() {
            @Override
            public void update(String key, Factor oldValue, Factor newValue) {
                System.out.println("Updated { key: " + key + ", oldValue: " + oldValue + ", value: " + newValue + " }");
                success.set(-1000);
            }

            @Override
            public void insert(String key, Factor value) {
                System.out.println("Inserted { key: " + key + ", value: " + value + " }");
                success.incrementAndGet();
            }

            @Override
            public void remove(String key, Factor oldValue) {
                System.out.println("Removed { key: " + key + ", value: " + oldValue + " }");
                success.set(-100);
            }
        };

        Asset asset = getAsset(NAME);
        registerSubscriber(NAME, MapEvent.class, e -> e.apply(listener));
        //ChronicleMapKeyValueStore sbskvStore = asset.acquireView(ChronicleMapKeyValueStore.class);
        //sbskvStore.registerSubscriber(MapEvent.class, (x) ->
        //        System.out.println(x), "");

        Factor factor = new Factor();
        factor.setAccountNumber("xyz");
        map.put("testA", factor);
        assertEquals(1, map.size());
        assertEquals("xyz", map.get("testA").getAccountNumber());


        expectedSuccess(success, 1);
        success.set(0);

        factor.setAccountNumber("abc");
        map.put("testA", factor);

        expectedSuccess(success, -1000);
        success.set(0);

        map.remove("testA");

        expectedSuccess(success, -100);
        success.set(0);

    }

    private void expectedSuccess(AtomicInteger success, int expected){
        for (int i = 0; i < 20; i++) {
            if (success.get() == expected)
                break;
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(expected, success.get());
    }
}