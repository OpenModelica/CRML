within CRML.Blocks.Logical;

block BooleanConstant "Generate a constant signal of type Boolean"
        parameter Boolean K = true "Constant output value";
        ETL.Connectors.BooleanOutput y annotation(
          extent = [100, -10; 120, 10],
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = K;
        annotation(
          Coordsys(extent = [-100, -100; 100, 100], grid = [2, 2], component = [20, 20]),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Line(points = {{-80, 0}, {70, 0}}, color = {255, 0, 128}, thickness = 0.5), Text(extent = {{-47, 30}, {-27, 10}}, lineColor = {192, 192, 192}, textString = "K"), Text(extent = {{-106, 6}, {-86, -4}}, lineColor = {192, 192, 192}, textString = "true"), Text(extent = {{-108, -58}, {-82, -68}}, lineColor = {192, 192, 192}, textString = "false"), Polygon(points = {{-80, 92}, {-86, 70}, {-74, 70}, {-80, 92}}, lineColor = {192, 192, 192}, fillColor = {192, 192, 192}, fillPattern = FillPattern.Solid), Line(points = {{-80, 70}, {-80, -88}}, color = {192, 192, 192}), Text(extent = {{-74, 92}, {-56, 74}}, lineColor = {192, 192, 192}, textString = "y"), Line(points = {{-100, -70}, {58, -70}}, color = {192, 192, 192}), Polygon(points = {{80, -70}, {58, -64}, {58, -76}, {80, -70}}, lineColor = {192, 192, 192}, fillColor = {192, 192, 192}, fillPattern = FillPattern.Solid), Text(extent = {{44, -80}, {96, -92}}, lineColor = {192, 192, 192}, textString = "time")}),
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Polygon(points = {{-80, 88}, {-88, 66}, {-72, 66}, {-80, 88}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 66}, {-80, -82}}, color = {255, 0, 255}), Line(points = {{-90, -70}, {72, -70}}, color = {255, 0, 255}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{71, 7}, {85, -7}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, 0}, {80, 0}}), Text(extent = {{-176, 22}, {168, 56}}, lineColor = {0, 0, 0}, textString = "%K")}),
          Window(x = 0.1, y = 0.22, width = 0.6, height = 0.6),
          Documentation(info = "<html>
<h4>Adapted from the Modelica.Blocks.Sources library </h4>
<p><b>Version 1.0</b> </p>
</html>"));
      end BooleanConstant;