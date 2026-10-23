package crml.web.teavm;

import org.teavm.extension.spi.substitution.SimpleSubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

/**
 * TeaVM provides no javax.xml; EMF's XML types refer to a few of its classes,
 * so they are taken from crml.web.emul.javax.xml instead.
 */
public class JavaxXmlSubstitution extends SimpleSubstitutionPolicy {
  @Override
  public void contribute(SubstitutionSink sink) {
    sink.selectClasses(inPackage("javax.xml", true)).packagePrefix("crml.web.emul.");
  }
}
