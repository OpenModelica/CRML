within CRML.Blocks.MathInteger;

block Switch "Switch between two Real signals"
        ETL.Connectors.IntegerInput u1 annotation(
          Placement(transformation(extent = {{-120, 70}, {-100, 90}})));
        ETL.Connectors.IntegerInput u2 annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}})));
        ETL.Connectors.BooleanInput u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.IntegerOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = if u then u1 else u2;
        annotation(
          defaultComponentName = "switch1",
          Documentation(info = "<html>
<p>The Logical.Switch switches, depending on the
logical connector u2 (the middle connector)
between the two possible input signals
u1 (upper connector) and u3 (lower connector).</p>
<p>If u2 is <b>true</b>, the output signal y is set equal to
u1, else it is set equal to u3.</p>
</html>"),Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, initialScale = 0.04), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{12.0, 0.0}, {100.0, 0.0}}, color = {255, 128, 0}), Line(points = {{-100.0, 0.0}, {-40.0, 0.0}}, color = {255, 0, 255}), Line(points = {{-100.0, -80.0}, {-40.0, -80.0}, {-40.0, -80.0}}, color = {255, 128, 0}), Line(points = {{-40.0, 12.0}, {-40.0, -12.0}}, color = {255, 0, 255}), Line(points = {{-100.0, 80.0}, {-38.0, 80.0}}, color = {255, 128, 0}), Line(points = {{-38, 80}, {8, 4}}, color = {255, 128, 0}, thickness = 1), Ellipse(lineColor = {0, 0, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, extent = {{2.0, -8.0}, {18.0, 8.0}}, fillColor = {255, 128, 0}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{12, 0}, {100, 0}}, color = {255, 128, 0}), Line(points = {{-100, 0}, {-40, 0}}, color = {255, 0, 255}), Line(points = {{-100, -80}, {-40, -80}, {-40, -80}}, color = {255, 128, 0}), Line(points = {{-40, 12}, {-40, -12}}, color = {255, 0, 255}), Line(points = {{-100, 80}, {-38, 80}}, color = {255, 128, 0}), Line(points = {{-38, 80}, {8, 4}}, color = {255, 128, 0}, thickness = 1), Ellipse(lineColor = {0, 0, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, extent = {{2, -8}, {18, 8}}, fillColor = {255, 128, 0}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}));
      end Switch;