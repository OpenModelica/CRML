within CRML.Blocks.Logical4;
block ShowBoolean4 "Show Boolean4 in diagram layer dynamically"
        import CRML.ETL.Types.Boolean4;
        output Boolean4 y;
      public
        ETL.Connectors.Boolean4Input u annotation(
          Placement(transformation(extent = {{-220, -10}, {-200, 10}}), iconTransformation(extent = {{-220, -10}, {-200, 10}})));
equation
        y = u;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-200, -100}, {200, 100}}, initialScale = 0.1), graphics = {Rectangle(lineColor = {0, 0, 127}, fillColor = {255, 213, 170}, fillPattern = FillPattern.Solid, lineThickness = 5, borderPattern = BorderPattern.Raised, extent = {{-200, -40}, {200, 40}}), Text(extent = {{-76, -20}, {76, 26}}, textString = DynamicSelect("-------", if Integer(u) == 1 then "undefined" else if Integer(u) == 2 then "undecided" else if Integer(u) == 3 then "false" else if Integer(u) == 4 then "true" else "????"))}),
          Documentation(info = "<html>
<p>
This block visualizes an Integer number in a diagram animation.
The number to be visualized can be defined in the following ways:
</p>

<ul>
<li> If useNumberPort = <b>true</b> (which is the default), an Integer
     input is present and this input variable is shown.</li>

<li> If useNumberPort = <b>false</b> no input connector is present.
     Instead, an Integer input field is activated in the parameter menu
     and the Integer expression from this input menu is shown.</li>
</ul>

<p>
The two versions of the block are shown in the following image (in the right variant, the
name of the variable value that is displayed is also shown below the icon):
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Interaction/IntegerValue.png\"
     alt=\"IntegerValue.png\">
</p>

<p>
The usage is demonstrated, e.g., in example
<a href=\"modelica://Modelica.Blocks.Examples.IntegerNetwork1\">Modelica.Blocks.Examples.IntegerNetwork1</a>.
</p>
</html>"),Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-200, -100}, {200, 100}}, initialScale = 0.1), graphics = {Rectangle(lineColor = {0, 0, 127}, fillColor = {255, 213, 170}, fillPattern = FillPattern.Solid, lineThickness = 5, borderPattern = BorderPattern.Raised, extent = {{-200, -40}, {200, 40}}), Text(extent = {{-76, -20}, {76, 26}}, textString = DynamicSelect("-------", if Integer(u) == 1 then "undefined" else if Integer(u) == 2 then "undecided" else if Integer(u) == 3 then "false" else if Integer(u) == 4 then "true" else "????"))}));
end ShowBoolean4;
