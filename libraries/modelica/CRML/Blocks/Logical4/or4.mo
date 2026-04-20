within CRML.Blocks.Logical4;
block Or4 "Or operator to on two Boolean4: u1 or u2"
  ETL.Connectors.Boolean4Input u1 annotation(
    Placement(transformation(extent = {{-120, 70}, {-100, 90}}), iconTransformation(extent = {{-120, 70}, {-100, 90}})));
  ETL.Connectors.Boolean4Input u2 annotation(
    Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
  ETL.Connectors.Boolean4Output y annotation(
    Placement(transformation(extent = {{100, -10}, {120, 10}})));
equation
  y = or4(u1, u2);
  annotation(
    Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "or")}),
    Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "or")}));
end Or4;

function or4 "Boolean4 or operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
algorithm
        y := not4(and4(not4(x1), not4(x2)));
end or4;
