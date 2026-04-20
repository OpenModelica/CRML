within CRML.ETL.TimeLocators.Attributes;

block IsRightBoundaryIncluded "Returns true if the right boundary of the time period is included"
          Connectors.TimeLocatorInput tl annotation(
            Placement(transformation(extent = {{-10, 90}, {10, 110}})));
          Connectors.BooleanOutput y annotation(
            Placement(transformation(extent = {{100, -10}, {120, 10}})));
        equation
          y = tl.isRightBoundaryIncluded;
          annotation(
            Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5, fillColor = {170, 213, 255}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-86, -64}, {-50, -64}, {-50, -20}, {60, -20}, {60, -66}, {88, -66}}, color = {0, 0, 0}), Line(points = {{60, 18}, {60, -14}}, color = {0, 0, 0})}),
            Diagram(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5, fillColor = {170, 213, 255}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-86, -64}, {-50, -64}, {-50, -20}, {60, -20}, {60, -66}, {88, -66}}, color = {0, 0, 0}), Line(points = {{60, 18}, {60, -14}}, color = {0, 0, 0})}),
            Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> =<b>ShowDiscreteTimeLocator</b>(<b>tl</b> = discrete_time_locator); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p>Displays the time periods of a <span style=\"font-family: MS Shell Dlg 2;\"><a href=\"modelica://ETL.TimeLocators.DiscreteTimeLocator\">DiscreteTimeLocator</a> block in the form of clock ticks inside the time periods of the time locator.</span></p>
</html>"));
        end IsRightBoundaryIncluded;