within CRML.Blocks.MathInteger;

block IntegerRamp "Generate ramp signal"
        parameter Integer step = 1 "Height of steps";
        parameter Integer offset = 0 "Offset of output signal";
      public
        ETL.Connectors.IntegerOutput y(start = offset, fixed = true) annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanInput reset annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      equation
        if (cardinality(reset) == 0) then
          reset = false;
        end if;
        when reset then
          y = pre(y) + step;
        end when;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 68}, {-80, -80}}, color = {0, 0, 0}), Polygon(points = {{-80, 90}, {-88, 68}, {-72, 68}, {-80, 90}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Line(points = {{-90, -70}, {82, -70}}, color = {0, 0, 0}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Line(points = {{-80, -40}, {-40, -40}, {-40, -20}}, color = {244, 125, 35}), Text(extent = {{-158, 54}, {186, 88}}, lineColor = {244, 125, 35}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, textString = "%offset% / %step% "), Line(points = {{-40, -20}, {0, -20}}, color = {244, 125, 35}), Line(points = {{0, -20}, {0, 0}, {30, 0}}, color = {244, 125, 35}), Line(points = {{30, 0}, {30, 20}, {60, 20}}, color = {244, 125, 35}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Polygon(points = {{-80, 90}, {-86, 68}, {-74, 68}, {-80, 90}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-80, 68}, {-80, -80}}, color = {95, 95, 95}), Line(points = {{-80, -20}, {-20, -20}, {-20, 20}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{-90, -70}, {82, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{-40, -20}, {-42, -30}, {-38, -30}, {-40, -20}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-40, -20}, {-40, -70}}, color = {95, 95, 95}), Polygon(points = {{-40, -70}, {-42, -60}, {-38, -60}, {-40, -70}, {-40, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-72, -39}, {-34, -50}}, lineColor = {0, 0, 0}, textString = "offset"), Text(extent = {{-78, 92}, {-37, 72}}, lineColor = {0, 0, 0}, textString = "y"), Text(extent = {{70, -80}, {94, -91}}, lineColor = {0, 0, 0}, textString = "time"), Line(points = {{-20, -20}, {-20, -70}}, color = {95, 95, 95}), Line(points = {{-20, 20}, {20, 20}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{-12, 20}, {-12, -20}}, color = {95, 95, 95}), Polygon(points = {{-12, 20}, {-14, 10}, {-10, 10}, {-12, 20}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{-12, -20}, {-14, -10}, {-10, -10}, {-12, -20}, {-12, -20}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-9, 7}, {20, -6}}, lineColor = {0, 0, 0}, textString = "step"), Line(points = {{20, 60}, {60, 60}}, color = {0, 0, 255}, thickness = 0.5), Polygon(points = {{28, 60}, {26, 50}, {30, 50}, {28, 60}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{28, 60}, {28, 20}}, color = {95, 95, 95}), Polygon(points = {{28, 20}, {26, 30}, {30, 30}, {28, 20}, {28, 20}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{31, 47}, {60, 34}}, lineColor = {0, 0, 0}, textString = "step"), Line(points = {{20, 60}, {20, 20}}, color = {28, 108, 200}, thickness = 0.5), Text(extent = {{-17, -83}, {12, -96}}, lineColor = {0, 0, 0}, textString = "event"), Polygon(points = {{-20, -70}, {-22, -80}, {-18, -80}, {-20, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-20, -70}, {-20, -100}}, color = {95, 95, 95}), Text(extent = {{23, -83}, {52, -96}}, lineColor = {0, 0, 0}, textString = "event"), Polygon(points = {{20, -70}, {18, -80}, {22, -80}, {20, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{20, -70}, {20, -100}}, color = {95, 95, 95}), Line(points = {{20, 20}, {20, -70}}, color = {95, 95, 95})}),
          Documentation(info = "<html>
<p>
The Real output y is a ramp signal:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/Ramp.png\"
     alt=\"Ramp.png\">
</p>

<p>
If parameter duration is set to 0.0, the limiting case of a Step signal is achieved.
</p>
</html>"));
      end IntegerRamp;