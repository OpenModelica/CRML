within CRML.Blocks.Events;

block Before "Returns true if event on u1 occurs before event on u2"
      protected
        Real t1(start = 0, fixed = true);
        Real t2(start = 0, fixed = true);
        Boolean x1(start = false, fixed = true);
        Boolean x2(start = false, fixed = true);
      public
        ETL.Connectors.BooleanInput u1 annotation(
          Placement(transformation(extent = {{-120, 70}, {-100, 90}}), iconTransformation(extent = {{-120, 70}, {-100, 90}})));
        ETL.Connectors.BooleanInput u2 annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
        ETL.Connectors.BooleanOutput y "y = true if event on u1 occurs before event on u2" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanOutput isUndecided(start = true, fixed = true) "isUndecided = false if at least one  event has occurred on u1 or  u2, else true" annotation(
          Placement(transformation(extent = {{100, -90}, {120, -70}})));
        ETL.Connectors.BooleanInput reset annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
        ETL.Connectors.BooleanInput strictlyBefore "If true, event on u1 should occurr strictly before event on u2" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
      algorithm
        when reset then
          x1 := false;
          x2 := false;
          isUndecided := true;
        end when;
        when u1 then
          x1 := true;
          t1 := time;
          isUndecided := false;
        end when;
        when u2 then
          x2 := true;
          t2 := time;
        end when;
        if strictlyBefore then
          y := (x1 and x2 and (t2 - t1) > 0) or (x1 and not x2);
        else
          y := (x1 and x2 and (t2 - t1) >= 0) or (x1 and not x2);
        end if;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-80, -58}, {-4, -58}, {-4, -58}, {38, -58}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 32}, {-20, 32}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-62, 76}, {-62, 32}}, color = {217, 67, 180}), Line(points = {{24, -14}, {24, -58}}, color = {0, 0, 0}), Text(extent = {{-90, -40}, {60, 40}}, lineColor = {0, 0, 0}, textString = "<")}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-80, -58}, {-4, -58}, {-4, -58}, {38, -58}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 32}, {-20, 32}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-62, 76}, {-62, 32}}, color = {217, 67, 180}), Line(points = {{24, -14}, {24, -58}}, color = {0, 0, 0}), Text(extent = {{-90, -40}, {60, 40}}, lineColor = {0, 0, 0}, textString = "<")}));
      end Before;