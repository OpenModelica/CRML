package crml.compiler.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SharedParameter implements ParameterResolver {
    public static final ExtensionContext.Namespace MESSAGE_NAMESPACE =
            ExtensionContext.Namespace.create("OMC", "message");
    public static final String OMC_MESSAGE_KEY = "OMC Files";
    public static final String CRML_FILE_KEY = "CRML File";

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.getParameter().getType().equals(ExtensionContext.Store.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        return ec.getStore(MESSAGE_NAMESPACE);
    }
    
    public static Map<String, Object> asMap(ExtensionContext context){
        ExtensionContext.Store store = context.getStore(SharedParameter.MESSAGE_NAMESPACE);
        Map<String, Object> map = new HashMap<>();

        addIfExists(map, CRML_FILE_KEY, store);
        addIfExists(map, OMC_MESSAGE_KEY, store);

        return map;
    }

    private static void addIfExists(Map<String, Object> map, String key, ExtensionContext.Store store){
        Object value = store.get(key);
        if(value!=null){
            map.put(key, value);
        }
    }
}
