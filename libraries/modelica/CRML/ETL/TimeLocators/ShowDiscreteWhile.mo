within CRML.ETL.TimeLocators;

block ShowDiscreteWhile "Visualizer of a discrete time while"
        Connectors.WhileInput tl annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
        CRML.Blocks.Events.ClockEvent eventClock annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}})));
        CRML.Blocks.Events.ClockToBoolean clockToBoolean annotation(
          Placement(transformation(extent = {{40, -10}, {60, 10}})));
        Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        Attributes.WhileTimePeriod timePeriod annotation(
          Placement(transformation(extent = {{-60, -10}, {-40, 10}})));
      equation
        connect(clockToBoolean.u, eventClock.y) annotation(
          Line(points = {{38, 0}, {11, 0}}, color = {175, 175, 175}, pattern = LinePattern.Dot, thickness = 0.5));
        connect(clockToBoolean.y, y) annotation(
          Line(points = {{62, 0}, {110, 0}}, color = {217, 67, 180}));
        connect(tl, timePeriod.tl) annotation(
          Line(points = {{0, 100}, {0, 40}, {-50, 40}, {-50, 10}}, color = {0, 0, 255}));
        connect(timePeriod.y, eventClock.u) annotation(
          Line(points = {{-39, 0}, {-11, 0}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5, fillColor = {170, 213, 255}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(lineColor = {64, 64, 64}, fillColor = {170, 213, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Sphere, extent = {{-40, -42}, {40, 40}}), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid)}),
          Diagram(coordinateSystem(initialScale = 0.04)),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>ShowDiscreteTimeWhile</b>(<b>tl</b> = discrete_time_while); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p>Displays the time periods of a discrete <span style=\"font-family: MS Shell Dlg 2;\"><a href=\"modelica://CRML.ETL.TimeLocators.While\">While</a> block in the form of clock ticks inside the time periods of the time locator.</span></p>
</html>"));
      end ShowDiscreteWhile;