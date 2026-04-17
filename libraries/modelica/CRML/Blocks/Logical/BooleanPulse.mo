within CRML.Blocks.Logical;

block BooleanPulse "Generate pulse signal of type Boolean"
        parameter Real width = 0.5 "Width of pulse (s)";
        parameter Real period = 1 "Time for one period (s)";
        parameter Real startTime = 0 "Output = false for time < startTime";
      protected
        Real tick;
      public
        ETL.Connectors.BooleanInput reset "Reset signal" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      public
        ETL.Connectors.BooleanOutput y annotation(
          extent = [100, -10; 120, 10],
          Placement(transformation(extent = {{100, -10}, {120, 10}}, rotation = 0), iconTransformation(extent = {{100, -10}, {120, 10}})));
      initial equation
        if (cardinality(reset) == 0) then
          tick = startTime;
        else
          tick = Modelica.Constants.inf;
        end if;
      equation
        if (cardinality(reset) == 0) then
          reset = false;
        end if;
        when {time >= pre(tick) + period, reset} then
          tick = time;
        end when;
        y = (time > tick) and (time <= tick + width);
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Polygon(points = {{-80, 88}, {-88, 66}, {-72, 66}, {-80, 88}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 66}, {-80, -82}}, color = {255, 0, 255}), Line(points = {{-90, -70}, {72, -70}}, color = {255, 0, 255}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{71, 7}, {85, -7}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -70}, {-40, -70}, {-40, 44}, {0, 44}, {0, -70}, {40, -70}, {40, 44}, {79, 44}}), Text(extent = {{-172, 56}, {172, 90}}, lineColor = {0, 0, 0}, textString = "%width% / %period"), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-60, -74}, {-19, -82}}, lineColor = {0, 0, 0}, textString = "startTime"), Line(points = {{-78, -70}, {-40, -70}, {-40, 20}, {20, 20}, {20, -70}, {50, -70}, {50, 20}, {100, 20}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{-40, 61}, {-40, 21}}, color = {95, 95, 95}), Line(points = {{20, 44}, {20, 20}}, color = {95, 95, 95}), Line(points = {{50, 58}, {50, 20}}, color = {95, 95, 95}), Line(points = {{-40, 53}, {50, 53}}, color = {95, 95, 95}), Line(points = {{-40, 35}, {20, 35}}, color = {95, 95, 95}), Text(extent = {{-30, 65}, {16, 55}}, lineColor = {0, 0, 0}, textString = "period"), Text(extent = {{-33, 47}, {14, 37}}, lineColor = {0, 0, 0}, textString = "width"), Line(points = {{-70, 20}, {-41, 20}}, color = {95, 95, 95}), Polygon(points = {{-40, 35}, {-31, 37}, {-31, 33}, {-40, 35}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{20, 35}, {12, 37}, {12, 33}, {20, 35}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{-40, 53}, {-31, 55}, {-31, 51}, {-40, 53}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{50, 53}, {42, 55}, {42, 51}, {50, 53}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-95, 26}, {-66, 17}}, lineColor = {0, 0, 0}, textString = "true"), Text(extent = {{-96, -60}, {-75, -69}}, lineColor = {0, 0, 0}, textString = "false"), Polygon(points = {{-70, 92}, {-76, 70}, {-64, 70}, {-70, 92}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-70, 70}, {-70, -88}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {68, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid)}),
          Documentation(info = "<html>
<p>
The Boolean output y is a pulse signal:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/Pulse.png\"
     alt=\"Pulse.png\">
</p>
</html>"));
      end BooleanPulse;