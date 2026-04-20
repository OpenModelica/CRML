within CRML.Blocks.Logical4;
block Implies4 "Implies operator on two Boolean4: u1 implies u2"
  ETL.Connectors.Boolean4Input u1 annotation(
    Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
  ETL.Connectors.Boolean4Input u2 annotation(
    Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
  ETL.Connectors.Boolean4Output y annotation(
    Placement(transformation(extent = {{100, -10}, {120, 10}})));
equation
  y = implies4(u1, u2);
  annotation(
    Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-104, 36}, {62, -22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {162, 29, 33}), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {162, 29, 33}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid)}),
    Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-104, 36}, {62, -22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {162, 29, 33}), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {162, 29, 33}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid)}));
end Implies4;

function implies4 "Boolean4 implies operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
algorithm
        y := or4(not4(x1), x2);
end implies4;
