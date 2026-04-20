package crml.model;

import crml.model.language.BinaryOpExpr;
import crml.model.language.BuiltinOpKind;
import crml.model.language.BuiltinType;
import crml.model.language.ClassDef;
import crml.model.language.Definition;
import crml.model.language.DefinitionType;
import crml.model.language.Element;
import crml.model.language.IdentExpr;
import crml.model.language.LanguageFactory;
import crml.model.language.MemberVarDef;
import crml.model.language.OperatorDef;
import crml.model.language.OperatorSignaturePart;
import crml.model.language.TypeDef;
import crml.model.language.TypeRef;
import crml.model.language.VarDef;

import java.util.stream.Collectors;

/**
 * Demonstrates building a CRML domain model using the EMF classes generated
 * from crml.xcore.  The model built here corresponds to:
 *
 *   model SpeedMonitor is {
 *     class Vehicle is { Real speed; Boolean moving; };
 *     type Distance extends Real {};
 *     Real threshold;
 *     Operator [Boolean] 'above' Real x = x > threshold;
 *   };
 *
 * Run with:  ./gradlew run
 */
public class Main {

    public static void main(String[] args) {

        LanguageFactory f = LanguageFactory.eINSTANCE;

        // ── Top-level definition ──────────────────────────────────────────
        Definition model = f.createDefinition();
        model.setKind(DefinitionType.MODEL);
        model.setName("SpeedMonitor");

        // ── class Vehicle is { Real speed; Boolean moving; }; ─────────────
        ClassDef vehicleClass = f.createClassDef();
        vehicleClass.setName("Vehicle");

        MemberVarDef speedField = f.createMemberVarDef();
        speedField.setVarType(builtinType(f, BuiltinType.REAL));
        speedField.getNames().add("speed");

        MemberVarDef movingField = f.createMemberVarDef();
        movingField.setVarType(builtinType(f, BuiltinType.BOOLEAN));
        movingField.getNames().add("moving");

        vehicleClass.getMembers().add(speedField);
        vehicleClass.getMembers().add(movingField);
        model.getElements().add(vehicleClass);

        // ── type Distance extends Real {}; ────────────────────────────────
        TypeDef distanceType = f.createTypeDef();
        distanceType.setName("Distance");
        distanceType.setSuperType(builtinType(f, BuiltinType.REAL));
        model.getElements().add(distanceType);

        // ── Real threshold; ───────────────────────────────────────────────
        VarDef thresholdVar = f.createVarDef();
        thresholdVar.setVarType(builtinType(f, BuiltinType.REAL));
        thresholdVar.getNames().add("threshold");
        model.getElements().add(thresholdVar);

        // ── Operator [Boolean] 'above' Real x = x > threshold; ───────────
        OperatorDef aboveOp = f.createOperatorDef();
        aboveOp.setReturnType(builtinType(f, BuiltinType.BOOLEAN));

        OperatorSignaturePart kwPart = f.createOperatorSignaturePart();
        kwPart.setUserKeyword("'above'");
        aboveOp.getSignature().add(kwPart);

        OperatorSignaturePart paramPart = f.createOperatorSignaturePart();
        paramPart.setParamType(builtinType(f, BuiltinType.REAL));
        paramPart.setParamName("x");
        aboveOp.getSignature().add(paramPart);

        BinaryOpExpr gtExpr = f.createBinaryOpExpr();
        IdentExpr xRef = f.createIdentExpr();
        xRef.setName("x");
        IdentExpr thRef = f.createIdentExpr();
        thRef.setName("threshold");
        gtExpr.setLeft(xRef);
        gtExpr.setOpkind(BuiltinOpKind.GT);
        gtExpr.setRight(thRef);
        aboveOp.setBody(gtExpr);
        model.getElements().add(aboveOp);

        // ── Print summary ─────────────────────────────────────────────────
        System.out.printf("=== CRML %s: %s ===%n%n", model.getKind(), model.getName());
        for (Element el : model.getElements()) {
            System.out.println("  " + describe(el));
        }
    }

    private static TypeRef builtinType(LanguageFactory f, BuiltinType kind) {
        TypeRef t = f.createTypeRef();
        t.setBuiltinKind(kind);
        return t;
    }

    private static String describe(Element el) {
        if (el instanceof ClassDef) {
            ClassDef cd = (ClassDef) el;
            return "class " + cd.getName() + " { "
                    + cd.getMembers().size() + " member(s) }";
        }
        if (el instanceof TypeDef) {
            TypeDef td = (TypeDef) el;
            return "type " + td.getName()
                    + " extends " + td.getSuperType().getBuiltinKind();
        }
        if (el instanceof VarDef) {
            VarDef vd = (VarDef) el;
            return vd.getVarType().getBuiltinKind()
                    + " " + String.join(", ", vd.getNames());
        }
        if (el instanceof OperatorDef) {
            OperatorDef op = (OperatorDef) el;
            String sig = op.getSignature().stream()
                    .map(p -> p.getUserKeyword() != null
                            ? p.getUserKeyword()
                            : p.getParamType().getBuiltinKind() + " " + p.getParamName())
                    .collect(Collectors.joining(" "));
            return "Operator [" + op.getReturnType().getBuiltinKind() + "] " + sig;
        }
        return el.getClass().getSimpleName();
    }
}
