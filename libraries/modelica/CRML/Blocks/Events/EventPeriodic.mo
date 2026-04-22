within CRML.Blocks.Events;

block EventPeriodic "Generate periodic events"
        parameter Real period = 1 "Time for one period (s)";
        parameter Real startTime = 0 "Time instant of first sample trigger";
      protected
        Real tick;
        Boolean btick(start = false, fixed = true) = (time > tick);
      public
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanInput reset annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
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
        y = edge(btick);
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Polygon(points = {{-80, 88}, {-88, 66}, {-72, 66}, {-80, 88}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 66}, {-80, -82}}, color = {255, 0, 255}), Line(points = {{-90, -70}, {72, -70}}, color = {255, 0, 255}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-60, -70}, {-60, 40}}), Line(points = {{-20, -70}, {-20, 40}}), Line(points = {{20, -70}, {20, 40}}), Line(points = {{60, -70}, {60, 40}}), Text(extent = {{-172, 56}, {172, 90}}, lineColor = {0, 0, 0}, textString = "%period"), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid)}),
          Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Polygon(points = {{-70, 82}, {-76, 60}, {-64, 60}, {-70, 82}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-70, 60}, {-70, -98}}, color = {95, 95, 95}), Line(points = {{-90, -80}, {68, -80}}, color = {95, 95, 95}), Polygon(points = {{90, -80}, {68, -74}, {68, -86}, {90, -80}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{54, -90}, {106, -102}}, lineColor = {0, 0, 0}, textString = "time"), Text(extent = {{-64, 82}, {-46, 64}}, lineColor = {0, 0, 0}, textString = "y"), Text(extent = {{-51, -82}, {-11, -91}}, lineColor = {0, 0, 0}, textString = "startTime"), Line(points = {{-30, 37}, {-30, 9}}, color = {95, 95, 95}), Line(points = {{0, 37}, {0, 8}}, color = {95, 95, 95}), Line(points = {{-30, 31}, {0, 31}}, color = {95, 95, 95}), Text(extent = {{-37, 51}, {9, 39}}, lineColor = {0, 0, 0}, textString = "period"), Line(points = {{-73, 9}, {-30, 9}}, color = {95, 95, 95}), Polygon(points = {{-30, 31}, {-21, 33}, {-21, 29}, {-30, 31}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{0, 31}, {-8, 33}, {-8, 29}, {0, 31}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-91, 13}, {-71, 3}}, lineColor = {0, 0, 0}, textString = "true"), Text(extent = {{-90, -69}, {-70, -78}}, lineColor = {0, 0, 0}, textString = "false"), Line(points = {{0, -80}, {0, 9}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{-30, -80}, {-30, 9}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{30, -80}, {30, 9}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{60, -80}, {60, 9}}, color = {0, 0, 255}, thickness = 0.5)}),
          Documentation(info = "<html>
<h4>Adapted from the Modelica.Blocks.Sources library </h4>
<h4>Version 1.0 </h4>
<p>The Boolean output y is a trigger signal where the output y is only <b>true</b> at sample times (defined by parameter <b>period</b>) and is otherwise <b>false</b>. </p>
<p><img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/SampleTrigger.png\" alt=\"SampleTrigger.png\"/> </p>
</html>"));
      end EventPeriodic;