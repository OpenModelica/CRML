within CRML.TimeLocators.Discrete;

block FromFor
        import CRML.ETL.Types.Boolean4;
      protected
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
        ETL.Connectors.WhileOutput tl1 = tl;
      public
        ETL.Connectors.Boolean4Input u "Opening event" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.WhileInput tl "Master time period" annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
      public
        ETL.Connectors.TimeLocatorOutput[N] y(timePeriod(each fixed = true, each start = false)) "Vector of time periods" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100}), iconTransformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100})));
        ETL.TimeLocators.Periods periods(rightBoundaryIncluded = true, durationSpecified = true, discreteClockSpecified = true, leftBoundaryIncluded = true) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}})));
        ETL.Connectors.Boolean4Input clock annotation(
          Placement(transformation(extent = {{-120, 70}, {-100, 90}}), iconTransformation(extent = {{-120, 70}, {-100, 90}})));
        ETL.Connectors.IntegerInput duration annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
      equation
        if (cardinality(tl) == 0) then
          tl.timePeriod = true;
          tl.clock = Boolean4.true4;
          tl.isLeftBoundaryIncluded = true;
          tl.isRightBoundaryIncluded = true;
        end if;
        connect(u, periods.u1) annotation(
          Line(points = {{-110, 0}, {-11, 0}}, color = {162, 29, 33}));
        connect(tl1, periods.tl) annotation(
          Line(points = {{0, 100}, {0, 10}}, color = {0, 0, 255}));
        connect(periods.y, y) annotation(
          Line(points = {{0, -10}, {0, -100}}, color = {0, 0, 255}));
        connect(clock, periods.clock) annotation(
          Line(points = {{-110, 80}, {-60, 80}, {-60, 8}, {-11, 8}}, color = {162, 29, 33}));
        connect(duration, periods.discreteDuration) annotation(
          Line(points = {{-110, -80}, {-60, -80}, {-60, -8}, {-11, -8}}, color = {255, 127, 0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {85, 170, 255}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Rectangle(extent = {{-70, 30}, {70, -52}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-50, -34}, {-50, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-48, -12}, {52, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{52, -12}, {52, -34}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-62, -34}, {58, -34}}, color = {175, 175, 175}), Line(points = {{-50, -12}, {-50, 10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Rectangle(extent = {{22, 2}, {52, -12}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Line(points = {{-50, 10}, {22, 10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, 10}, {22, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, -12}, {20, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, -12}, {24, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, -12}, {-52, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, -12}, {-48, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, 30}, {-70, 30}, {-70, -52}, {-50, -52}}, color = {0, 0, 0}, visible = periods.leftBoundaryIncluded), Line(points = {{50, 30}, {70, 30}, {70, -52}, {50, -52}}, color = {0, 0, 0}, visible = periods.rightBoundaryIncluded), Text(extent = {{-28, -10}, {22, -34}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u"), Text(extent = {{-78, 86}, {76, 38}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "from for")}),
          Diagram(coordinateSystem(preserveAspectRatio = false)));
      end FromFor;