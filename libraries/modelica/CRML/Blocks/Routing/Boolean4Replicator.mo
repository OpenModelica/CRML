within CRML.Blocks.Routing;

block Boolean4Replicator "Boolean4 signal replicator"
        parameter Integer nout = 1 "Number of outputs";
        ETL.Connectors.Boolean4Input u "Connector of Boolean input signal" annotation(
          Placement(transformation(extent = {{-140, -20}, {-100, 20}})));
        ETL.Connectors.Boolean4Output y[nout] "Connector of Boolean output signals" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = fill(u, nout);
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {162, 29, 33}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-100, 0}, {-6, 0}}, color = {162, 29, 33}), Line(points = {{100, 0}, {10, 0}}, color = {162, 29, 33}), Line(points = {{0, 0}, {100, 10}}, color = {162, 29, 33}), Line(points = {{0, 0}, {100, -10}}, color = {162, 29, 33}), Ellipse(extent = {{-14, 14}, {16, -16}}, lineColor = {217, 67, 180}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid)}),
          Documentation(info = "<html>
<p>
This block replicates the Boolean input signal to an array of <code>nout</code> identical Boolean output signals.
</p>
</html>"),Diagram(graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {162, 29, 33}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-100, 0}, {-6, 0}}, color = {162, 29, 33}), Line(points = {{100, 0}, {10, 0}}, color = {162, 29, 33}), Line(points = {{0, 0}, {100, 10}}, color = {162, 29, 33}), Line(points = {{0, 0}, {100, -10}}, color = {162, 29, 33}), Ellipse(extent = {{-14, 14}, {16, -16}}, lineColor = {217, 67, 180}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid)}));
      end Boolean4Replicator;