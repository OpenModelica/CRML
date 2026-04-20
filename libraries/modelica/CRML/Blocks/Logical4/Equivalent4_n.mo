within CRML.Blocks.Logical4;
block Equivalent4 "Equivalent operator on two Boolean4: u1 equivalent to u2"
  ETL.Connectors.Boolean4Input u1 annotation(
    Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
  ETL.Connectors.Boolean4Input u2 annotation(
    Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
  ETL.Connectors.Boolean4Output y annotation(
    Placement(transformation(extent = {{100, -10}, {120, 10}})));
equation
  y = equivalent4(u1, u2);
  annotation(
    Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-106, 36}, {60, -22}}, lineColor = {0, 0, 0}, fontName = "Symbol", textString = ""), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {162, 29, 33}), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {162, 29, 33}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid)}),
    Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-106, 36}, {60, -22}}, lineColor = {0, 0, 0}, fontName = "Symbol", textString = ""), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {162, 29, 33}), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {162, 29, 33}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid)}));
end Equivalent4;