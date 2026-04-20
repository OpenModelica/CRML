within CRML.Blocks.Logical4;
block Not4 "Not operator on a Boolean4: not u"
  ETL.Connectors.Boolean4Input u annotation(
    Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
  ETL.Connectors.Boolean4Output y annotation(
    Placement(transformation(extent = {{100, -10}, {120, 10}})));
equation
  y = not4(u);
  annotation(
    Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "not")}),
    Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "not")}));
end Not4;

function not4 "Boolean4 not operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x;
        output Boolean4 y;
algorithm
        y := TruthTables.not4[Integer(x)];
end not4;
