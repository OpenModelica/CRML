package crml.compiler.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class SharedParameter implements ParameterResolver {
    public static final ExtensionContext.Namespace MESSAGE_NAMESPACE =
            ExtensionContext.Namespace.create("OMC", "message");
    public static final String OMC_MESSAGE_KEY = "omcmessage";

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.getParameter().getType().equals(ExtensionContext.Store.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        return ec.getStore(MESSAGE_NAMESPACE);
    }
    
}
