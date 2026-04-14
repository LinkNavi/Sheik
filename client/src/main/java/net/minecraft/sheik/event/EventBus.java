package net.minecraft.sheik.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static final Map<Class<?>, List<IEventListener<?>>> listeners = new HashMap<>();

    public static <T> void subscribe(Class<T> type, IEventListener<T> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public static <T> void unsubscribe(Class<T> type, IEventListener<T> listener) {
        List<IEventListener<?>> list = listeners.get(type);
        if (list != null) list.remove(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T> void post(T event) {
        List<IEventListener<?>> list = listeners.get(event.getClass());
        if (list == null) return;
        for (IEventListener<?> l : list) {
            ((IEventListener<T>) l).onEvent(event);
        }
    }

    public interface IEventListener<T> {
        void onEvent(T event);
    }
}
