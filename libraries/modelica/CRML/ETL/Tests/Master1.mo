within CRML.ETL.Tests;

model Master1
        import CRML;
        CRML.Blocks.Logical.BooleanPulse booleanPulse(width = 6, period = 20, startTime = 2) annotation(
          Placement(transformation(extent = {{-100, 80}, {-80, 100}})));
        inner CRML.TimeLocators.Continuous.Master master annotation(
          Placement(transformation(extent = {{80, 80}, {100, 100}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_7 annotation(
          Placement(transformation(extent = {{16, 86}, {24, 94}})));
        CRML.TimeLocators.Continuous.During during annotation(
          Placement(transformation(extent = {{60, -20}, {80, 0}})));
        CRML.Blocks.Logical.BooleanConstant booleanConstant annotation(
          Placement(transformation(extent = {{-100, 0}, {-80, 20}})));
        Modelica.Blocks.Logical.And and1 annotation(
          Placement(transformation(extent = {{-40, 20}, {-20, 40}})));
      equation
        connect(booleanToBoolean4_7.y, master.u) annotation(
          Line(points = {{24.4, 90}, {79, 90}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_7.y, during.u) annotation(
          Line(points = {{24.4, 90}, {40, 90}, {40, -10}, {59, -10}}, color = {162, 29, 33}));
        connect(booleanConstant.y, and1.u2) annotation(
          Line(points = {{-79, 10}, {-60, 10}, {-60, 22}, {-42, 22}}, color = {217, 67, 180}));
        connect(booleanPulse.y, and1.u1) annotation(
          Line(points = {{-79, 90}, {-60, 90}, {-60, 30}, {-42, 30}}, color = {217, 67, 180}));
        connect(and1.y, booleanToBoolean4_7.u) annotation(
          Line(points = {{-19, 30}, {0, 30}, {0, 90}, {15.6, 90}}, color = {255, 0, 255}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end Master1;