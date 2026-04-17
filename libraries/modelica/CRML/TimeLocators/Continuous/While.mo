within CRML.TimeLocators.Continuous;

block While "While time locator"
        import CRML.ETL.Types.Boolean4;
        parameter Boolean leftBoundaryIncluded = true "If true, the left boundaries of the time periods are included";
        parameter Boolean rightBoundaryIncluded = true "If true, the right boundaries of the time periods are included";
      protected
        ETL.Connectors.WhileOutput tl1 = tl;
      public
        ETL.Connectors.WhileOutput y "Vector of time periods" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100}), iconTransformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100})));
        ETL.Connectors.Boolean4Input u "Alternating opening and closing events" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
      public
        ETL.Connectors.WhileInput tl annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
        ETL.TimeLocators.While While(leftBoundaryIncluded = leftBoundaryIncluded, rightBoundaryIncluded = rightBoundaryIncluded) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}})));
      equation
        if (cardinality(tl) == 0) then
          tl.timePeriod = true;
          tl.clock = Boolean4.true4;
          tl.isLeftBoundaryIncluded = true;
          tl.isRightBoundaryIncluded = true;
        end if;
        connect(u, While.u) annotation(
          Line(points = {{-110, 0}, {-11, 0}}, color = {162, 29, 33}));
        connect(tl1, While.tl) annotation(
          Line(points = {{0, 100}, {0, 10}}, color = {0, 0, 255}));
        connect(While.y, y) annotation(
          Line(points = {{0, -10}, {0, -100}}, color = {0, 0, 255}));
        annotation(
          Icon(coordinateSystem(extent = {{-100, -100}, {100, 100}}, initialScale = 0.1, preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {85, 170, 255}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Rectangle(extent = {{-70, 30}, {70, -52}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-78, 88}, {76, 40}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "while"), Line(points = {{-90, 30}, {-70, 30}, {-70, -52}, {-90, -52}}, color = {0, 0, 0}, visible = not leftBoundaryIncluded), Line(points = {{-50, 30}, {-70, 30}, {-70, -52}, {-50, -52}}, color = {0, 0, 0}, visible = leftBoundaryIncluded), Line(points = {{50, 30}, {70, 30}, {70, -52}, {50, -52}}, color = {0, 0, 0}, visible = rightBoundaryIncluded), Line(points = {{90, 30}, {70, 30}, {70, -52}, {90, -52}}, color = {0, 0, 0}, visible = not rightBoundaryIncluded), Text(extent = {{-40, 2}, {10, -22}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u"), Line(points = {{-50, 10}, {20, 10}}, color = {0, 0, 0}), Line(points = {{-50, -30}, {-50, 10}}, color = {0, 0, 0}), Line(points = {{20, 10}, {20, -30}}, color = {0, 0, 0}), Line(points = {{-52, -30}, {56, -30}}, color = {175, 175, 175}), Line(points = {{40, -30}, {40, 10}}, color = {0, 0, 0}), Line(points = {{52, 10}, {52, -30}}, color = {0, 0, 0}), Line(points = {{40, 10}, {52, 10}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(extent = {{-100, -100}, {100, 100}}, initialScale = 0.1, preserveAspectRatio = false)),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>While</b>(<b>u </b>= opening_or_closing_event [, <b>tl </b>= master_time_period]); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">While</span></b> is a special case of a continuous time locator with no overlapping time periods.</p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Opening and closing events are respectively delivered on input <b>u</b> when <b>u</b> becomes respectively true and false.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">For more information, refer to the <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> block.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">While can only be used as the master time locator of a <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> block.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The <a href=\"modelica://CRML.TimeLocators.Continuous.Master\">Master</a> block can be used to specify the same master period to all time periods of the same model.</span></p>
</html>"));
      end While;