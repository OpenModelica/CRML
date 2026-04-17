within CRML.ETL.Tests;

model Shift
        TimeLocators.Periods timePeriod(durationSpecified = true, leftBoundaryIncluded = false, rightBoundaryIncluded = true) annotation(
          Placement(transformation(extent = {{40, 40}, {60, 60}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic annotation(
          Placement(transformation(extent = {{-80, -50}, {-60, -30}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, -34}, {-36, -26}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1, 3, 6, 9, 11}) annotation(
          Placement(transformation(extent = {{-80, 40}, {-60, 60}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 5) annotation(
          Placement(transformation(extent = {{-80, -80}, {-60, -60}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreaterEqual) annotation(
          Placement(transformation(extent = {{40, -40}, {60, -20}})));
        TimeLocators.While While annotation(
          Placement(transformation(extent = {{40, 80}, {60, 100}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(startTime = 1, width = 20, period = 22) annotation(
          Placement(transformation(extent = {{-80, 80}, {-60, 100}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{22, 46}, {30, 54}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-44, 86}, {-36, 94}})));
        CRML.Blocks.Math.Constant const(k = 10) annotation(
          Placement(transformation(extent = {{-80, -20}, {-60, 0}})));
        CRML.Blocks.Events.EventDelay eventDelay annotation(
          Placement(transformation(extent = {{-30, 40}, {-10, 60}})));
        CRML.Blocks.Math.Constant const1(k = 5) annotation(
          Placement(transformation(extent = {{-80, 10}, {-60, 30}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-44, 56}, {-36, 64}})));
        CRML.Blocks.Events.ShowEvent showEvent1 annotation(
          Placement(transformation(extent = {{8, 18}, {16, 26}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -40}, {-52, -40}, {-52, -30}, {-44.4, -30}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-35.6, -30}, {39, -30}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{50, 40}, {50, -20}}, color = {0, 0, 255}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{-59, -70}, {32, -70}, {32, -38}, {39, -38}}, color = {255, 127, 0}));
        connect(While.y, timePeriod.tl) annotation(
          Line(points = {{50, 80}, {50, 60}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{30.4, 50}, {39, 50}}, color = {162, 29, 33}));
        connect(booleanPulse.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-59, 90}, {-44.4, 90}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_4.y, While.u) annotation(
          Line(points = {{-35.6, 90}, {39, 90}}, color = {162, 29, 33}));
        connect(const1.y, eventDelay.delay) annotation(
          Line(points = {{-59, 20}, {-40, 20}, {-40, 42}, {-31, 42}}, color = {0, 0, 0}));
        connect(eventTable.y, showEvent.u) annotation(
          Line(points = {{-59, 50}, {-50, 50}, {-50, 60}, {-44.4, 60}}, color = {217, 67, 180}));
        connect(eventDelay.y, showEvent1.u) annotation(
          Line(points = {{-9, 50}, {-4, 50}, {-4, 22}, {7.6, 22}}, color = {217, 67, 180}));
        connect(eventTable.y, eventDelay.u) annotation(
          Line(points = {{-59, 50}, {-31, 50}}, color = {217, 67, 180}));
        connect(eventDelay.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-9, 50}, {21.6, 50}}, color = {217, 67, 180}));
        connect(const.y, timePeriod.continuousDuration) annotation(
          Line(points = {{-59, -10}, {32, -10}, {32, 42}, {39, 42}}, color = {0, 0, 0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end Shift;