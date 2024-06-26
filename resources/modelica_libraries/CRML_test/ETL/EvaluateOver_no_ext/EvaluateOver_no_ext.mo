within CRML_test.ETL.EvaluateOver_no_ext;
model EvaluateOver_no_ext
  import CRML_test;

protected
  parameter Integer N=CRML.ETL.Types.nMaxOverlap;

public
  CRML.Blocks.Logical.BooleanTable phi1(
    y0=false,
    option_width=false,
    instant={2,3.5})
    annotation (Placement(transformation(extent={{-60,40},{-40,60}})));
  CRML.Blocks.Logical.BooleanTable b1(
    y0=false,
    option_width=false,
    instant={2.5,5})
    annotation (Placement(transformation(extent={{-60,-60},{-40,-40}})));
  CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1
    annotation (Placement(transformation(extent={{-20,46},{-12,54}})));
  CRML.Blocks.Logical4.And4 and4_1
    annotation (Placement(transformation(extent={{0,32},{20,52}})));
  CRML.Blocks.Logical4.Boolean4Constant boolean4Constant3(K=CRML.ETL.Types.Boolean4.undecided)
    annotation (Placement(transformation(extent={{-34,20},{-14,40}})));
  CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2
    annotation (Placement(transformation(extent={{-4,-54},{4,-46}})));
  CRML.TimeLocators.Continuous.During during
    annotation (Placement(transformation(extent={{40,-60},{60,-40}})));
  CRML.ETL.Connectors.Boolean4Output b_evaluate_over
    annotation (Placement(transformation(extent={{316,-10},{336,10}})));
  CRML.ETL.Requirements.EvaluateOver evaluateOver
    annotation (Placement(transformation(extent={{160,-10},{180,10}})));
equation
  connect(phi1.y, booleanToBoolean4_1.u)
    annotation (Line(points={{-39,50},{-20.4,50}}, color={217,67,180}));
  connect(booleanToBoolean4_1.y, and4_1.u1)
    annotation (Line(points={{-11.6,50},{-1,50}}, color={162,29,33}));
  connect(boolean4Constant3.y, and4_1.u2) annotation (Line(points={{-13,30},{-6,
          30},{-6,34},{-1,34}}, color={162,29,33}));
  connect(b1.y, booleanToBoolean4_2.u)
    annotation (Line(points={{-39,-50},{-4.4,-50}}, color={217,67,180}));
  connect(booleanToBoolean4_2.y, during.u)
    annotation (Line(points={{4.4,-50},{39,-50}}, color={162,29,33}));
  connect(and4_1.y, evaluateOver.u) annotation (Line(points={{21,42},{78,42},{
          78,0},{159,0}}, color={162,29,33}));
  connect(during.y[1], evaluateOver.tl) annotation (Line(points={{50,-60},{50,
          -80},{140,-80},{140,20},{170,20},{170,10}}, color={0,0,255}));
  connect(evaluateOver.y, b_evaluate_over)
    annotation (Line(points={{181,0},{326,0}}, color={162,29,33}));
  annotation (Icon(coordinateSystem(preserveAspectRatio=false,
        extent={{-100,-100},{320,100}},
        initialScale=0.1)),                                      Diagram(
        coordinateSystem(preserveAspectRatio=false,
        extent={{-100,-100},{320,100}},
        initialScale=0.1)));
end EvaluateOver_no_ext;
