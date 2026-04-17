within CRML.ETL.Requirements;

block CheckInteger
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
        parameter String boxName = condition[1].name;
        Connectors.Boolean4Input u "Boolean4 condition" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
        Connectors.TimeLocatorInput[N] tl annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}}), iconTransformation(extent = {{-10, 90}, {10, 110}})));
        Connectors.Boolean4Output y "Result of the comparison" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        CRML.Blocks.Logical4.And4_n and4(N = N) annotation(
          Placement(transformation(extent = {{70, -10}, {90, 10}})));
        Evaluator.Eval[N] eval annotation(
          Placement(transformation(extent = {{30, -10}, {50, 10}})));
        CRML.Blocks.Routing.Boolean4Replicator boolean4Replicator(nout = N) annotation(
          Placement(transformation(extent = {{-90, -10}, {-70, 10}})));
        Connectors.IntegerInput threshold annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}})));
        CRML.Blocks.Routing.IntegerReplicator integerReplicator(nout = N) annotation(
          Placement(transformation(extent = {{-90, -90}, {-70, -70}})));
        replaceable model Function = Functions.MathInteger.EventCounter annotation(
          Placement(transformation(extent = {{-40, 10}, {-20, 30}})),
          choices(choice(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter "Event counter")));
        Function[N] genericFunction annotation(
          Placement(transformation(extent = {{-50, -10}, {-30, 10}})));
        replaceable model Condition = Conditions.MathInteger.IntegerGreater annotation(
          Placement(transformation(extent = {{0, 10}, {20, 30}})),
          choices(choice(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreater ">"), choice(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreaterEqual ">="), choice(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerLower "<"), choice(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerLowerEqual "<="), choice(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerEqual "=="), choice(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerNotEqual "<>")));
        Condition[N] condition annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}})));
        CRML.Blocks.Logical4.BooleanToBoolean4[N] booleanToBoolean3_1 annotation(
          Placement(transformation(extent = {{16, 4}, {24, 12}})));
      equation
        connect(tl, eval.tl) annotation(
          Line(points = {{0, 100}, {0, 60}, {40, 60}, {40, 10}}, color = {0, 0, 255}));
        connect(u, boolean4Replicator.u) annotation(
          Line(points = {{-110, 0}, {-92, 0}}, color = {162, 29, 33}));
        connect(eval.y, and4.u) annotation(
          Line(points = {{51, 0}, {69, 0}}, color = {162, 29, 33}));
        connect(and4.y, y) annotation(
          Line(points = {{91, 0}, {110, 0}}, color = {162, 29, 33}));
        connect(threshold, integerReplicator.u) annotation(
          Line(points = {{-110, -80}, {-92, -80}}, color = {255, 127, 0}));
        connect(boolean4Replicator.y, genericFunction.u) annotation(
          Line(points = {{-69, 0}, {-51, 0}}, color = {162, 29, 33}));
        connect(tl, genericFunction.tl) annotation(
          Line(points = {{0, 100}, {0, 60}, {-40, 60}, {-40, 10}}, color = {0, 0, 255}));
        connect(genericFunction.y, condition.u) annotation(
          Line(points = {{-29, 0}, {-20, 0}, {-20, 8}, {-11, 8}}, color = {255, 127, 0}));
        connect(integerReplicator.y, condition.threshold) annotation(
          Line(points = {{-69, -80}, {-20, -80}, {-20, -8}, {-11, -8}}, color = {255, 127, 0}));
        connect(condition.y, eval.u) annotation(
          Line(points = {{11, 0}, {29, 0}}, color = {162, 29, 33}));
        connect(genericFunction.ft, condition.ft) annotation(
          Line(points = {{-40, -11}, {-40, -20}, {0, -20}, {0, -11}}, color = {102, 44, 145}));
        connect(condition.decisionEvent, booleanToBoolean3_1.u) annotation(
          Line(points = {{11, 8}, {15.6, 8}}, color = {217, 67, 180}));
        connect(booleanToBoolean3_1.y, eval.a) annotation(
          Line(points = {{24.4, 8}, {29, 8}}, color = {162, 29, 33}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {244, 125, 35}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Rectangle(extent = {{-78, 80}, {82, -80}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-74, 32}, {74, -36}}, lineColor = {0, 0, 0}, fillColor = {28, 108, 200}, fillPattern = FillPattern.Solid, textString = boxName), Text(extent = {{-70, 72}, {70, 44}}, lineColor = {28, 108, 200}, textString = "Check"), Text(extent = {{-70, 18}, {70, -10}}, lineColor = {28, 108, 200}, textString = "integer")}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>CheckInteger </b>(<b>u</b> = condition, <b>tl</b> = time_period, <b>func</b> = IntegerFunction(<b>threshold</b> = integer_threshold, <b>tl </b>= tl)); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p>The purpose of this block is to generalize the <a href=\"modelica://CRML.ETL.Requirements.Check\">Check</a> block in order to evaluate requirements that take integer functions as arguments such as:</p>
<p style=\"margin-left: 30px;\">within <b>tl</b> ensure count <b>u</b> &lt; <b>threshold</b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">which means</span></p>
<p style=\"margin-left: 30px;\"><span style=\"font-family: MS Shell Dlg 2;\">at the end time locator <b>tl</b>, ensure that the number of events on <b>u</b> is less than <b>threshold</b>.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">In this example, <b>func</b> is the function count that counts the number of events on <b>u</b>, i.e. the number of occurences when condition becomes true (is satisfied) and compares it to <b>threshold</b>.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Function <b>func</b> can be chosen from the dialog box among predefined integer functions of the <a href=\"modelica://CRML.ETL.Requirements.Functions.MathInteger\">Functions</a> package. Currently, the only implemented integer function is the <a href=\"modelica://CRML.ETL.Requirements.Functions.MathInteger.EventCounter\">EventCounter</a>.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The comparator to the <b>threshold</b> (e.g. &gt;, &lt;, etc.) can be chosen from the dialog box among the predefined integer comparators of the <a href=\"modelica://CRML.ETL.Requirements.Conditions.MathInteger\">Conditions</a> package.</span></p>
<p><br><b><span style=\"font-family: MS Shell Dlg 2;\">Example</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">This block is demonstrated with the following <a href=\"modelica://ReqSysPro.Examples.TimeLocators.Continuous.After\">example</a>:</span></p>
</html>"));
      end CheckInteger;