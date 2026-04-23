within CRML.ETL.Tests;

model SlidingRandom
        TimeLocators.Periods timePeriod(rightBoundaryIncluded = false, durationSpecified = true) annotation(
          Placement(transformation(extent = {{60, -20}, {80, 0}})));
        CRML.Blocks.Logical.RandomFailure failure annotation(
          Placement(transformation(extent = {{-40, -60}, {-20, -40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{28, -54}, {36, -46}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 8) annotation(
          Placement(transformation(extent = {{20, -80}, {40, -60}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerLower) annotation(
          Placement(transformation(extent = {{60, -60}, {80, -40}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(startTime = 1, width = 20, period = 1000) annotation(
          Placement(transformation(extent = {{-60, 20}, {-40, 40}})));
        CRML.Blocks.Math.Constant const(k = 8) annotation(
          Placement(transformation(extent = {{10, -20}, {30, 0}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{36, 46}, {44, 54}})));
        Modelica.Blocks.Logical.And or1 annotation(
          Placement(transformation(extent = {{0, 20}, {20, 40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{36, 26}, {44, 34}})));
        CRML.Blocks.Math.Constant integerConstant1 annotation(
          Placement(transformation(extent = {{-80, -60}, {-60, -40}})));
        TimeLocators.While While annotation(
          Placement(transformation(extent = {{60, 60}, {80, 80}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse1(startTime = 1, period = 1000, width = 28) annotation(
          Placement(transformation(extent = {{-60, 60}, {-40, 80}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-4, 66}, {4, 74}})));
        inner CRML.Blocks.Logical.GlobalSeed globalSeed(fixedSeed = 111111, useAutomaticSeed = true) annotation(
          Placement(transformation(extent = {{-80, -20}, {-60, 0}})));
      equation
        connect(failure.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-19, -50}, {27.6, -50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{36.4, -50}, {59, -50}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{70, -20}, {70, -40}}, color = {0, 0, 255}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{41, -70}, {50, -70}, {50, -58}, {59, -58}}, color = {255, 127, 0}));
        connect(or1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{21, 30}, {35.6, 30}}, color = {255, 0, 255}));
        connect(booleanPulse.y, or1.u1) annotation(
          Line(points = {{-39, 30}, {-2, 30}}, color = {217, 67, 180}));
        connect(failure.y, or1.u2) annotation(
          Line(points = {{-19, -50}, {-10, -50}, {-10, 22}, {-2, 22}}, color = {255, 0, 255}));
        connect(booleanToBoolean4_3.y, timePeriod.u1) annotation(
          Line(points = {{44.4, 30}, {50, 30}, {50, -10}, {59, -10}}, color = {162, 29, 33}));
        connect(or1.y, showEvent.u) annotation(
          Line(points = {{21, 30}, {28, 30}, {28, 50}, {35.6, 50}}, color = {255, 0, 255}));
        connect(booleanPulse1.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-39, 70}, {-4.4, 70}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, While.u) annotation(
          Line(points = {{4.4, 70}, {59, 70}}, color = {162, 29, 33}));
        connect(While.y, timePeriod.tl) annotation(
          Line(points = {{70, 60}, {70, 0}}, color = {0, 0, 255}));
        connect(integerConstant1.y, failure.failureRate) annotation(
          Line(points = {{-59, -50}, {-41, -50}}, color = {0, 0, 0}));
        connect(integerConstant1.y, failure.repairRate) annotation(
          Line(points = {{-59, -50}, {-50, -50}, {-50, -58}, {-41, -58}}, color = {0, 0, 0}));
        connect(const.y, timePeriod.continuousDuration) annotation(
          Line(points = {{31, -10}, {44, -10}, {44, -18}, {59, -18}}, color = {0, 0, 0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 30));
      end SlidingRandom;