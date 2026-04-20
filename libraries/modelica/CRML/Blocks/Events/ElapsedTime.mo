within CRML.Blocks.Events;

block ElapsedTime "Elapsed time u2 - u1 time between events u1 and u2"
      protected
        Real t1(start = 0, fixed = true);
        Real t2(start = 0, fixed = true);
        Boolean x1(start = false, fixed = true);
        Boolean x2(start = false, fixed = true);
      public
        ETL.Connectors.BooleanInput u1 annotation(
          Placement(transformation(extent = {{-120, 70}, {-100, 90}}), iconTransformation(extent = {{-120, 70}, {-100, 90}})));
        ETL.Connectors.RealOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}}), iconTransformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanInput u2 annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
        ETL.Connectors.BooleanOutput isUndecided "isUndecided = false if both events have occurred on u1 and u2, else true" annotation(
          Placement(transformation(extent = {{100, -90}, {120, -70}})));
        ETL.Connectors.BooleanInput reset annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      algorithm
        when reset then
          x1 := false;
          x2 := false;
        end when;
        when u1 then
          x1 := true;
          t1 := time;
        end when;
        when u2 then
          x2 := true;
          t2 := time;
        end when;
        isUndecided := not (x1 and x2);
        y := t2 - t1;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-80, -58}, {-4, -58}, {-4, -58}, {38, -58}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 32}, {-20, 32}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-62, 76}, {-62, 32}}, color = {217, 67, 180}), Line(points = {{24, -14}, {24, -58}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-80, -58}, {-4, -58}, {-4, -58}, {38, -58}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 32}, {-20, 32}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-62, 76}, {-62, 32}}, color = {217, 67, 180}), Line(points = {{24, -14}, {24, -58}}, color = {0, 0, 0})}));
      end ElapsedTime;