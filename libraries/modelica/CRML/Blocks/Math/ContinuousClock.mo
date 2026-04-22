within CRML.Blocks.Math;

block ContinuousClock "Generates a continuous clock signal"
        parameter Real p_start = 0 "Clock start instant (in seconds)";
      protected
        Real time0;
        Real t0;
      public
        ETL.Connectors.RealOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.RealInput start "Clock start instant (in seconds)" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanInput reset "Clock reset to zero" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      initial equation
        pre(reset) = false;
        time0 = start;
      equation
        if (cardinality(start) == 0) then
          start = p_start;
        end if;
        if (cardinality(reset) == 0) then
          reset = false;
        end if;
        when initial() then
          t0 = start;
        end when;
        when reset and time > t0 then
          time0 = time;
        end when;
        y = if (time > time0) then time - time0 else 0;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}, initialScale = 0.06), graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}), Ellipse(extent = {{9, -10}, {-11, 10}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-1, 0}, {52, 50}}, color = {0, 0, 0}), Rectangle(extent = {{80, 6}, {100, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{-100, 6}, {-80, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, -120}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {80, 6}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {-6, 80}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {122, -48}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {-52, -118}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, 60}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0})}),
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
</html>"),Diagram(graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, lineColor = {0, 0, 0}), Ellipse(extent = {{9, -10}, {-11, 10}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-1, 0}, {52, 50}}, color = {0, 0, 0}), Rectangle(extent = {{80, 6}, {100, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{-100, 6}, {-80, -6}}, fillPattern = FillPattern.Solid, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, -120}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {80, 6}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {-6, 80}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 135, origin = {122, -48}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 45, origin = {-52, -118}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0}), Rectangle(extent = {{20, 58}, {40, 46}}, fillPattern = FillPattern.Solid, rotation = 90, origin = {52, 60}, lineColor = {95, 95, 95}, fillColor = {0, 0, 0})}));
      end ContinuousClock;