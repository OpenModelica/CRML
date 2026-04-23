within CRML.TimeLocators.Attributes;

block PeriodsEnd "Returns the closing events of a multiple time period"
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
      public
        ETL.Connectors.TimeLocatorInput[N] tl annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        Blocks.Logical.Or_n or_n annotation(
          Placement(transformation(extent = {{40, -10}, {60, 10}})));
        ETL.TimeLocators.Attributes.PeriodEnd[N] timePeriodClosingEvents annotation(
          Placement(transformation(extent = {{-4, -4}, {4, 4}})));
      equation
        connect(tl, timePeriodClosingEvents.tl) annotation(
          Line(points = {{0, 100}, {0, 4}}, color = {0, 0, 255}));
        connect(timePeriodClosingEvents.y, or_n.u) annotation(
          Line(points = {{4.4, 0}, {39, 0}}, color = {217, 67, 180}));
        connect(or_n.y, y) annotation(
          Line(points = {{61, 0}, {110, 0}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5, fillColor = {170, 213, 255}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Line(points = {{-86, -64}, {-50, -64}, {-50, -20}, {60, -20}, {60, -66}, {88, -66}}, color = {0, 0, 0}), Text(extent = {{36, 40}, {82, -22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol")}),
          Diagram(coordinateSystem(initialScale = 0.04)),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> =<b>PeriodsEnd</b>(<b>tl</b>); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Returns the closing events of the time period <b>tl</b>.</span></p>
</html>"));
      end PeriodsEnd;