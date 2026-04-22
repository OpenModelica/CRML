within CRML.Blocks.MathInteger;

block IntegerAdd "Output the sum of the two inputs"
        parameter Integer k1 = +1 "Gain of upper input";
        parameter Integer k2 = +1 "Gain of lower input";
        ETL.Connectors.IntegerInput u1 annotation(
          Placement(transformation(extent = {{-120, 50}, {-100, 70}}), iconTransformation(extent = {{-120, 50}, {-100, 70}})));
        ETL.Connectors.IntegerInput u2 annotation(
          Placement(transformation(extent = {{-120, -70}, {-100, -50}})));
        ETL.Connectors.IntegerOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = k1*u1 + k2*u2;
        annotation(
          Documentation(info = "<html>
<p>
This blocks computes output <b>y</b> as <i>sum</i> of the
two input signals <b>u1</b> and <b>u2</b>:
</p>
<pre>
    <b>y</b> = k1*<b>u1</b> + k2*<b>u2</b>;
</pre>
<p>
Example:
</p>
<pre>
     parameter:   k1= +2, k2= -3

  results in the following equations:

     y = 2 * u1 - 3 * u2
</pre>

</html>"),Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {244, 125, 35}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(lineColor = {0, 0, 255}, extent = {{-150, 110}, {150, 150}}, textString = "%name"), Line(points = {{-100, 60}, {-74, 24}, {-44, 24}}, color = {244, 125, 35}), Line(points = {{-100, -60}, {-74, -28}, {-42, -28}}, color = {244, 125, 35}), Ellipse(lineColor = {244, 125, 35}, extent = {{-50, -50}, {50, 50}}), Line(points = {{50, 0}, {100, 0}}, color = {244, 125, 35}), Text(extent = {{-38, -34}, {38, 34}}, textString = "+"), Text(extent = {{-100, 52}, {5, 92}}, textString = "%k1"), Text(extent = {{-100, -92}, {5, -52}}, textString = "%k2")}),
          Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {244, 125, 35}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{50, 0}, {100, 0}}, color = {0, 0, 255}), Line(points = {{-100, 60}, {-74, 24}, {-44, 24}}, color = {244, 125, 35}), Line(points = {{-100, -60}, {-74, -28}, {-42, -28}}, color = {244, 125, 35}), Ellipse(extent = {{-50, 50}, {50, -50}}, lineColor = {0, 0, 127}), Line(points = {{50, 0}, {100, 0}}, color = {0, 0, 127}), Text(extent = {{-36, 38}, {40, -30}}, lineColor = {244, 125, 35}, textString = "+"), Text(extent = {{-100, 52}, {5, 92}}, lineColor = {0, 0, 0}, textString = "k1"), Text(extent = {{-100, -52}, {5, -92}}, lineColor = {0, 0, 0}, textString = "k2")}));
      end IntegerAdd;