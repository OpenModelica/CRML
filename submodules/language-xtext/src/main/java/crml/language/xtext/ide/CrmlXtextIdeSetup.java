package crml.language.xtext.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import crml.language.xtext.CrmlXtextRuntimeModule;
import crml.language.xtext.CrmlXtextStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class CrmlXtextIdeSetup extends CrmlXtextStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new CrmlXtextRuntimeModule(), new CrmlXtextIdeModule()));
	}
	
}
