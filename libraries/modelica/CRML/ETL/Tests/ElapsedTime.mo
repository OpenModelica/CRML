within CRML.ETL.Tests;

model ElapsedTime
        CRML.Blocks.Events.EventTable event1(option_width = false, instant = {0.5, 4, 5, 7, 10}) annotation(
          Placement(transformation(extent = {{-100, 30}, {-80, 50}})));
        CRML.Blocks.Events.ShowEvent showEvent1 annotation(
          Placement(transformation(extent = {{-54, 44}, {-46, 52}})));
        CRML.Blocks.Events.EventTable event2(option_width = false, instant = {5, 7, 10, 12, 15}) annotation(
          Placement(transformation(extent = {{-100, -10}, {-80, 10}})));
        CRML.Blocks.Events.ShowEvent showEvent2 annotation(
          Placement(transformation(extent = {{-54, 6}, {-46, 14}})));
        CRML.Blocks.Events.ElapsedTime elapsedTime annotation(
          Placement(transformation(extent = {{-20, 10}, {0, 30}})));
      equation
        connect(event1.y, showEvent1.u) annotation(
          Line(points = {{-79, 40}, {-60, 40}, {-60, 48}, {-54.4, 48}}, color = {217, 67, 180}));
        connect(event2.y, showEvent2.u) annotation(
          Line(points = {{-79, 0}, {-60, 0}, {-60, 10}, {-54.4, 10}}, color = {217, 67, 180}));
        connect(event1.y, elapsedTime.u1) annotation(
          Line(points = {{-79, 40}, {-30, 40}, {-30, 28}, {-21, 28}}, color = {217, 67, 180}));
        connect(event2.y, elapsedTime.u2) annotation(
          Line(points = {{-79, 0}, {-30, 0}, {-30, 12}, {-21, 12}}, color = {217, 67, 180}));
        connect(event1.y, elapsedTime.reset) annotation(
          Line(points = {{-79, 40}, {-40, 40}, {-40, -10}, {-10, -10}, {-10, 9}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 20));
      end ElapsedTime;