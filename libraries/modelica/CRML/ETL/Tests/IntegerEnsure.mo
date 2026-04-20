within CRML.ETL.Tests;

model IntegerEnsure
        TimeLocators.Periods timePeriod(rightBoundaryIncluded = false, leftBoundaryIncluded = true) annotation(
          Placement(transformation(extent = {{-20, 0}, {0, 20}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic annotation(
          Placement(transformation(extent = {{-80, -50}, {-60, -30}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, -44}, {-36, -36}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1, 1.5, 3, 5}) annotation(
          Placement(transformation(extent = {{-80, 10}, {-60, 30}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {12, 13}) annotation(
          Placement(transformation(extent = {{-80, -20}, {-60, 0}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 5) annotation(
          Placement(transformation(extent = {{-80, -80}, {-60, -60}})));
        TimeLocators.While While annotation(
          Placement(transformation(extent = {{-20, 40}, {0, 60}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(period = 12, width = 9, startTime = 1) annotation(
          Placement(transformation(extent = {{-80, 40}, {-60, 60}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-44, 6}, {-36, 14}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-44, -2}, {-36, 6}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-44, 46}, {-36, 54}})));
        TimeLocators.While While1 annotation(
          Placement(transformation(extent = {{-20, 80}, {0, 100}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse1(period = 12, width = 8, startTime = 2) annotation(
          Placement(transformation(extent = {{-80, 80}, {-60, 100}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_5 annotation(
          Placement(transformation(extent = {{-44, 86}, {-36, 94}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreaterEqual) annotation(
          Placement(transformation(extent = {{-20, -50}, {0, -30}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-46, -32}, {-38, -24}})));
        Requirements.CheckAnytime checkRequirement annotation(
          Placement(transformation(extent = {{60, -50}, {80, -30}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -40}, {-44.4, -40}}, color = {217, 67, 180}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 20}, {-50, 20}, {-50, 10}, {-44.4, 10}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-35.6, 10}, {-21, 10}}, color = {162, 29, 33}));
        connect(eventTable1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-59, -10}, {-50, -10}, {-50, 2}, {-44.4, 2}}, color = {217, 67, 180}));
        connect(booleanPulse.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-59, 50}, {-44.4, 50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_4.y, While.u) annotation(
          Line(points = {{-35.6, 50}, {-21, 50}}, color = {162, 29, 33}));
        connect(While.y, timePeriod.tl) annotation(
          Line(points = {{-10, 40}, {-10, 20}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_3.y, timePeriod.u2) annotation(
          Line(points = {{-35.6, 2}, {-21, 2}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_5.y, While1.u) annotation(
          Line(points = {{-35.6, 90}, {-21, 90}}, color = {162, 29, 33}));
        connect(booleanPulse1.y, booleanToBoolean4_5.u) annotation(
          Line(points = {{-59, 90}, {-44.4, 90}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-35.6, -40}, {-21, -40}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{-10, 0}, {-10, -30}, {-10, -30}}, color = {0, 0, 255}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{-59, -70}, {-30, -70}, {-30, -48}, {-21, -48}}, color = {255, 127, 0}));
        connect(eventPeriodic.y, showEvent.u) annotation(
          Line(points = {{-59, -40}, {-52, -40}, {-52, -28}, {-46.4, -28}}, color = {217, 67, 180}));
        connect(timePeriod.y, checkRequirement.tl) annotation(
          Line(points = {{-10, 0}, {-10, -24}, {70, -24}, {70, -30}}, color = {0, 0, 255}));
        connect(ensure.y, checkRequirement.u) annotation(
          Line(points = {{1, -40}, {59, -40}}, color = {162, 29, 33}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end IntegerEnsure;