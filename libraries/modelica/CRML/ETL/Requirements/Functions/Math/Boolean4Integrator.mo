within CRML.ETL.Requirements.Functions.Math;

block Boolean4Integrator "Boolean4 integrator"
            extends Math.RealFunction(name = "Boolean4 integrator");
            import CRML.ETL.Types.Boolean4;
            import CRML.ETL.Types.FunctionType;
          protected
            Real x(start = 0, fixed = true);
          public
            Connectors.TimeLocatorInput tl annotation(
              Placement(transformation(extent = {{-10, 90}, {10, 110}})));
          equation
            when not tl.timePeriod then
              reinit(x, 0);
            end when;
            der(x) = (if (u == Boolean4.true4) then 1 else 0)*(if tl.timePeriod then 1 else 0);
            y = x;
            ft = FunctionType.monotonicIncreasing;
            annotation(
              Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -102}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Polygon(lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid, points = {{-80.0, 90.0}, {-88.0, 68.0}, {-72.0, 68.0}, {-80.0, 90.0}}), Line(points = {{-80.0, 78.0}, {-80.0, -90.0}}, color = {0, 0, 0}), Line(points = {{-80.0, -80.0}, {80.0, 80.0}}, color = {0, 0, 0}), Text(lineColor = {0, 0, 0}, extent = {{0.0, -70.0}, {60.0, -10.0}}, textString = "I"), Line(points = {{-90.0, -80.0}, {82.0, -80.0}}, color = {0, 0, 0}), Polygon(lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid, points = {{90.0, -80.0}, {68.0, -72.0}, {68.0, -88.0}, {90.0, -80.0}})}),
              Diagram(coordinateSystem(preserveAspectRatio = false)),
              Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>Boolean4Integrator</b>(<b>u</b> = condition, <b>tl</b> = time_period); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p>This blocks computes the accumulated continuous time the Boolean4 condition <b>u </b>is true over time period <b>tl</b>.</p>
<p><br><b><span style=\"font-family: MS Shell Dlg 2;\">Example</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">This block is demonstrated with the following <a href=\"modelica://ReqSysPro.Examples.TimeLocators.Continuous.After\">example</a>:</span></p>
</html>"));
          end Boolean4Integrator;