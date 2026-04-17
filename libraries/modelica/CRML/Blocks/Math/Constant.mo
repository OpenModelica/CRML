within CRML.Blocks.Math;

block Constant "Generate constant signal of type Real"
        parameter Real k = 1 "Constant output value";
        ETL.Connectors.RealOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = k;
        annotation(
          defaultComponentName = "const",
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Polygon(points = {{-80, 90}, {-86, 68}, {-74, 68}, {-80, 90}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-80, 68}, {-80, -80}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {82, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-80, 0}, {80, 0}}, color = {0, 0, 0}), Text(extent = {{-176, 22}, {168, 56}}, lineColor = {0, 0, 0}, textString = "%k"), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Polygon(points = {{-80, 90}, {-86, 68}, {-74, 68}, {-80, 90}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-80, 68}, {-80, -80}}, color = {95, 95, 95}), Line(points = {{-80, 0}, {80, 0}}, color = {28, 108, 200}, thickness = 0.5), Line(points = {{-90, -70}, {82, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-83, 92}, {-30, 74}}, lineColor = {0, 0, 0}, textString = "y"), Text(extent = {{70, -80}, {94, -100}}, lineColor = {0, 0, 0}, textString = "time"), Text(extent = {{-101, 8}, {-81, -12}}, lineColor = {0, 0, 0}, textString = "k")}),
          Documentation(info = "<html>
<p>
The Real output y is a constant signal:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/Constant.png\"
     alt=\"Constant.png\">
</p>
</html>"));
      end Constant;