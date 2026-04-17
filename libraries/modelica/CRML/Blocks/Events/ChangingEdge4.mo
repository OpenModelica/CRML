within CRML.Blocks.Events;

block ChangingEdge4 "Output y is true, if the input u has either a rising (becomes true) or a falling edge (becomes false), otherwise it is false"
        import CRML.ETL.Types.Boolean4;
      protected
        Boolean v1 = (u == Boolean4.false4);
        Boolean v2 = (u == Boolean4.true4);
      public
        ETL.Connectors.Boolean4Input u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}}), iconTransformation(extent = {{100, -10}, {120, 10}})));
      initial equation
        pre(u) = Boolean4.false4;
        pre(v1) = false;
        pre(v2) = false;
      equation
        y = edge(v1) or edge(v2);
        annotation(
          defaultComponentName = "changing1",
          Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -68}, {-36, -68}, {-36, -24}, {22, -24}, {22, -68}, {66, -68}}), Line(points = {{-80, 32}, {-36, 32}, {-36, 76}, {-36, 76}, {-36, 32}, {66, 32}}, color = {255, 0, 255}), Line(points = {{24, 32}, {24, 76}}, color = {255, 0, 255})}),
          Documentation(info = "<html>
<p>
A changing edge, i.e., either rising or falling,
of the Boolean input u results in y = <b>true</b> at this
time instant. At all other time instants, y = <b>false</b>.
</p>

<p>
The usage is demonstrated, e.g., in example
<a href=\"modelica://Modelica.Blocks.Examples.BooleanNetwork1\">Modelica.Blocks.Examples.BooleanNetwork1</a>.
</p>

</html>"),Diagram(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -68}, {-36, -68}, {-36, -24}, {22, -24}, {22, -68}, {66, -68}}), Line(points = {{-80, 32}, {-36, 32}, {-36, 76}, {-36, 76}, {-36, 32}, {66, 32}}, color = {255, 0, 255}), Line(points = {{24, 32}, {24, 76}}, color = {255, 0, 255})}));
      end ChangingEdge4;