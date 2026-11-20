package crml.web.teavm;

import java.util.Collections;
import java.util.List;

import org.teavm.model.ClassHolder;
import org.teavm.model.ClassHolderTransformer;
import org.teavm.model.ClassHolderTransformerContext;
import org.teavm.model.MethodHolder;
import org.teavm.model.emit.ProgramEmitter;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;

/**
 * Adapts EMF to the browser while TeaVM compiles it.
 *
 * <p>EPackageRegistryImpl.createGlobalRegistry chooses the registry by system
 * property, Eclipse plug-in, security manager and class loader, none of which
 * exist in a browser, and TeaVM rejects the classes they need. In a browser
 * the registry is always the plain one.
 *
 * <p>URIHandler.DEFAULT_HANDLERS, the URI handlers every URI converter
 * starts with, read the Eclipse workspace, EFS, archives and files; TeaVM
 * rejects the classes they need. A resource set reaches them only to resolve a
 * proxy or to load a resource by URI, and the CRML workspace does neither in a
 * browser: it loads text, and makes no proxies. In a browser there are no
 * default handlers, and EcorePlugin.getWorkspaceRoot, which the URI converter
 * also asks, returns null, as it does outside Eclipse.
 */
public class EmfPlugin implements TeaVMPlugin, ClassHolderTransformer {
  private static final String REGISTRY = "org.eclipse.emf.ecore.impl.EPackageRegistryImpl";
  private static final String URI_HANDLER = "org.eclipse.emf.ecore.resource.URIHandler";
  private static final String ECORE_PLUGIN = "org.eclipse.emf.ecore.plugin.EcorePlugin";

  @Override
  public void install(TeaVMHost host) {
    host.add(this);
  }

  @Override
  public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
    if (cls.getName().equals(REGISTRY)) {
      for (MethodHolder method : cls.getMethods()) {
        if (method.getName().equals("createGlobalRegistry")) {
          ProgramEmitter pe = ProgramEmitter.create(method, context.getHierarchy());
          pe.construct(REGISTRY).returnValue();
        }
      }
    } else if (cls.getName().equals(URI_HANDLER)) {
      for (MethodHolder method : cls.getMethods()) {
        if (method.getName().equals("<clinit>")) {
          ProgramEmitter pe = ProgramEmitter.create(method, context.getHierarchy());
          pe.setField(URI_HANDLER, "DEFAULT_HANDLERS", pe.invoke(Collections.class, "emptyList", List.class));
          pe.exit();
        }
      }
    } else if (cls.getName().equals(ECORE_PLUGIN)) {
      for (MethodHolder method : cls.getMethods()) {
        if (method.getName().equals("getWorkspaceRoot")) {
          ProgramEmitter pe = ProgramEmitter.create(method, context.getHierarchy());
          pe.constantNull(method.getResultType()).returnValue();
        }
      }
    }
  }
}
