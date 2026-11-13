package crml.web.teavm;

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
 */
public class EmfPlugin implements TeaVMPlugin, ClassHolderTransformer {
  private static final String REGISTRY = "org.eclipse.emf.ecore.impl.EPackageRegistryImpl";

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
    }
  }
}
