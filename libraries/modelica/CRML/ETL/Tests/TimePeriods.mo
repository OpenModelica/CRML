within CRML.ETL.Tests;

model TimePeriods
        import CRML;
        TimeLocators.Periods timePeriod(rightBoundaryIncluded = false, leftBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{40, 60}, {60, 80}})));
        CRML.ETL.Requirements.CheckAtEnd checkAtEnd annotation(
          Placement(transformation(extent = {{40, 20}, {60, 40}})));
        CRML.ETL.Requirements.CheckAnytime checkAnytime annotation(
          Placement(transformation(extent = {{70, -20}, {90, 0}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse1(period = 5, startTime = 1, width = 4) annotation(
          Placement(transformation(extent = {{-80, 0}, {-60, 20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-4, 6}, {4, 14}})));
        CRML.ETL.Requirements.CheckInteger checkInteger(redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreaterEqual) annotation(
          Placement(transformation(extent = {{70, -60}, {90, -40}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic(startTime = 1) annotation(
          Placement(transformation(extent = {{-80, -60}, {-60, -40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{36, -54}, {44, -46}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 3) annotation(
          Placement(transformation(extent = {{-40, -80}, {-20, -60}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-24, -34}, {-16, -26}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse2(period = 5, startTime = 1, width = 4) annotation(
          Placement(transformation(extent = {{-80, 50}, {-60, 70}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-4, 66}, {4, 74}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-4, 58}, {4, 66}})));
        Modelica.Blocks.Logical.Not not1 annotation(
          Placement(transformation(extent = {{-24, 58}, {-16, 66}})));
      equation
        connect(timePeriod.y, checkAtEnd.tl) annotation(
          Line(points = {{50, 60}, {50, 40}}, color = {0, 0, 255}));
        connect(timePeriod.y, checkAnytime.tl) annotation(
          Line(points = {{50, 60}, {50, 50}, {80, 50}, {80, 0}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_4.y, checkAnytime.u) annotation(
          Line(points = {{4.4, 10}, {20, 10}, {20, -10}, {69, -10}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_4.y, checkAtEnd.u) annotation(
          Line(points = {{4.4, 10}, {20, 10}, {20, 30}, {39, 30}}, color = {162, 29, 33}));
        connect(booleanPulse1.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-59, 10}, {-4.4, 10}}, color = {217, 67, 180}));
        connect(timePeriod.y, checkInteger.tl) annotation(
          Line(points = {{50, 60}, {50, 50}, {100, 50}, {100, -30}, {80, -30}, {80, -40}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_1.y, checkInteger.u) annotation(
          Line(points = {{44.4, -50}, {69, -50}}, color = {162, 29, 33}));
        connect(integerConstant.y, checkInteger.threshold) annotation(
          Line(points = {{-19, -70}, {20, -70}, {20, -58}, {69, -58}}, color = {255, 127, 0}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{4.4, 70}, {39, 70}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_3.y, timePeriod.u2) annotation(
          Line(points = {{4.4, 62}, {39, 62}}, color = {162, 29, 33}));
        connect(booleanPulse2.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 60}, {-40, 60}, {-40, 70}, {-4.4, 70}}, color = {217, 67, 180}));
        connect(booleanPulse2.y, not1.u) annotation(
          Line(points = {{-59, 60}, {-40, 60}, {-40, 62}, {-24.8, 62}}, color = {217, 67, 180}));
        connect(not1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-15.6, 62}, {-4.4, 62}}, color = {255, 0, 255}));
        connect(eventPeriodic.y, showEvent.u) annotation(
          Line(points = {{-59, -50}, {-40, -50}, {-40, -30}, {-24.4, -30}}, color = {217, 67, 180}));
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -50}, {35.6, -50}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 20));
      end TimePeriods;