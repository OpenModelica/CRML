within CRML.Tests;
model Pump
      TimeLocators.Continuous.FromFor delayToNominalSpeed annotation(
        Placement(transformation(extent = {{40, -32}, {60, -12}})));
      Requirements.CheckAtEnd Req2 annotation(
        Placement(transformation(extent = {{40, -100}, {60, -80}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
        Placement(transformation(extent = {{6, -26}, {14, -18}})));
      Blocks.Logical.BooleanPulse pulse(width = 40, period = 60) annotation(
        Placement(transformation(extent = {{-100, -10}, {-80, 10}})));
      Blocks.Logical.BooleanPulse plantStarted(width = 200, startTime = 10, period = 230) annotation(
        Placement(transformation(extent = {{-100, 50}, {-80, 70}})));
      TimeLocators.Continuous.FromUntil plantInOperation annotation(
        Placement(transformation(extent = {{40, 50}, {60, 70}})));
      Modelica.Blocks.Logical.Not not2 annotation(
        Placement(transformation(extent = {{-64, 48}, {-56, 56}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
        Placement(transformation(extent = {{16, 56}, {24, 64}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_5 annotation(
        Placement(transformation(extent = {{16, 48}, {24, 56}})));
      Blocks.Math.Ramp pumpSpeed(height = 100, startTime = 2, duration = 30) annotation(
        Placement(transformation(extent = {{-100, -70}, {-80, -50}})));
      Blocks.Math.Constant nominalSpeedLimit(k = 90) annotation(
        Placement(transformation(extent = {{-100, -100}, {-80, -80}})));
      Blocks.Math.GreaterEqual4 greater4_1 annotation(
        Placement(transformation(extent = {{0, -100}, {20, -80}})));
      Blocks.Math.Constant startupDelay(k = 30) annotation(
        Placement(transformation(extent = {{-100, -40}, {-80, -20}})));
      Requirements.CheckCountLowerEqual Req1(threshold = 3) annotation(
        Placement(transformation(extent = {{40, 20}, {60, 40}})));
      Blocks.Logical4.bAnd4 and4_1
                                  annotation(
        Placement(transformation(extent = {{80, -10}, {100, 10}})));
      Modelica.Blocks.Logical.And pumpStarted annotation(
        Placement(transformation(extent = {{-40, -20}, {-20, 0}})));
      TimeLocators.Continuous.While While annotation(
        Placement(transformation(extent = {{40, 80}, {60, 100}})));
      Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
        Placement(transformation(extent = {{16, 86}, {24, 94}})));
equation
      connect(delayToNominalSpeed.y, Req2.tl) annotation(
        Line(points = {{50, -32}, {50, -80}}, color = {0, 0, 255}));
      connect(plantStarted.y, not2.u) annotation(
        Line(points = {{-79, 60}, {-70, 60}, {-70, 52}, {-64.8, 52}}, color = {217, 67, 180}));
      connect(plantStarted.y, booleanToBoolean4_4.u) annotation(
        Line(points = {{-79, 60}, {15.6, 60}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_4.y, plantInOperation.u1) annotation(
        Line(points = {{24.4, 60}, {39, 60}}, color = {162, 29, 33}));
      connect(not2.y, booleanToBoolean4_5.u) annotation(
        Line(points = {{-55.6, 52}, {15.6, 52}}, color = {255, 0, 255}));
      connect(pumpSpeed.y, greater4_1.u1) annotation(
        Line(points = {{-79, -60}, {-40, -60}, {-40, -90}, {-1, -90}}, color = {0, 0, 0}));
      connect(nominalSpeedLimit.y, greater4_1.u2) annotation(
        Line(points = {{-79, -90}, {-60, -90}, {-60, -98}, {-1, -98}}, color = {0, 0, 0}));
      connect(greater4_1.y, Req2.u) annotation(
        Line(points = {{21, -90}, {39, -90}}, color = {162, 29, 33}));
      connect(booleanToBoolean4_5.y, plantInOperation.u2) annotation(
        Line(points = {{24.4, 52}, {39, 52}}, color = {162, 29, 33}));
      connect(booleanToBoolean4_2.y, Req1.u) annotation(
        Line(points = {{14.4, -22}, {30, -22}, {30, 30}, {39, 30}}, color = {162, 29, 33}));
      connect(plantInOperation.y, Req1.tl) annotation(
        Line(points = {{50, 50}, {50, 40}}, color = {0, 0, 255}));
      connect(Req1.y, and4_1.u1) annotation(
        Line(points = {{61, 30}, {70, 30}, {70, 8}, {79, 8}}, color = {162, 29, 33}));
      connect(Req2.y, and4_1.u2) annotation(
        Line(points = {{61, -90}, {70, -90}, {70, -8}, {79, -8}}, color = {162, 29, 33}));
      connect(plantStarted.y, pulse.reset) annotation(
        Line(points = {{-79, 60}, {-70, 60}, {-70, -14}, {-90, -14}, {-90, -11}}, color = {217, 67, 180}));
      connect(pulse.y, pumpStarted.u2) annotation(
        Line(points = {{-79, 0}, {-60, 0}, {-60, -18}, {-42, -18}}, color = {217, 67, 180}));
      connect(pumpStarted.y, booleanToBoolean4_2.u) annotation(
        Line(points = {{-19, -10}, {-10, -10}, {-10, -22}, {5.6, -22}}, color = {255, 0, 255}));
      connect(pumpStarted.y, pumpSpeed.reset) annotation(
        Line(points = {{-19, -10}, {-10, -10}, {-10, -74}, {-90, -74}, {-90, -71}}, color = {255, 0, 255}));
      connect(plantStarted.y, pumpStarted.u1) annotation(
        Line(points = {{-79, 60}, {-70, 60}, {-70, 10}, {-48, 10}, {-48, -10}, {-42, -10}}, color = {217, 67, 180}));
      connect(While.y, plantInOperation.tl) annotation(
        Line(points = {{50, 80}, {50, 70}}, color = {0, 0, 255}));
      connect(plantStarted.y, booleanToBoolean4_1.u) annotation(
        Line(points = {{-79, 60}, {-70, 60}, {-70, 90}, {15.6, 90}}, color = {217, 67, 180}));
      connect(booleanToBoolean4_1.y, While.u) annotation(
        Line(points = {{24.4, 90}, {39, 90}}, color = {162, 29, 33}));
      connect(While.y, delayToNominalSpeed.tl) annotation(
        Line(points = {{50, 80}, {50, 74}, {76, 74}, {76, 12}, {50, 12}, {50, -12}}, color = {0, 0, 255}));
      connect(startupDelay.y, delayToNominalSpeed.duration) annotation(
        Line(points = {{-79, -30}, {39, -30}}, color = {0, 0, 0}));
      connect(booleanToBoolean4_2.y, delayToNominalSpeed.u) annotation(
        Line(points = {{14.4, -22}, {39, -22}}, color = {162, 29, 33}));
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
        Diagram(coordinateSystem(preserveAspectRatio = false)),
        experiment(StopTime = 500));
end Pump;
