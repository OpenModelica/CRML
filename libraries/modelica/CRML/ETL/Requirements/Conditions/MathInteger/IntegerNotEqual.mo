within CRML.ETL.Requirements.Conditions.MathInteger;

block IntegerNotEqual "Output y is true, if u is not equal to threshold"
            extends MathInteger.IntegerCondition(name = "<>");
            import CRML.ETL.Types.FunctionType;
          equation
            y = CRML.ETL.Types.cvBooleanToBoolean4(u <> threshold);
            if ft == FunctionType.monotonicIncreasing then
              decisionEvent = (u > threshold);
            elseif ft == FunctionType.monotonicDecreasing then
              decisionEvent = (u < threshold);
            else
              decisionEvent = false;
            end if;
            annotation(
              Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {244, 125, 35}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {244, 125, 35}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-50, 10}, {-2, 10}}, color = {244, 125, 35}), Line(points = {{-50, -10}, {-2, -10}}, color = {244, 125, 35}), Line(points = {{-44, -22}, {-8, 22}}, color = {244, 125, 35})}),
              Documentation(info = "<html>
<p>
The output is <b>true</b> if Real input u1 is greater than
Real input u2, otherwise the output is <b>false</b>.
</p>
</html>"));
          end IntegerNotEqual;