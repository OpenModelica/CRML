within CRML_test.ETL.CountInside;
model CountInside_verif
  extends CountInside;
  CountInside_externals externals
    annotation (Placement(transformation(extent={{-200,0},{-140,60}})));
equation
  // Bindings
  // NOTE: Reverted to the bindings of commit 922c396 ("add CRML_test Modelica library").
  // Commit 24c8067 ("verification scenario update") changed them to
  //   P1 = externals.P1; c2b.u = externals.C1;
  //   C1.b = CRMLtoModelica.Functions.cvBooleanToBoolean4(c2b.y);
  // (with a ClockToBoolean c2b), which does not match this library:
  // C1 is a Clock (no field b) and P1 is a TimeLocator, not a CRMLPeriod.
  // Those bindings fit the compiler-generated model instead
  // (see verificationModels/libraries/ETL_test/CountInside/CountInside_verif.mo).
  P1 =externals.tl1;
  C1 = externals.C1;
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
