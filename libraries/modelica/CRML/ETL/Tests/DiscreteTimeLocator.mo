within CRML.ETL.Tests;

model DiscreteTimeLocator
        TimeLocators.Periods timePeriod(discreteClockSpecified = true, leftBoundaryIncluded = false, durationSpecified = true, rightBoundaryIncluded = true) annotation(
          Placement(transformation(extent = {{0, -10}, {20, 10}})));
        CRML.Blocks.Logical.BooleanConstant eventPeriodic annotation(
          Placement(transformation(extent = {{-80, -100}, {-60, -80}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, -74}, {-36, -66}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1, 1.5, 3, 5}) annotation(
          Placement(transformation(extent = {{-80, 0}, {-60, 20}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {10}) annotation(
          Placement(transformation(extent = {{-80, -70}, {-60, -50}})));
        Requirements.CheckAtEnd ensure annotation(
          Placement(transformation(extent = {{-2, -80}, {18, -60}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-44, -4}, {-36, 4}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-44, -64}, {-36, -56}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic1(period = 1.2) annotation(
          Placement(transformation(extent = {{-80, 70}, {-60, 90}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-42, 64}, {-34, 72}})));
        TimeLocators.While While1(rightBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{0, 30}, {20, 50}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(period = 14, startTime = 2.6, width = 6) annotation(
          Placement(transformation(extent = {{-80, 30}, {-60, 50}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_5 annotation(
          Placement(transformation(extent = {{-44, 36}, {-36, 44}})));
        TimeLocators.ShowDiscretePeriods showDiscreteTimePeriods annotation(
          Placement(transformation(extent = {{26, -36}, {34, -28}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-44, 76}, {-36, 84}})));
        CRML.Blocks.MathInteger.IntegerConstant const(K = 5) annotation(
          Placement(transformation(extent = {{-80, -30}, {-60, -10}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse1(period = 12, startTime = 4, width = 4) annotation(
          Placement(transformation(extent = {{0, 70}, {20, 90}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_6 annotation(
          Placement(transformation(extent = {{36, 76}, {44, 84}})));
        TimeLocators.While While(rightBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{60, 70}, {80, 90}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -90}, {-52, -90}, {-52, -70}, {-44.4, -70}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-35.6, -70}, {-3, -70}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{10, -10}, {10, -60}, {8, -60}}, color = {0, 0, 255}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 10}, {-50, 10}, {-50, 0}, {-44.4, 0}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-35.6, 0}, {-1, 0}}, color = {162, 29, 33}));
        connect(eventTable1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-59, -60}, {-44.4, -60}}, color = {217, 67, 180}));
        connect(booleanPulse.y, booleanToBoolean4_5.u) annotation(
          Line(points = {{-59, 40}, {-44.4, 40}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_5.y, While1.u) annotation(
          Line(points = {{-35.6, 40}, {-1, 40}}, color = {162, 29, 33}));
        connect(timePeriod.y, showDiscreteTimePeriods.tl) annotation(
          Line(points = {{10, -10}, {10, -20}, {30, -20}, {30, -28}}, color = {0, 0, 255}));
        connect(eventPeriodic1.y, showEvent.u) annotation(
          Line(points = {{-59, 80}, {-52, 80}, {-52, 68}, {-42.4, 68}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-59, 80}, {-44.4, 80}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_4.y, timePeriod.clock) annotation(
          Line(points = {{-35.6, 80}, {-20, 80}, {-20, 8}, {-1, 8}}, color = {162, 29, 33}));
        connect(booleanPulse1.y, booleanToBoolean4_6.u) annotation(
          Line(points = {{21, 80}, {35.6, 80}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_6.y, While.u) annotation(
          Line(points = {{44.4, 80}, {59, 80}}, color = {162, 29, 33}));
        connect(While.y, While1.tl) annotation(
          Line(points = {{70, 70}, {70, 60}, {10, 60}, {10, 50}}, color = {0, 0, 255}));
        connect(const.y, timePeriod.discreteDuration) annotation(
          Line(points = {{-59, -20}, {-20, -20}, {-20, -8}, {-1, -8}}, color = {255, 127, 0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end DiscreteTimeLocator;