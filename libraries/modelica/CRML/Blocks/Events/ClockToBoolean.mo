within CRML.Blocks.Events;

block ClockToBoolean "Transform clock to clocked Boolean (output is true if clock is active)"
        ETL.Connectors.BooleanOutput y "Clocked Boolean output signal" annotation(
          Placement(transformation(extent = {{100, -20}, {140, 20}})));
        ETL.Connectors.ClockInput u annotation(
          Placement(transformation(extent = {{-140, -20}, {-100, 20}})));
      equation
        when u then
          y = true;
        end when;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-250, 170}, {250, 110}}, textString = "%name", lineColor = {0, 0, 255}), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-74, -82}, {-30, -82}, {-30, -38}, {-30, -38}, {-30, -82}, {72, -82}}, color = {95, 95, 95}, pattern = LinePattern.Dot), Ellipse(extent = {{-40, -28}, {-20, -48}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-72, 34}, {-28, 34}, {-28, 78}, {-28, 78}, {-28, 34}, {74, 34}}, color = {255, 0, 255}, pattern = LinePattern.Dot), Ellipse(extent = {{-38, 88}, {-18, 68}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid)}),
          Diagram(graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-250, 170}, {250, 110}}, textString = "%name", lineColor = {0, 0, 255}), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-74, -82}, {-30, -82}, {-30, -38}, {-30, -38}, {-30, -82}, {72, -82}}, color = {95, 95, 95}, pattern = LinePattern.Dot), Ellipse(extent = {{-40, -28}, {-20, -48}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-72, 34}, {-28, 34}, {-28, 78}, {-28, 78}, {-28, 34}, {74, 34}}, color = {255, 0, 255}, pattern = LinePattern.Dot), Ellipse(extent = {{-38, 88}, {-18, 68}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid)}));
      end ClockToBoolean;