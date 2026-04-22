within CRML.Blocks.MathInteger;

block Greater "Output y is true, if input u1 is greater than input u2"
        ETL.Connectors.IntegerInput u1 annotation(
          Placement(transformation(extent = {{-120, 50}, {-100, 70}})));
        ETL.Connectors.IntegerInput u2 annotation(
          Placement(transformation(extent = {{-120, -70}, {-100, -50}})));
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = u1 > u2;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {244, 125, 35}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {244, 125, 35}), Line(points = {{-54, 22}, {-8, 2}, {-54, -18}}, thickness = 0.5, color = {244, 125, 35}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Documentation(info = "<html>
<p>
The output is <b>true</b> if Real input u1 is greater than
Real input u2, otherwise the output is <b>false</b>.
</p>
</html>"),Diagram(graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {244, 125, 35}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {244, 125, 35}), Line(points = {{-54, 22}, {-8, 2}, {-54, -18}}, thickness = 0.5, color = {244, 125, 35}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}));
      end Greater;