within CRML.Blocks.Events;

block ClockEvent "Generates a clock signal when the Boolean input changes from false to true"
      protected
        Boolean z;
      public
        ETL.Connectors.BooleanInput u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.ClockOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      initial equation
        pre(u) = false;
      equation
        z = edge(u);
        y = Clock(z);
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, initialScale = 0.06), graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineColor = {0, 140, 72}), Ellipse(extent = {{9, -10}, {-11, 10}}, lineColor = {0, 140, 72}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-1, 0}, {52, 50}}, color = {0, 140, 72}), Rectangle(extent = {{80, 6}, {100, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{-100, 6}, {-80, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, -120}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {80, 6}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {-6, 80}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {122, -48}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, 60}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {-52, -118}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Documentation(info = "<html>
<p>
This component generates a clock signal triggered by a continuous-time
Boolean input signal u: Whenever the Boolean input signal <b>u</b>
changes from <b>false</b> to <b>true</b>, then the output
clock signal <b>y</b> ticks.
</p>

<p>
For an introduction to clocks see
<a href=\"modelica://Modelica_Synchronous.UsersGuide.Clocks\">UsersGuide.Clocks</a>.
</p>

<p>
If a clock is associated to a clocked continuous-time partition, then an <b>integrator</b>
has to be defined that is used to integrate the partition from the previous
to the current clock tick. This is performed by setting parameter <b>useSolver</b>
= <b>true</b> and defining the integration method as String with
parameter <b>solver</b>. Both parameters are in tab <b>Advanced</b>.
For an example, see
<a href=\"modelica://Modelica_Synchronous.Examples.Systems.ControlledMixingUnit\">Examples.Systems.ControlledMixingUnit</a>.
</p>
</html>"),Diagram(graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineColor = {0, 140, 72}), Ellipse(extent = {{9, -10}, {-11, 10}}, lineColor = {0, 140, 72}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-1, 0}, {52, 50}}, color = {0, 140, 72}), Rectangle(extent = {{80, 6}, {100, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{-100, 6}, {-80, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, -120}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {80, 6}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {-6, 80}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {122, -48}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, 60}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {-52, -118}, lineColor = {95, 95, 95}, fillColor = {0, 140, 72}), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}));
      end ClockEvent;