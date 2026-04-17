within CRML.Requirements;

block Ensure
    protected
      parameter Integer N = CRML.ETL.Types.nMaxOverlap;
    public
      ETL.Connectors.Boolean4Input u "Boolean4 condition" annotation(
        Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
      ETL.Connectors.TimeLocatorInput[N] tl annotation(
        Placement(transformation(extent = {{-10, 90}, {10, 110}}), iconTransformation(extent = {{-10, 90}, {10, 110}})));
      ETL.Connectors.Boolean4Output y "Result of the comparison" annotation(
        Placement(transformation(extent = {{100, -10}, {120, 10}})));
      CheckCountEqual checkCountEqual(threshold = 0) annotation(
        Placement(transformation(extent = {{-10, -10}, {10, 10}})));
      CheckAnytime checkAnyTime annotation(
        Placement(transformation(extent = {{-50, -40}, {-30, -20}})));
      Blocks.Logical4.And4 and4_1 annotation(
        Placement(transformation(extent = {{60, -10}, {80, 10}})));
      Blocks.Logical4.Not4 not4_1 annotation(
        Placement(transformation(extent = {{-68, -10}, {-48, 10}})));
    equation
      connect(tl, checkCountEqual.tl) annotation(
        Line(points = {{0, 100}, {0, 10}}, color = {0, 0, 255}));
      connect(u, checkAnyTime.u) annotation(
        Line(points = {{-110, 0}, {-80, 0}, {-80, -30}, {-51, -30}}, color = {162, 29, 33}));
      connect(tl, checkAnyTime.tl) annotation(
        Line(points = {{0, 100}, {0, 60}, {-40, 60}, {-40, -20}, {-40, -20}}, color = {0, 0, 255}));
      connect(checkCountEqual.y, and4_1.u1) annotation(
        Line(points = {{11, 0}, {40, 0}, {40, 8}, {59, 8}}, color = {162, 29, 33}));
      connect(checkAnyTime.y, and4_1.u2) annotation(
        Line(points = {{-29, -30}, {40, -30}, {40, -8}, {59, -8}}, color = {162, 29, 33}));
      connect(and4_1.y, y) annotation(
        Line(points = {{81, 0}, {90, 0}, {90, 0}, {110, 0}}, color = {162, 29, 33}));
      connect(u, not4_1.u) annotation(
        Line(points = {{-110, 0}, {-69, 0}}, color = {162, 29, 33}));
      connect(not4_1.y, checkCountEqual.u) annotation(
        Line(points = {{-47, 0}, {-11, 0}}, color = {162, 29, 33}));
      //         Text(
      //           extent={{-74,32},{74,-36}},
      //           lineColor={0,0,0},
      //           fillColor={28,108,200},
      //           fillPattern=FillPattern.Solid,
      //           textString=boxName),
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {162, 29, 33}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Rectangle(extent = {{-78, 80}, {82, -80}}, lineColor = {175, 175, 175}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid), Text(extent = {{-68, 72}, {72, 44}}, lineColor = {28, 108, 200}, textString = "Ensure"), Line(points = {{-78, -62}, {-50, -62}, {-50, -18}, {60, -18}, {60, -64}, {82, -64}}, color = {0, 0, 0}), Text(extent = {{-18, 58}, {28, -4}}, lineColor = {0, 0, 0}, fontName = "Symbol", textString = "")}),
        Diagram(coordinateSystem(preserveAspectRatio = false)),
        Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>Ensure</b>(<b>u</b> = condition, <b>tl</b> = time_period); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Each instance of this block creates a requirement that evaluates whether the condition <b>u</b> is true all along the time period <b>tl</b> (which can be a continuous or discrete time period). The condition is a <a href=\"modelica://ETL.Types.Boolean4\">Boolean4</a> that takes its values in the { true, false, undecided, undefined } set. The value of <b>y</b> is always undecided within the time period <b>tl</b>, and is true or false at the end of <b>tl</b>.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The difference with the <a href=\"modelica://CRML.Requirements.CheckAnytime\">CheckAnyTime</a> block is the following: <b>Ensure</b> is evaluated all along time period <b>tl</b> and the sole decision event is the end of <b>tl</b>. Therefore, the value of <b>y</b> can never be true inside <b>tl</b>, but only at the end of <b>tl</b>.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">To create continuous time locators, refer to the <a href=\"modelica://ETL.TimeLocators.ContinuousTimeLocator\">ContinuousTimeLocator</a> block. To create discrete time locators, refer to the <a href=\"modelica://ETL.TimeLocators.DiscreteTimeLocator\">DiscreteTimeLocator</a> block.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The value of a requirement is a <a href=\"modelica://ETL.Types.Boolean4\">Boolean4</a> that can be used as input of another Ensure block. It is therefore possible to express requirements on requirements.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Requirements can be combined using <a href=\"modelica://ETL.Types.Boolean4\">Boolean4</a> operators, refer to the <a href=\"modelica://ETL.Blocks.Logical4\">Logical4</a> package.</span></p>
<p>The condition <b>u</b> can be generated by converting Boolean signals to Boolean4 signals with the block <a href=\"modelica://ETL.Blocks.Logical.BooleanToBoolean4\">BooleanToBoolean4</a>, or by using the output y of another requirement block.</p>
<p><br><b><span style=\"font-family: MS Shell Dlg 2;\">Example</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">This block is demonstrated with the following <a href=\"modelica://ReqSysPro.Examples.TimeLocators.Continuous.After\">example</a>:</span></p>
</html>"));
    end Ensure;