within CRML.ETL.Tests;

model EnsureAtEnd
        import CRML;
        TimeLocators.Periods timePeriod(leftBoundaryIncluded = false, rightBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{-20, 40}, {0, 60}})));
        CRML.Blocks.Logical.BooleanStep eventPeriodic(startTime = 5) annotation(
          Placement(transformation(extent = {{-80, -40}, {-60, -20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, -34}, {-36, -26}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false) annotation(
          Placement(transformation(extent = {{-80, 50}, {-60, 70}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {10}) annotation(
          Placement(transformation(extent = {{-80, 20}, {-60, 40}})));
        CRML.ETL.Requirements.CheckAtEnd ensure annotation(
          Placement(transformation(extent = {{-20, -40}, {0, -20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-44, 46}, {-36, 54}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-44, 38}, {-36, 46}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -30}, {-44.4, -30}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-35.6, -30}, {-21, -30}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{-10, 40}, {-10, -20}, {-10, -20}}, color = {0, 0, 255}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 60}, {-50, 60}, {-50, 50}, {-44.4, 50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-35.6, 50}, {-21, 50}}, color = {162, 29, 33}));
        connect(eventTable1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-59, 30}, {-50, 30}, {-50, 42}, {-44.4, 42}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_3.y, timePeriod.u2) annotation(
          Line(points = {{-35.6, 42}, {-21, 42}}, color = {162, 29, 33}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end EnsureAtEnd;