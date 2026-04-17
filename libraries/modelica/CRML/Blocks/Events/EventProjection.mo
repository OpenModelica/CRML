within CRML.Blocks.Events;

block EventProjection "Event projection on a clock"
      protected
        Boolean x(start = false, fixed = true);
        // Input event delayed until the next clock tick
      public
        ETL.Connectors.BooleanInput clock annotation(
          Placement(transformation(extent = {{-120, 70}, {-100, 90}})));
        ETL.Connectors.BooleanInput u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      algorithm
        /* Maintain event u until the next clock tick */
        when u then
          x := true;
        end when;
        y := false;
        /* Output event on clock tick */
        when clock then
          if x then
            y := true;
            x := false;
          end if;
        end when;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -58}, {-4, -58}, {-4, -58}, {38, -58}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 32}, {-20, 32}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Line(points = {{-22, 76}, {-22, 32}}, color = {217, 67, 180}), Line(points = {{20, 76}, {20, 32}}, color = {217, 67, 180}), Line(points = {{-38, -14}, {-38, -58}}, color = {0, 0, 0}), Line(points = {{10, -14}, {10, -58}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -58}, {-4, -58}, {-4, -58}, {38, -58}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 32}, {-20, 32}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Line(points = {{-22, 76}, {-22, 32}}, color = {217, 67, 180}), Line(points = {{20, 76}, {20, 32}}, color = {217, 67, 180}), Line(points = {{-38, -14}, {-38, -58}}, color = {0, 0, 0}), Line(points = {{10, -14}, {10, -58}}, color = {0, 0, 0})}));
      end EventProjection;