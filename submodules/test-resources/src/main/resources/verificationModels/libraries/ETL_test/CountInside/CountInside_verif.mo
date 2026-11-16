within CountInside;
model CountInside_verif
  extends CountInside;
  CountInside_externals externals
    annotation (Placement(transformation(extent={{-200,0},{-140,60}})));
equation
  // Bindings
  // NOTE: Commit 24c8067 ("verification scenario update") tried to fix this model by converting
  // the Modelica clock externals.C1 to the CRMLClock C1 (in libraries/modelica/CRML_test, see the
  // note there). But in CountInside.crml only b1 and b2 are external: P1 and C1 are computed
  // from them (P1 = [new Event b1, new Event (not b1)], C1 = new Clock b2), so binding P1 and C1
  // over-determines the model. The external Booleans are bound instead:
  // b1 drives the period (externals.P1), b2 the clock ticks (externals.C1).
  b1 = externals.booleanToBoolean4_1.y;
  b2 = CRMLtoModelica.Functions.cvBooleanToBoolean4(externals.bool2.y);
    annotation (Placement(transformation(extent={{0,-20},{60,40}})),
              Icon(coordinateSystem(preserveAspectRatio=false,
        extent={{-200,-100},{100,100}},
        initialScale=0.1),                                      graphics={
        Ellipse(lineColor = {75,138,73},
                fillColor={255,255,255},
                fillPattern = FillPattern.Solid,
                extent = {{-100,-100},{100,100}}),
        Polygon(lineColor = {0,0,255},
                fillColor = {75,138,73},
                pattern = LinePattern.None,
                fillPattern = FillPattern.Solid,
                points = {{-36,60},{64,0},{-36,-60},{-36,60}})}), Diagram(
        coordinateSystem(preserveAspectRatio=false,
        extent={{-200,-100},{100,100}},
        initialScale=0.1)),
    experiment(StopTime=14));
end CountInside_verif;
