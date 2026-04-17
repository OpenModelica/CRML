within CRML.Blocks.MathInteger;

block IntegerConstant "Generate a constant signal of type Integer"
        parameter Integer K = 1 "Constant output value";
        ETL.Connectors.IntegerOutput y annotation(
          extent = [100, -10; 120, 10],
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = K;
        annotation(
          Coordsys(extent = [-100, -100; 100, 100], grid = [2, 2], component = [20, 20]),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Line(points = {{-80, 0}, {70, 0}}, color = {244, 125, 35}, thickness = 0.5), Text(extent = [-150, 150; 150, 110], textString = "%name"), Text(extent = {{-47, 30}, {-27, 10}}, lineColor = {192, 192, 192}, textString = "K"), Polygon(points = {{-80, 92}, {-86, 70}, {-74, 70}, {-80, 92}}, lineColor = {192, 192, 192}, fillColor = {192, 192, 192}, fillPattern = FillPattern.Solid), Line(points = {{-80, 70}, {-80, -88}}, color = {192, 192, 192}), Text(extent = {{-74, 92}, {-56, 74}}, lineColor = {192, 192, 192}, textString = "y"), Line(points = {{-100, -70}, {58, -70}}, color = {192, 192, 192}), Polygon(points = {{80, -70}, {58, -64}, {58, -76}, {80, -70}}, lineColor = {192, 192, 192}, fillColor = {192, 192, 192}, fillPattern = FillPattern.Solid), Text(extent = {{44, -80}, {96, -92}}, lineColor = {192, 192, 192}, textString = "time")}),
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Polygon(points = {{-80, 90}, {-86, 68}, {-74, 68}, {-80, 90}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-176, 22}, {168, 56}}, lineColor = {0, 0, 0}, textString = "%K"), Line(points = {{-80, 0}, {80, 0}}, color = {244, 125, 35}), Line(points = {{-80, 68}, {-80, -80}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {82, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid)}),
          Window(x = 0.1, y = 0.22, width = 0.6, height = 0.6),
          Documentation(info = "<html>
<h4>Adapted from the Modelica.Blocks.Sources library </h4>
<h4>Version 1.0 </h4>
</html>"));
      end IntegerConstant;