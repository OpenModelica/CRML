within Inside;
model Inside_verif
  extends Inside;
  Inside_externals externals
    annotation (Placement(transformation(extent={{-200,20},{-140,80}})));
  // C1 is built as the compiler builds 'new Clock b', from the Boolean whose rising
  // edges make the externals' clock (bool2: 2 s, 3.5 s, 6 s). externals.C1 is not
  // used: CRML.CompilerCompliancy.CRMLClockClock keeps counter one past the last tick
  // (ours: at the last tick), and records only the first tick, as its b is the held
  // output of ClockToBoolean, which stays true.
  CRMLtoModelica.Types.CRMLClock_build C1_init(clock = C1);
equation
  // Bindings
  C1.b = CRMLtoModelica.Functions.cvBooleanToBoolean4(externals.bool2.y);
  P1 =externals.P1;
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
end Inside_verif;
