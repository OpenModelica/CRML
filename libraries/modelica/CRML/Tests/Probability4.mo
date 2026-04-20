within CRML.Tests;

model Probability4
      parameter Boolean reset = true;
      Blocks.Logical.RandomFailure randomFailure annotation(
        Placement(transformation(extent = {{-80, 10}, {-60, 30}})));
      inner Blocks.Logical.GlobalSeed globalSeed annotation(
        Placement(transformation(extent = {{-100, 80}, {-80, 100}})));
      Blocks.Math.Constant const1(k = 0.3) annotation(
        Placement(transformation(extent = {{-120, 20}, {-100, 40}})));
      Blocks.Math.Constant const2(k = 5.7) annotation(
        Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
      Blocks.Logical4.Probability probability(reset = reset, fileName = "probatest1.csv", dirName = "C:/Users/I51238/Documents/Dymola") annotation(
        Placement(transformation(extent = {{160, 20}, {180, 40}})));
      ETL.TimeLocators.Periods timePeriod(rightBoundaryIncluded = false, durationSpecified = true) annotation(
        Placement(transformation(extent = {{40, 80}, {60, 100}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
        Placement(transformation(extent = {{16, 26}, {24, 34}})));
      Requirements.CheckDurationLowerEqual checkDuration(threshold = 1) annotation(
        Placement(transformation(extent = {{40, 20}, {60, 40}})));
      Blocks.Math.Constant const(k = 4) annotation(
        Placement(transformation(extent = {{-10, 72}, {10, 92}})));
      inner TimeLocators.Continuous.Master master annotation(
        Placement(transformation(extent = {{140, 138}, {160, 158}})));
      Blocks.Math.Greater4 greater4 annotation(
        Placement(transformation(extent = {{100, -60}, {120, -40}})));
      Blocks.Math.Constant const3(k = 0.7) annotation(
        Placement(transformation(extent = {{28, -68}, {48, -48}})));
      TimeLocators.Continuous.During during(rightBoundaryIncluded = false) annotation(
        Placement(transformation(extent = {{100, 140}, {120, 160}})));
      Blocks.Logical.BooleanPulse inOperation(width = 40, period = 50, startTime = 2) annotation(
        Placement(transformation(extent = {{-100, 140}, {-80, 160}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
        Placement(transformation(extent = {{-24, 146}, {-16, 154}})));
      Modelica.Blocks.Logical.Or or1 annotation(
        Placement(transformation(extent = {{-6, 24}, {6, 36}})));
      Requirements.Ensure ensure annotation(
        Placement(transformation(extent = {{100, 20}, {120, 40}})));
      ETL.TimeLocators.While While annotation(
        Placement(transformation(extent = {{40, 120}, {60, 140}})));
      Requirements.CheckAtEnd checkAtEnd annotation(
        Placement(transformation(extent = {{140, -60}, {160, -40}})));
      Modelica.Blocks.Logical.Edge edge1 annotation(
        Placement(transformation(extent = {{4, -4}, {-4, 4}}, rotation = 180, origin = {-30, 40})));
      Modelica.Blocks.Logical.Not not1 annotation(
        Placement(transformation(extent = {{-34, 106}, {-26, 114}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
        Placement(transformation(extent = {{116, -14}, {124, -6}})));
      TimeLocators.Continuous.During during1 annotation(
        Placement(transformation(extent = {{140, -20}, {160, 0}})));
    equation
      connect(const1.y, randomFailure.failureRate) annotation(
        Line(points = {{-99, 30}, {-90, 30}, {-90, 20}, {-81, 20}}, color = {0, 0, 0}));
      connect(const2.y, randomFailure.repairRate) annotation(
        Line(points = {{-99, 0}, {-90, 0}, {-90, 12}, {-81, 12}}, color = {0, 0, 0}));
      connect(booleanToBoolean4_2.y, checkDuration.u) annotation(
        Line(points = {{24.4, 30}, {39, 30}}, color = {162, 29, 33}));
      connect(timePeriod.y, checkDuration.tl) annotation(
        Line(points = {{50, 80}, {50, 40}}, color = {0, 0, 255}));
      connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
        Line(points = {{24.4, 30}, {30, 30}, {30, 90}, {39, 90}}, color = {162, 29, 33}));
      connect(const.y, timePeriod.continuousDuration) annotation(
        Line(points = {{11, 82}, {39, 82}}, color = {0, 0, 0}));
      connect(probability.y, greater4.u1) annotation(
        Line(points = {{181, 30}, {190, 30}, {190, 10}, {60, 10}, {60, -50}, {99, -50}}, color = {0, 0, 0}));
      connect(const3.y, greater4.u2) annotation(
        Line(points = {{49, -58}, {99, -58}}, color = {0, 0, 0}));
      connect(inOperation.y, booleanToBoolean4_1.u) annotation(
        Line(points = {{-79, 150}, {-24.4, 150}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_1.y, during.u) annotation(
        Line(points = {{-15.6, 150}, {99, 150}}, color = {162, 29, 33}));
      connect(or1.y, booleanToBoolean4_2.u) annotation(
        Line(points = {{6.6, 30}, {15.6, 30}}, color = {255, 0, 255}));
      connect(checkDuration.y, ensure.u) annotation(
        Line(points = {{61, 30}, {99, 30}}, color = {162, 29, 33}));
      connect(ensure.y, probability.u) annotation(
        Line(points = {{121, 30}, {159, 30}}, color = {162, 29, 33}));
      connect(during.y, ensure.tl) annotation(
        Line(points = {{110, 140}, {110, 40}}, color = {0, 0, 255}));
      connect(randomFailure.y, or1.u2) annotation(
        Line(points = {{-59, 20}, {-20, 20}, {-20, 25.2}, {-7.2, 25.2}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_1.y, While.u) annotation(
        Line(points = {{-15.6, 150}, {0, 150}, {0, 130}, {39, 130}}, color = {162, 29, 33}));
      connect(While.y, timePeriod.tl) annotation(
        Line(points = {{50, 120}, {50, 100}}, color = {0, 0, 255}));
      connect(greater4.y, checkAtEnd.u) annotation(
        Line(points = {{121, -50}, {139, -50}}, color = {162, 29, 33}));
      connect(edge1.y, or1.u1) annotation(
        Line(points = {{-25.6, 40}, {-20, 40}, {-20, 30}, {-7.2, 30}}, color = {255, 0, 255}));
      connect(inOperation.y, edge1.u) annotation(
        Line(points = {{-79, 150}, {-50, 150}, {-50, 40}, {-34.8, 40}}, color = {217, 67, 180}));
      connect(inOperation.y, not1.u) annotation(
        Line(points = {{-79, 150}, {-50, 150}, {-50, 110}, {-34.8, 110}}, color = {217, 67, 180}));
      connect(inOperation.y, booleanToBoolean4_4.u) annotation(
        Line(points = {{-79, 150}, {-50, 150}, {-50, -10}, {115.6, -10}}, color = {217, 67, 180}));
      connect(not1.y, probability.event) annotation(
        Line(points = {{-25.6, 110}, {140, 110}, {140, 22}, {159, 22}}, color = {255, 0, 255}));
      connect(booleanToBoolean4_4.y, during1.u) annotation(
        Line(points = {{124.4, -10}, {139, -10}}, color = {162, 29, 33}));
      connect(during1.y, checkAtEnd.tl) annotation(
        Line(points = {{150, -20}, {150, -40}}, color = {0, 0, 255}));
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false, initialScale = 0.1), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
        Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-120, -100}, {200, 160}}), graphics = {Text(extent = {{-86, -14}, {-44, -22}}, lineColor = {0, 0, 0}, pattern = LinePattern.Dash, fillColor = {210, 210, 210}, fillPattern = FillPattern.Solid, textString = "Unavailability"), Text(extent = {{-34, -62}, {134, -112}}, lineColor = {28, 108, 200}, textString = "While the system is in operation, 
unvailability should be less of 1 hour in a sliding time period of one month
 with a probability greater than 99 percent."), Text(extent = {{-22, 70}, {22, 62}}, lineColor = {28, 108, 200}, textString = "1 month"), Text(extent = {{-112, 136}, {-68, 128}}, lineColor = {28, 108, 200}, textString = "10 months")}),
        experiment(StopTime = 50));
    end Probability4;