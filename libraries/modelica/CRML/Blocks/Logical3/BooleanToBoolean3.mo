within CRML.Blocks.Logical3;

block BooleanToBoolean3
        ETL.Connectors.BooleanInput u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.Boolean3Output y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = ETL.Types.cvBooleanToBoolean3(u);
        annotation(
          Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{0, 100}, {100, -100}}, fillColor = {213, 255, 170}, fillPattern = FillPattern.Solid, lineColor = {127, 127, 0}), Rectangle(extent = {{-100, 100}, {0, -100}}, lineColor = {175, 175, 175}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid), Line(points = {{-58, 0}, {68, 0}, {48, 20}}, color = {0, 0, 0}), Line(points = {{68, 0}, {48, -20}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{0, 100}, {100, -100}}, fillColor = {213, 255, 170}, fillPattern = FillPattern.Solid, lineColor = {127, 127, 0}), Rectangle(extent = {{-100, 100}, {0, -100}}, lineColor = {175, 175, 175}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid), Line(points = {{-58, 0}, {68, 0}, {48, 20}}, color = {0, 0, 0}), Line(points = {{68, 0}, {48, -20}}, color = {0, 0, 0})}));
      end BooleanToBoolean3;