package crml.language.specification.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SharedParameter implements ParameterResolver {
    public static class Keys extends HashSet<String> {
        public Keys(Object _ignored){};
        public static final String keyCode = "crml.language.specification.util.SharedParameter.Keys.keyCode";
    }

    public static final ExtensionContext.Namespace MESSAGE_NAMESPACE =
            ExtensionContext.Namespace.create("CRML", "message");

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.getParameter().getType().equals(ExtensionContext.Store.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        return ec.getStore(MESSAGE_NAMESPACE);
    }
    
    public static void registerKey(String key, ExtensionContext.Store context){
        if(key==Keys.keyCode){
            throw new IllegalArgumentException("Store key \""+key + "\" is reserved.");
        }

        Keys keys = context.getOrComputeIfAbsent("keys", Keys::new, Keys.class);
        keys.add(key);
    }

    public static Map<String, Object> asMap(ExtensionContext context){
        ExtensionContext.Store store = context.getStore(SharedParameter.MESSAGE_NAMESPACE);
        Map<String, Object> map = new HashMap<>();


        Keys keys = store.getOrComputeIfAbsent("keys", Keys::new, Keys.class);
        keys.forEach(key -> addIfExists(map, key, store));

        return map;
    }

    private static void addIfExists(Map<String, Object> map, String key, ExtensionContext.Store store){
        Object value = store.get(key);
        if(value!=null){
            map.put(key, value);
        }
    }
}
