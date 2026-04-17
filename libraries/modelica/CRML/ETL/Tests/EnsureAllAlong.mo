within CRML.ETL.Tests;

model EnsureAllAlong
        TimeLocators.Periods timePeriod(leftBoundaryIncluded = false, rightBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{0, 40}, {20, 60}})));
        CRML.Blocks.Logical.BooleanStep eventPeriodic(startTime = 0) annotation(
          Placement(transformation(extent = {{-80, -40}, {-60, -20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, -34}, {-36, -26}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false) annotation(
          Placement(transformation(extent = {{-80, 50}, {-60, 70}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {10}) annotation(
          Placement(transformation(extent = {{-80, 20}, {-60, 40}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerEqual) annotation(
          Placement(transformation(extent = {{0, -40}, {20, -20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-44, 46}, {-36, 54}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-44, 38}, {-36, 46}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 0) annotation(
          Placement(transformation(extent = {{-80, -80}, {-60, -60}})));
        CRML.Blocks.Logical4.Not4 not4_1 annotation(
          Placement(transformation(extent = {{-26, -36}, {-14, -24}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -30}, {-44.4, -30}}, color = {217, 67, 180}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{10, 40}, {10, -20}}, color = {0, 0, 255}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 60}, {-50, 60}, {-50, 50}, {-44.4, 50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-35.6, 50}, {-1, 50}}, color = {162, 29, 33}));
        connect(eventTable1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-59, 30}, {-50, 30}, {-50, 42}, {-44.4, 42}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_3.y, timePeriod.u2) annotation(
          Line(points = {{-35.6, 42}, {-1, 42}}, color = {162, 29, 33}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{-59, -70}, {-40, -70}, {-40, -38}, {-1, -38}}, color = {255, 127, 0}));
        connect(booleanToBoolean4_1.y, not4_1.u) annotation(
          Line(points = {{-35.6, -30}, {-26.6, -30}}, color = {162, 29, 33}));
        connect(not4_1.y, ensure.u) annotation(
          Line(points = {{-13.4, -30}, {-1, -30}}, color = {162, 29, 33}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end EnsureAllAlong;