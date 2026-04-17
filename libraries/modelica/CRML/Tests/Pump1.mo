within CRML.Tests;

model Pump1 "While system is in operation, pump must not be started more than twice"
      Blocks.Logical.BooleanPulse pumpIsStarted(width = 40, period = 60) annotation(
        Placement(transformation(extent = {{-60, -10}, {-40, 10}})));
      Blocks.Logical.BooleanPulse systemInOperation(width = 200, startTime = 10, period = 230) annotation(
        Placement(transformation(extent = {{-60, 20}, {-40, 40}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
        Placement(transformation(extent = {{-4, 26}, {4, 34}})));
      Requirements.CheckCountLowerEqual checkCountLowerEqual(threshold = 2) annotation(
        Placement(transformation(extent = {{20, -10}, {40, 10}})));
      TimeLocators.Continuous.During during annotation(
        Placement(transformation(extent = {{20, 20}, {40, 40}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
        Placement(transformation(extent = {{-4, -4}, {4, 4}})));
    equation
      connect(systemInOperation.y, booleanToBoolean4_4.u) annotation(
        Line(points = {{-39, 30}, {-4.4, 30}}, color = {217, 67, 180}));
      connect(systemInOperation.y, pumpIsStarted.reset) annotation(
        Line(points = {{-39, 30}, {-28, 30}, {-28, -20}, {-50, -20}, {-50, -11}}, color = {217, 67, 180}));
      connect(during.y, checkCountLowerEqual.tl) annotation(
        Line(points = {{30, 20}, {30, 10}}, color = {0, 0, 255}));
      connect(booleanToBoolean4_4.y, during.u) annotation(
        Line(points = {{4.4, 30}, {19, 30}}, color = {162, 29, 33}));
      connect(pumpIsStarted.y, booleanToBoolean4_1.u) annotation(
        Line(points = {{-39, 0}, {-4.4, 0}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_1.y, checkCountLowerEqual.u) annotation(
        Line(points = {{4.4, 0}, {19, 0}}, color = {162, 29, 33}));
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
        Diagram(coordinateSystem(preserveAspectRatio = false)),
        experiment(StopTime = 500));
    end Pump1;