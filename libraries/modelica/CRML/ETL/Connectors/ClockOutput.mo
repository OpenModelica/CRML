within CRML.ETL.Connectors;

connector ClockOutput = output Clock "'output Clock' as connector" annotation(
        defaultComponentName = "y",
        Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, grid = {2, 2}, initialScale = 0.1), graphics = {Polygon(points = {{-10, 6}, {10, 0}, {-10, -6}, {-10, 6}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineThickness = 0.5, pattern = LinePattern.Dot), Polygon(points = {{-100, 99}, {100, -1}, {-100, -101}, {-100, 99}}, lineColor = {0, 140, 72}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid)}),
        Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, grid = {2, 2}, initialScale = 0.1), graphics = {Polygon(points = {{-100, 100}, {100, 0}, {-100, -100}, {-100, 100}}, lineColor = {0, 140, 72}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid)}),
        Documentation(info = "<html>
<p>
Connector with one output signal of type Boolean.
</p>
</html>"));