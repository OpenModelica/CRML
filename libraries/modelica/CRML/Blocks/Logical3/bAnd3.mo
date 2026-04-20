within CRML.Blocks.Logical3;

block bAnd3 "And operator on two Boolean3: u1 or u2"
  ETL.Connectors.Boolean3Input u1 annotation(
    Placement(transformation(extent = {{-120, 70}, {-100, 90}}), iconTransformation(extent = {{-120, 70}, {-100, 90}})));
  ETL.Connectors.Boolean3Input u2 annotation(
    Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
  ETL.Connectors.Boolean3Output y annotation(
    Placement(transformation(extent = {{100, -10}, {120, 10}})));
equation 
  y = Logical3.and3(u1, u2);
  annotation(
    Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {213, 255, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "and")}),
    Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {213, 255, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "and")}));
end bAnd3;
