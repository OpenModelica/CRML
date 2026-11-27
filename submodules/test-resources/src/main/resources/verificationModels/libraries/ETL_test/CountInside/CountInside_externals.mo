within CountInside;
model CountInside_externals
  // Only b1 and b2 are external in CountInside.crml (P1 and C1 are computed from them),
  // so only these two Boolean4 signals are provided. Do not add unused Modelica clock
  // outputs here: an unconnected clock creates an empty clocked partition, for which
  // OpenModelica generates C code that does not compile ("duplicate case value").

public
  CRML.Blocks.Logical.BooleanTable bool1(
    y0=false,
    option_width=false,
    instant={2.5,5}) "Drives the period P1"
    annotation (Placement(transformation(extent={{-60,-60},{-40,-40}})));
  CRML.Blocks.Logical.BooleanTable bool2(
    y0=false,
    option_width=false,
    instant={2,3,3.5,4.5,6,7.5}) "Drives the clock C1"
    annotation (Placement(transformation(extent={{-60,40},{-40,60}})));
  CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1
    annotation (Placement(transformation(extent={{-4,-54},{4,-46}})));
  CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2
    annotation (Placement(transformation(extent={{-4,46},{4,54}})));
  CRML.ETL.Connectors.Boolean4Output b1
    annotation (Placement(transformation(extent={{100,-60},{120,-40}})));
  CRML.ETL.Connectors.Boolean4Output b2
    annotation (Placement(transformation(extent={{100,40},{120,60}})));
equation
  connect(bool1.y, booleanToBoolean4_1.u)
    annotation (Line(points={{-39,-50},{-4.4,-50}}, color={217,67,180}));
  connect(booleanToBoolean4_1.y, b1)
    annotation (Line(points={{4.4,-50},{110,-50}}, color={162,29,33}));
  connect(bool2.y, booleanToBoolean4_2.u)
    annotation (Line(points={{-39,50},{-4.4,50}}, color={217,67,180}));
  connect(booleanToBoolean4_2.y, b2)
    annotation (Line(points={{4.4,50},{110,50}}, color={162,29,33}));
  annotation (Icon(coordinateSystem(preserveAspectRatio=false)), Diagram(
        coordinateSystem(preserveAspectRatio=false)));
end CountInside_externals;
