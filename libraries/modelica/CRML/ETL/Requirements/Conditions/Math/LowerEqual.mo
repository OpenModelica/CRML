within CRML.ETL.Requirements.Conditions.Math;

block LowerEqual "Output y is true, if u is lower or equal than threshold"
            extends Math.RealCondition(name = "<=");
            import CRML.ETL.Types.FunctionType;
          equation
            y = CRML.ETL.Types.cvBooleanToBoolean4(u <= threshold);
            if ft == FunctionType.monotonicIncreasing then
              decisionEvent = not (u <= threshold);
            elseif ft == FunctionType.monotonicDecreasing then
              decisionEvent = (u <= threshold);
            else
              decisionEvent = false;
            end if;
            annotation(
              Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {0, 0, 0}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {0, 0, 0}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-6, 18}, {-50, -2}, {-6, -20}}, thickness = 0.5, color = {0, 0, 0}), Line(points = {{-50, -18}, {-10, -34}}, thickness = 0.5, color = {0, 0, 0})}),
              Documentation(info = "<html>
<p>
The output is <b>true</b> if Real input u1 is greater than
Real input u2, otherwise the output is <b>false</b>.
</p>
</html>"));
          end LowerEqual;