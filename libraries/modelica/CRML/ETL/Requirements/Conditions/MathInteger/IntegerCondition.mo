within CRML.ETL.Requirements.Conditions.MathInteger;

partial block IntegerCondition
            parameter String name = "Model name";
            Connectors.IntegerInput u annotation(
              Placement(transformation(extent = {{-120, 70}, {-100, 90}})));
            Connectors.IntegerInput threshold annotation(
              Placement(transformation(extent = {{-120, -90}, {-100, -70}})));
            Connectors.Boolean4Output y annotation(
              Placement(transformation(extent = {{100, -10}, {120, 10}})));
            Connectors.BooleanOutput decisionEvent annotation(
              Placement(transformation(extent = {{100, 70}, {120, 90}})));
            Connectors.FunctionTypeInput ft "Function type (monotonic increasing, monotonic decreasing, non monotonic)" annotation(
              Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
            annotation(
              Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {244, 125, 35}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {244, 125, 35}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
              Documentation(info = "<html>
<p>
The output is <b>true</b> if Real input u1 is greater than
Real input u2, otherwise the output is <b>false</b>.
</p>
</html>"));
          end IntegerCondition;