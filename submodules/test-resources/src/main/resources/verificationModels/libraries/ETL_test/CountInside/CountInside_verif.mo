within CountInside;
model CountInside_verif
  extends CountInside;
  Modelica.Clocked.BooleanSignals.NonPeriodic.ClockToBoolean c2b;
  CountInside_externals externals
    annotation (Placement(transformation(extent={{-200,0},{-140,60}})));
equation
  // Bindings: the externals of the CRML model are b1 and b2; P1 and C1 are
  // built from them by the model itself. externals.C1 is a Modelica Clock,
  // turned into a Boolean that is true at its ticks.
  b1 = CRMLtoModelica.Functions.cvBooleanToBoolean4(externals.bool1.y);
  c2b.u = externals.C1;
  b2 = CRMLtoModelica.Functions.cvBooleanToBoolean4(c2b.y);
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
