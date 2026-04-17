within CRML.ETL.TimeLocators;

block While "While time locator"
        import CRML.ETL.Types.Boolean4;
        parameter Boolean leftBoundaryIncluded = true "If true, the left boundaries of the time periods are included";
        parameter Boolean rightBoundaryIncluded = true "If true, the right boundaries of the time periods are included";
      public
        Connectors.WhileOutput y "Vector of time periods" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100}), iconTransformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100})));
        Connectors.Boolean4Input u "Alternating opening and closing events" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
      public
        Connectors.WhileInput tl annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
      equation
        if (cardinality(tl) == 0) then
          tl.timePeriod = true;
          tl.clock = Boolean4.true4;
          tl.isLeftBoundaryIncluded = true;
          tl.isRightBoundaryIncluded = true;
        end if;
        y.timePeriod = (u == Boolean4.true4) and tl.timePeriod;
        y.clock = Boolean4.true4;
        y.isLeftBoundaryIncluded = leftBoundaryIncluded;
        y.isRightBoundaryIncluded = rightBoundaryIncluded;
        annotation(
          Icon(coordinateSystem(extent = {{-100, -100}, {100, 100}}, initialScale = 0.1, preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {170, 255, 255}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Rectangle(extent = {{-70, 30}, {70, -52}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-78, 88}, {76, 40}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "while"), Line(points = {{-90, 30}, {-70, 30}, {-70, -52}, {-90, -52}}, color = {0, 0, 0}, visible = not leftBoundaryIncluded), Line(points = {{-50, 30}, {-70, 30}, {-70, -52}, {-50, -52}}, color = {0, 0, 0}, visible = leftBoundaryIncluded), Line(points = {{50, 30}, {70, 30}, {70, -52}, {50, -52}}, color = {0, 0, 0}, visible = rightBoundaryIncluded), Line(points = {{90, 30}, {70, 30}, {70, -52}, {90, -52}}, color = {0, 0, 0}, visible = not rightBoundaryIncluded), Text(extent = {{-40, 2}, {10, -22}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u"), Line(points = {{-50, 10}, {20, 10}}, color = {0, 0, 0}), Line(points = {{-50, -30}, {-50, 10}}, color = {0, 0, 0}), Line(points = {{20, 10}, {20, -30}}, color = {0, 0, 0}), Line(points = {{-52, -30}, {56, -30}}, color = {175, 175, 175}), Line(points = {{40, -30}, {40, 10}}, color = {0, 0, 0}), Line(points = {{52, 10}, {52, -30}}, color = {0, 0, 0}), Line(points = {{40, 10}, {52, 10}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(extent = {{-100, -100}, {100, 100}}, initialScale = 0.1, preserveAspectRatio = false), graphics = {Text(extent = {{-86, 38}, {-72, 22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{14, 38}, {28, 22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{-6, 38}, {8, 22}}, lineColor = {238, 46, 47}, fontName = "Symbol", textString = ""), Text(extent = {{74, 38}, {88, 22}}, lineColor = {238, 46, 47}, fontName = "Symbol", textString = ""), Rectangle(extent = {{-80, 20}, {0, 0}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Rectangle(extent = {{20, 20}, {80, 0}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Text(extent = {{-68, 14}, {-14, 6}}, lineColor = {0, 0, 0}, textString = "Time period"), Text(extent = {{22, 14}, {76, 6}}, lineColor = {0, 0, 0}, textString = "Time period"), Line(points = {{-90, 0}, {-80, 0}, {-80, 20}, {0, 20}, {0, 0}, {20, 0}, {20, 20}, {80, 20}, {80, 0}, {96, 0}}, color = {0, 0, 0}, arrow = {Arrow.None, Arrow.Filled}), Line(points = {{80, 24}, {80, -82}}, color = {0, 0, 0}, pattern = LinePattern.Dot), Text(extent = {{106, 96}, {120, 80}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{122, 96}, {170, 84}}, lineColor = {0, 0, 0}, textString = "Opening event"), Text(extent = {{124, 76}, {172, 64}}, lineColor = {0, 0, 0}, textString = "Closing event"), Text(extent = {{108, 76}, {122, 60}}, lineColor = {238, 46, 47}, fontName = "Symbol", textString = "")}),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>While</b>(<b>u </b>= opening_or_closing_event [, <b>tl </b>= master_time_period]); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The<b> While</span></b> block creates a master time period which is a special case of a time locator with no overlapping time periods. The <b>While</b> block truncates the time periods created with the <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> block or the <b>While</b> block whose <b>tl</b> inputs are connected to the output <b>y</b>.</p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Opening and closing events are respectively delivered on input <b>u</b> when <b>u</b> becomes respectively true and false.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">While can only be used as the master time locator of a <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> block or another <b>While</b> block.</span></p>
</html>"));
      end While;