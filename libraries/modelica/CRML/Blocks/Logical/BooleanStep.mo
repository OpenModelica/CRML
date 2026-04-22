within CRML.Blocks.Logical;

block BooleanStep "Generate step signal of type Boolean"
        parameter CRML.Units.SI.Time startTime = 0 "Time instant of step start";
        ETL.Connectors.BooleanOutput y annotation(
          extent = [100, -10; 120, 10],
          Placement(transformation(extent = {{100, -10}, {120, 10}}, rotation = 0)));
      equation
        y = (if time < startTime then false else true);
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Polygon(points = {{-80, 88}, {-88, 66}, {-72, 66}, {-80, 88}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 66}, {-80, -82}}, color = {255, 0, 255}), Line(points = {{-90, -70}, {72, -70}}, color = {255, 0, 255}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{71, 7}, {85, -7}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(visible = true, points = {{-80, -70}, {0, -70}, {0, 50}, {80, 50}}), Text(extent = {{-150, -100}, {150, -70}}, lineColor = {0, 0, 0}, textString = "%startTime"), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Polygon(points = {{-70, 92}, {-76, 70}, {-64, 70}, {-70, 92}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-64, 92}, {-46, 74}}, lineColor = {0, 0, 0}, textString = "y"), Text(extent = {{-66, 62}, {-22, 48}}, lineColor = {0, 0, 0}, textString = "true"), Polygon(points = {{2, 50}, {-80, 50}, {2, 50}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-70, 70}, {-70, -88}}, color = {95, 95, 95}), Text(extent = {{-58, -56}, {-26, -70}}, lineColor = {0, 0, 0}, textString = "false"), Text(extent = {{-15, -74}, {20, -82}}, lineColor = {0, 0, 0}, textString = "startTime"), Line(points = {{-90, -70}, {68, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{54, -80}, {106, -92}}, lineColor = {0, 0, 0}, textString = "time"), Line(points = {{-80, -70}, {0, -70}, {0, 50}, {80, 50}}, color = {0, 0, 255}, thickness = 0.5)}),
          Documentation(info = "<html>
<p>
The Boolean output y is a step signal:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/BooleanStep.png\"
     alt=\"BooleanStep.png\">
</p>
</html>"));
      end BooleanStep;