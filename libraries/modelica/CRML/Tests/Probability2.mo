within CRML.Tests;

model Probability2
      parameter Boolean reset = true;
      Blocks.Logical.RandomFailure randomFailure annotation(
        Placement(transformation(extent = {{-60, -30}, {-40, -10}})));
      inner Blocks.Logical.GlobalSeed globalSeed annotation(
        Placement(transformation(extent = {{-60, 0}, {-40, 20}})));
      Blocks.Math.Constant const1(k = 0.3) annotation(
        Placement(transformation(extent = {{-100, -20}, {-80, 0}})));
      Blocks.Math.Constant const2(k = 3) annotation(
        Placement(transformation(extent = {{-100, -50}, {-80, -30}})));
      Blocks.Logical.Heaviside heaviside annotation(
        Placement(transformation(extent = {{-10, -30}, {10, -10}})));
      Blocks.Logical4.Probability unavailability(reset = reset, fileName = "probatest1.csv", dirName = "C:/Users/I51238/Documents/Dymola") annotation(
        Placement(transformation(extent = {{20, 50}, {40, 70}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
        Placement(transformation(extent = {{-24, 56}, {-16, 64}})));
      TimeLocators.Continuous.During during annotation(
        Placement(transformation(extent = {{-10, 80}, {10, 100}})));
      Blocks.Logical.BooleanPulse booleanPulse(width = 10, period = 100, startTime = 1.5) annotation(
        Placement(transformation(extent = {{-80, 80}, {-60, 100}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
        Placement(transformation(extent = {{-24, 86}, {-16, 94}})));
      Blocks.Logical4.Probability unreliability(reset = reset, fileName = "probatest2.csv", dirName = "C:/Users/I51238/Documents/Dymola") annotation(
        Placement(transformation(extent = {{58, -30}, {78, -10}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
        Placement(transformation(extent = {{36, -24}, {44, -16}})));
      inner TimeLocators.Continuous.Master master annotation(
        Placement(transformation(extent = {{-60, 40}, {-40, 60}})));
      Blocks.Events.EventPeriodic eventPeriodic(period = 0.1) annotation(
        Placement(transformation(extent = {{-60, -80}, {-40, -60}})));
    equation
      connect(const1.y, randomFailure.failureRate) annotation(
        Line(points = {{-79, -10}, {-70, -10}, {-70, -20}, {-61, -20}}, color = {0, 0, 0}));
      connect(const2.y, randomFailure.repairRate) annotation(
        Line(points = {{-79, -40}, {-70, -40}, {-70, -28}, {-61, -28}}, color = {0, 0, 0}));
      connect(randomFailure.y, heaviside.u) annotation(
        Line(points = {{-39, -20}, {-11, -20}}, color = {217, 67, 180}));
      connect(randomFailure.y, booleanToBoolean4_1.u) annotation(
        Line(points = {{-39, -20}, {-30, -20}, {-30, 60}, {-24.4, 60}}, color = {217, 67, 180}));
      connect(booleanPulse.y, booleanToBoolean4_2.u) annotation(
        Line(points = {{-59, 90}, {-24.4, 90}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_2.y, during.u) annotation(
        Line(points = {{-15.6, 90}, {-11, 90}}, color = {162, 29, 33}));
      connect(booleanToBoolean4_3.y, unreliability.u) annotation(
        Line(points = {{44.4, -20}, {57, -20}}, color = {162, 29, 33}));
      connect(heaviside.y, booleanToBoolean4_3.u) annotation(
        Line(points = {{11, -20}, {35.6, -20}}, color = {217, 67, 180}));
      connect(eventPeriodic.y, unreliability.event) annotation(
        Line(points = {{-39, -70}, {40, -70}, {40, -28}, {57, -28}}, color = {217, 67, 180}));
      connect(eventPeriodic.y, unavailability.event) annotation(
        Line(points = {{-39, -70}, {-20, -70}, {-20, 52}, {19, 52}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_1.y, unavailability.u) annotation(
        Line(points = {{-15.6, 60}, {19, 60}}, color = {162, 29, 33}));
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
        Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{8, 44}, {50, 36}}, lineColor = {0, 0, 0}, pattern = LinePattern.Dash, fillColor = {210, 210, 210}, fillPattern = FillPattern.Solid, textString = "Unavailability"), Text(extent = {{48, -36}, {90, -44}}, lineColor = {0, 0, 0}, pattern = LinePattern.Dash, fillColor = {210, 210, 210}, fillPattern = FillPattern.Solid, textString = "Unreliability")}),
        experiment(StopTime = 22));
    end Probability2;