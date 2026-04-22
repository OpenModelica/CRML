within CRML.ETL.Tests;

model Sliding
        TimeLocators.Periods timePeriod(rightBoundaryIncluded = false, durationSpecified = true) annotation(
          Placement(transformation(extent = {{20, -20}, {40, 0}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic annotation(
          Placement(transformation(extent = {{-80, -60}, {-60, -40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-24, -54}, {-16, -46}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 5) annotation(
          Placement(transformation(extent = {{-80, -100}, {-60, -80}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerLowerEqual) annotation(
          Placement(transformation(extent = {{20, -60}, {40, -40}})));
        TimeLocators.While While annotation(
          Placement(transformation(extent = {{20, 60}, {40, 80}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(startTime = 1, width = 20, period = 22) annotation(
          Placement(transformation(extent = {{-80, 60}, {-60, 80}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-4, 66}, {4, 74}})));
        CRML.Blocks.Math.Constant const(k = 8) annotation(
          Placement(transformation(extent = {{-80, -20}, {-60, 0}})));
        Modelica.Blocks.Logical.And or1 annotation(
          Placement(transformation(extent = {{-40, 20}, {-20, 40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-4, 26}, {4, 34}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-24, -36}, {-16, -28}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, -50}, {-24.4, -50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-15.6, -50}, {19, -50}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{30, -20}, {30, -40}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_4.y, While.u) annotation(
          Line(points = {{4.4, 70}, {19, 70}}, color = {162, 29, 33}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{-59, -90}, {0, -90}, {0, -58}, {19, -58}}, color = {255, 127, 0}));
        connect(booleanPulse.y, or1.u1) annotation(
          Line(points = {{-59, 70}, {-50, 70}, {-50, 30}, {-42, 30}}, color = {217, 67, 180}));
        connect(eventPeriodic.y, or1.u2) annotation(
          Line(points = {{-59, -50}, {-50, -50}, {-50, 22}, {-42, 22}}, color = {217, 67, 180}));
        connect(or1.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-19, 30}, {-4.4, 30}}, color = {255, 0, 255}));
        connect(booleanPulse.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-59, 70}, {-4.4, 70}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, timePeriod.u1) annotation(
          Line(points = {{-15.6, -50}, {10, -50}, {10, -10}, {19, -10}}, color = {162, 29, 33}));
        connect(eventPeriodic.y, showEvent.u) annotation(
          Line(points = {{-59, -50}, {-30, -50}, {-30, -32}, {-24.4, -32}}, color = {217, 67, 180}));
        connect(const.y, timePeriod.continuousDuration) annotation(
          Line(points = {{-59, -10}, {-20, -10}, {-20, -18}, {19, -18}}, color = {0, 0, 0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 24));
      end Sliding;