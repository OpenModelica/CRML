within CRML.TimeLocators.Continuous;
block During
        import CRML.ETL.Types.Boolean4;
        parameter Boolean leftBoundaryIncluded = true "If true, the left boundaries of the time periods are included";
        parameter Boolean rightBoundaryIncluded = true "If true, the right boundaries of the time periods are included";
      protected
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
        ETL.Connectors.WhileOutput tl1 = tl;
      public
        ETL.Connectors.Boolean4Input u "Opening and closing events" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.WhileInput tl "Master time period" annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
      public
        ETL.Connectors.TimeLocatorOutput[N] y(timePeriod(each fixed = true, each start = false)) "Vector of time periods" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100}), iconTransformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100})));
        ETL.TimeLocators.Periods periods(durationSpecified = false, leftBoundaryIncluded = leftBoundaryIncluded, rightBoundaryIncluded = rightBoundaryIncluded) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}})));
        Blocks.Logical4.bNot4 not4_1
                                    annotation(
          Placement(transformation(extent = {{-60, -40}, {-40, -20}})));
equation
        if (cardinality(tl) == 0) then
          tl.timePeriod = true;
          tl.clock = Boolean4.true4;
          tl.isLeftBoundaryIncluded = true;
          tl.isRightBoundaryIncluded = true;
        end if;
        connect(u, periods.u1) annotation(
          Line(points = {{-110, 0}, {-11, 0}}, color = {162, 29, 33}));
        connect(tl1, periods.tl) annotation(
          Line(points = {{0, 100}, {0, 10}}, color = {0, 0, 255}));
        connect(periods.y, y) annotation(
          Line(points = {{0, -10}, {0, -100}}, color = {0, 0, 255}));
        connect(not4_1.y, periods.u2) annotation(
          Line(points = {{-39, -30}, {-20, -30}, {-20, -8}, {-11, -8}}, color = {162, 29, 33}));
        connect(u, not4_1.u) annotation(
          Line(points = {{-110, 0}, {-80, 0}, {-80, -30}, {-61, -30}}, color = {162, 29, 33}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {85, 170, 255}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Rectangle(extent = {{-70, 30}, {70, -52}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-50, -34}, {-50, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-48, -12}, {52, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{52, -12}, {52, -34}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-62, -34}, {58, -34}}, color = {175, 175, 175}), Line(points = {{-50, -12}, {-50, 10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Rectangle(extent = {{22, 2}, {52, -12}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Line(points = {{-50, 10}, {22, 10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, 10}, {22, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, -12}, {20, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, -12}, {24, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Text(extent = {{-78, 86}, {76, 38}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "during"), Line(points = {{-50, -12}, {-52, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, -12}, {-48, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, 30}, {-70, 30}, {-70, -52}, {-50, -52}}, color = {0, 0, 0}, visible = periods.leftBoundaryIncluded), Line(points = {{50, 30}, {70, 30}, {70, -52}, {50, -52}}, color = {0, 0, 0}, visible = periods.rightBoundaryIncluded), Line(points = {{90, 30}, {70, 30}, {70, -52}, {90, -52}}, color = {0, 0, 0}, visible = not periods.rightBoundaryIncluded), Line(points = {{-90, 30}, {-70, 30}, {-70, -52}, {-90, -52}}, color = {0, 0, 0}, visible = not periods.leftBoundaryIncluded), Text(extent = {{-28, -10}, {22, -34}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u")}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>During</b>(<b>u</b> = Boolean [, <b>tl </b>= master_time_period]); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p>Returns the time periods that start when the Boolean on <b>u</b> becomes true and end when the Boolean on <b>u</b> becomes false. The opening and the closing events are included or excluded according to the values of the parameters <code>leftBoundaryIncluded </code>and<code> rightBoundaryIncluded</code>. The time periods are truncated by the master_time_period if provided on <b>tl</b>.</p>
<p><span style=\"font-family: MS Shell Dlg 2;\">For more information, refer to the <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> block.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The <a href=\"modelica://CRML.TimeLocators.Continuous.Master\">Master</a> block can be used to specify the same master period to all time periods of the same model.</span></p>
</html>"));
end During;
