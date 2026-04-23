within CRML.ETL.Tests;

model When2
        TimeLocators.Periods timePeriod(durationSpecified = false, leftBoundaryIncluded = true) annotation(
          Placement(transformation(extent = {{-20, 40}, {0, 60}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic annotation(
          Placement(transformation(extent = {{-80, -40}, {-60, -20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, -34}, {-36, -26}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1, 3, 6}) annotation(
          Placement(transformation(extent = {{-80, 40}, {-60, 60}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 5) annotation(
          Placement(transformation(extent = {{-80, -80}, {-60, -60}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreaterEqual) annotation(
          Placement(transformation(extent = {{-20, -40}, {0, -20}})));
        TimeLocators.While While annotation(
          Placement(transformation(extent = {{-20, 80}, {0, 100}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(width = 8, period = 12, startTime = 1) annotation(
          Placement(transformation(extent = {{-80, 80}, {-60, 100}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-44, 46}, {-36, 54}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-44, 86}, {-36, 94}})));
        CRML.Blocks.Math.Constant const(k = 0) annotation(
          Placement(transformation(extent = {{-80, -10}, {-60, 10}})));
        TimeLocators.ShowDiscretePeriods showDiscreteTimeLocator annotation(
          Placement(transformation(extent = {{6, 16}, {14, 24}})));
        TimeLocators.While While1 annotation(
          Placement(transformation(extent = {{0, -90}, {20, -70}})));
        TimeLocators.ShowDiscreteWhile showDiscreteTimeWhile annotation(
          Placement(transformation(extent = {{44, -96}, {52, -88}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -30}, {-44.4, -30}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-35.6, -30}, {-21, -30}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{-10, 40}, {-10, -20}}, color = {0, 0, 255}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{-59, -70}, {-40, -70}, {-40, -38}, {-21, -38}}, color = {255, 127, 0}));
        connect(While.y, timePeriod.tl) annotation(
          Line(points = {{-10, 80}, {-10, 60}}, color = {0, 0, 255}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 50}, {-44.4, 50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-35.6, 50}, {-21, 50}}, color = {162, 29, 33}));
        connect(booleanPulse.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-59, 90}, {-44.4, 90}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_4.y, While.u) annotation(
          Line(points = {{-35.6, 90}, {-21, 90}}, color = {162, 29, 33}));
        connect(timePeriod.y, showDiscreteTimeLocator.tl) annotation(
          Line(points = {{-10, 40}, {-10, 32}, {10, 32}, {10, 24}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_2.y, timePeriod.u2) annotation(
          Line(points = {{-35.6, 50}, {-30, 50}, {-30, 42}, {-21, 42}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_1.y, While1.u) annotation(
          Line(points = {{-35.6, -30}, {-30, -30}, {-30, -80}, {-1, -80}}, color = {162, 29, 33}));
        connect(While1.y, showDiscreteTimeWhile.tl) annotation(
          Line(points = {{10, -90}, {10, -94}, {34, -94}, {34, -84}, {48, -84}, {48, -88}}, color = {0, 0, 255}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end When2;