within CRML.ETL.Tests;

model EventCounter
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1, 20}) annotation(
          Placement(transformation(extent = {{20, -100}, {40, -80}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {12, 30}) annotation(
          Placement(transformation(extent = {{-20, -100}, {0, -80}})));
        CRML.Blocks.Events.EventPeriodic start(startTime = 2, period = 11) annotation(
          Placement(transformation(extent = {{-100, 40}, {-80, 60}})));
        CRML.Blocks.Events.ShowEvent showEventStart annotation(
          Placement(transformation(extent = {{-54, 64}, {-46, 72}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic1(startTime = 1) annotation(
          Placement(transformation(extent = {{-100, -10}, {-80, 10}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-54, -24}, {-46, -16}})));
        CRML.Blocks.Events.EventPeriodic finish(period = 11, startTime = 10) annotation(
          Placement(transformation(extent = {{-100, -60}, {-80, -40}})));
        CRML.Blocks.Events.ShowEvent showEventFinish annotation(
          Placement(transformation(extent = {{-54, -54}, {-46, -46}})));
        TimeLocators.Periods timePeriod(leftBoundaryIncluded = false, rightBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{20, 40}, {40, 60}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-24, 46}, {-16, 54}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-24, 38}, {-16, 46}})));
        Requirements.Functions.MathInteger.EventCounter eventCounter1 annotation(
          Placement(transformation(extent = {{20, -10}, {40, 10}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-24, -4}, {-16, 4}})));
        Requirements.CheckInteger checkInteger(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerLowerEqual) annotation(
          Placement(transformation(extent = {{60, -40}, {80, -20}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 8) annotation(
          Placement(transformation(extent = {{0, -60}, {20, -40}})));
      equation
        connect(start.y, showEventStart.u) annotation(
          Line(points = {{-79, 50}, {-60, 50}, {-60, 68}, {-54.4, 68}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, showEvent.u) annotation(
          Line(points = {{-79, 0}, {-70, 0}, {-70, -20}, {-54.4, -20}}, color = {217, 67, 180}));
        connect(finish.y, showEventFinish.u) annotation(
          Line(points = {{-79, -50}, {-54.4, -50}}, color = {217, 67, 180}));
        connect(start.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-79, 50}, {-24.4, 50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, timePeriod.u1) annotation(
          Line(points = {{-15.6, 50}, {19, 50}}, color = {162, 29, 33}));
        connect(finish.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-79, -50}, {-60, -50}, {-60, 42}, {-24.4, 42}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u2) annotation(
          Line(points = {{-15.6, 42}, {19, 42}}, color = {162, 29, 33}));
        connect(timePeriod.y[1], eventCounter1.tl) annotation(
          Line(points = {{30, 40}, {30, 10}}, color = {0, 0, 255}));
        connect(eventPeriodic1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-79, 0}, {-24.4, 0}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_3.y, eventCounter1.u) annotation(
          Line(points = {{-15.6, 0}, {19, 0}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_3.y, checkInteger.u) annotation(
          Line(points = {{-15.6, 0}, {0, 0}, {0, -30}, {59, -30}}, color = {162, 29, 33}));
        connect(timePeriod.y, checkInteger.tl) annotation(
          Line(points = {{30, 40}, {30, 28}, {70, 28}, {70, -20}}, color = {0, 0, 255}));
        connect(integerConstant.y, checkInteger.threshold) annotation(
          Line(points = {{21, -50}, {40, -50}, {40, -38}, {59, -38}}, color = {255, 127, 0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 30));
      end EventCounter;