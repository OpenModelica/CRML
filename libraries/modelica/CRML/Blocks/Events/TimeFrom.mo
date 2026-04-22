within CRML.Blocks.Events;

block TimeFrom "Continuous physical time from an event"
        parameter Real initialValue = 0 "Output value before the first event has occurred";
      protected
        Real t0(start = 0, fixed = true);
        Boolean eventOccurred(start = false, fixed = true);
        outer CRML.TimeLocators.Continuous.Master master;
      public
        ETL.Connectors.BooleanInput u(start = false, fixed = true) "Event giving the time origin" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.RealOutput y "Delay since the time origin" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        if cardinality(u) == 0 then
          u = master.y.timePeriod;
        end if;
        when u then
          t0 = time;
          eventOccurred = true;
        end when;
        y = if eventOccurred then time - t0 else initialValue;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, initialScale = 0.06), graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}), Ellipse(extent = {{9, -10}, {-11, 10}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-1, 0}, {52, 50}}, color = {0, 0, 0}), Rectangle(extent = {{80, 6}, {100, -6}}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{-100, 6}, {-80, -6}}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, -120}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {80, 6}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {-6, 80}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {122, -48}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, 60}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {-52, -118}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Documentation(info = "<html>
<p>This component generates the continuous physical time elpased from the event given as input to the block.</p>
<p>The output is equal to initialValue if the event has never occurred. Otherwise, the output is the delay between the current time and the latest event on the input.</p>
<p>The <a href=\"modelica://CRML.TimeLocators.Continuous.Master\">Master</a> time locator can be used to generate the event instead of input u.</p>
</html>"),Diagram(graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}), Ellipse(extent = {{9, -10}, {-11, 10}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-1, 0}, {52, 50}}, color = {0, 0, 0}), Rectangle(extent = {{80, 6}, {100, -6}}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{-100, 6}, {-80, -6}}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, -120}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {80, 6}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {-6, 80}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {122, -48}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, 60}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {-52, -118}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}));
      end TimeFrom;