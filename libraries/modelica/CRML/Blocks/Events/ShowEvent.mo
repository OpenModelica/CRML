within CRML.Blocks.Events;

block ShowEvent "Event visualizer"
        ETL.Connectors.BooleanInput u(start = false, fixed = true) annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ClockEvent eventClock annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}})));
        ClockToBoolean clockToBoolean annotation(
          Placement(transformation(extent = {{40, -10}, {60, 10}})));
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        connect(u, eventClock.u) annotation(
          Line(points = {{-110, 0}, {-11, 0}, {-11, 0}}, color = {217, 67, 180}));
        connect(clockToBoolean.u, eventClock.y) annotation(
          Line(points = {{38, 0}, {24, 0}, {24, 0}, {11, 0}}, color = {175, 175, 175}, pattern = LinePattern.Dot, thickness = 0.5));
        connect(clockToBoolean.y, y) annotation(
          Line(points = {{62, 0}, {110, 0}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Ellipse(lineColor = {64, 64, 64}, fillColor = DynamicSelect({192, 192, 192}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), pattern = LinePattern.None, fillPattern = FillPattern.Sphere, extent = {{-40, -42}, {40, 40}}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(initialScale = 0.04)));
      end ShowEvent;