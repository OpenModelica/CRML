within CRML.TimeLocators.Continuous;

block Master "Master time locator"
        import CRML.ETL.Types.Boolean4;
        parameter Boolean leftBoundaryIncluded = true "If true, the left boundaries of the time periods are included";
        parameter Boolean rightBoundaryIncluded = true "If true, the right boundaries of the time periods are included";
        parameter Boolean defaultInputValue = true "Default input value when input u is not connected";
        ETL.Connectors.WhileOutput y "Vector of time periods";
        ETL.Connectors.Boolean4Input u "Alternating opening and closing events" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
      equation
        if (cardinality(u) == 0) then
          u = CRML.ETL.Types.cvBooleanToBoolean4(defaultInputValue);
        end if;
        y.timePeriod = (u == Boolean4.true4);
        y.clock = Boolean4.true4;
        y.isLeftBoundaryIncluded = leftBoundaryIncluded;
        y.isRightBoundaryIncluded = rightBoundaryIncluded;
        //           borderPattern=BorderPattern.Raised,
        //           borderPattern=BorderPattern.Raised,
        annotation(
          defaultComponentName = "master",
          defaultComponentPrefixes = "inner",
          missingInnerMessage = "
Your model is using an outer \"master\" component but
an inner \"master\" component is not defined and therefore
a default inner \"master\" component is introduced by the tool.
To change the default setting, drag block CRML.TimeLocators.Continuous.Master
into your model.
          ",
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 127}, fillColor = {85, 170, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-150, 150}, {150, 110}}, textString = "%name", lineColor = {0, 0, 255}), Line(points = {{-50, 38}, {-70, 38}, {-70, -44}, {-50, -44}}, color = {0, 0, 0}, visible = leftBoundaryIncluded), Line(points = {{50, 38}, {70, 38}, {70, -44}, {50, -44}}, color = {0, 0, 0}, visible = rightBoundaryIncluded), Line(points = {{90, 38}, {70, 38}, {70, -44}, {90, -44}}, color = {0, 0, 0}, visible = not rightBoundaryIncluded), Line(points = {{-90, 38}, {-70, 38}, {-70, -44}, {-90, -44}}, color = {0, 0, 0}, visible = not leftBoundaryIncluded), Line(points = {{-50, -22}, {-50, 18}}, color = {0, 0, 0}), Line(points = {{-50, 18}, {20, 18}}, color = {0, 0, 0}), Text(extent = {{-40, 10}, {10, -14}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u"), Line(points = {{-52, -22}, {56, -22}}, color = {175, 175, 175}), Line(points = {{20, 10}, {20, -30}}, color = {0, 0, 0}), Line(points = {{40, -22}, {40, 18}}, color = {0, 0, 0}), Line(points = {{40, 18}, {52, 18}}, color = {0, 0, 0}), Line(points = {{52, 18}, {52, -22}}, color = {0, 0, 0})}),
          Documentation(revisions = "<html>
</html>", info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>Master</b>(<b>u </b>= opening_or_closing_event); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Master</span></b> is a special case of a continuous time locator with no overlapping time periods.</p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Opening and closing events are respectively delivered on input <b>u</b> when <b>u</b> becomes respectively true and false.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">For more information, refer to the <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> block.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">This block can be used as the mastertime  period of all <a href=\"modelica://CRML.ETL.TimeLocators.Periods\">Periods</a> blocks within a given model by just dragging and dropping the block into the model. Only one master block per model is allowed.</span></p>
</html>"),Diagram(graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 127}, fillColor = {85, 170, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-150, 150}, {150, 110}}, textString = "%name", lineColor = {0, 0, 255}), Line(points = {{-50, 38}, {-70, 38}, {-70, -44}, {-50, -44}}, color = {0, 0, 0}, visible = leftBoundaryIncluded), Line(points = {{50, 38}, {70, 38}, {70, -44}, {50, -44}}, color = {0, 0, 0}, visible = rightBoundaryIncluded), Line(points = {{90, 38}, {70, 38}, {70, -44}, {90, -44}}, color = {0, 0, 0}, visible = not rightBoundaryIncluded), Line(points = {{-90, 38}, {-70, 38}, {-70, -44}, {-90, -44}}, color = {0, 0, 0}, visible = not leftBoundaryIncluded), Line(points = {{-50, -22}, {-50, 18}}, color = {0, 0, 0}), Line(points = {{-50, 18}, {20, 18}}, color = {0, 0, 0}), Text(extent = {{-40, 10}, {10, -14}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u"), Line(points = {{-52, -22}, {56, -22}}, color = {175, 175, 175}), Line(points = {{20, 10}, {20, -30}}, color = {0, 0, 0}), Line(points = {{40, -22}, {40, 18}}, color = {0, 0, 0}), Line(points = {{40, 18}, {52, 18}}, color = {0, 0, 0}), Line(points = {{52, 18}, {52, -22}}, color = {0, 0, 0})}));
      end Master;