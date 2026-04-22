within CRML.ETL.Blocks.Events;

model BecomesTrue "Events generated when a Boolean becomes true"
          Connectors.Boolean4Input b1 annotation(
            Placement(transformation(extent = {{-120, 10}, {-100, -10}})));
          Connectors.ClockOutput y annotation(
            Placement(transformation(extent = {{100, 10}, {120, -10}})));
          CRML.Blocks.Events.ClockEvent clockEvent annotation(
            Placement(transformation(extent = {{-6, -6}, {6, 6}})));
          CRML.Blocks.Events.Event4ToEvent event4ToEvent annotation(
            Placement(transformation(extent = {{-54, -4}, {-46, 4}})));
        equation
          connect(clockEvent.y, y) annotation(
            Line(points = {{6.6, 0}, {110, 0}}, color = {175, 175, 175}, pattern = LinePattern.Dot, thickness = 0.5));
          connect(b1, event4ToEvent.u) annotation(
            Line(points = {{-110, 0}, {-54.4, 0}}, color = {0, 0, 0}));
          connect(clockEvent.u, event4ToEvent.y) annotation(
            Line(points = {{-6.6, 0}, {-45.6, 0}}, color = {217, 67, 180}));
          annotation(
            Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{30, 78}, {30, 34}}, color = {0, 140, 72}, pattern = LinePattern.Dot), Text(extent = {{-250, 170}, {250, 110}}, textString = "%name", lineColor = {0, 0, 255}), Line(points = {{-80, 34}, {-50, 34}, {-50, 78}, {-50, 78}, {-50, 34}, {52, 34}}, color = {0, 140, 72}, pattern = LinePattern.Dot), Line(points = {{-86, -52}, {-50, -52}, {-50, -8}, {-8, -8}, {-8, -54}, {-8, -54}}, color = {162, 29, 33}), Ellipse(extent = {{20, 88}, {40, 68}}, lineColor = {0, 140, 72}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-60, 88}, {-40, 68}}, lineColor = {0, 140, 72}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-8, -54}, {28, -54}, {28, -8}, {52, -8}, {52, -54}, {76, -54}}, color = {162, 29, 33})}),
            Diagram(coordinateSystem(preserveAspectRatio = false)));
        end BecomesTrue;