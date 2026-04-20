within CRML.ETL.TimeLocators.Attributes;

block WhileTimePeriod "Extracts the time period of a while locator"
          ETL.Connectors.BooleanOutput y annotation(
            Placement(transformation(extent = {{100, -10}, {120, 10}})));
          ETL.Connectors.WhileInput tl annotation(
            Placement(transformation(extent = {{-10, 90}, {10, 110}}), iconTransformation(extent = {{-10, 90}, {10, 110}})));
        equation
          y = tl.timePeriod;
          annotation(
            Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-86, -24}, {-50, -24}, {-50, 20}, {60, 20}, {60, -26}, {88, -26}}, color = {0, 0, 0}), Rectangle(extent = {{-66, -46}, {0, -62}}, lineColor = {0, 0, 0}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, radius = 0), Rectangle(extent = {{0, -46}, {66, -62}}, lineColor = {0, 0, 0}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, radius = 0)}),
            Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-86, -24}, {-50, -24}, {-50, 20}, {60, 20}, {60, -26}, {88, -26}}, color = {0, 0, 0}), Rectangle(extent = {{-66, -46}, {0, -62}}, lineColor = {0, 0, 0}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, radius = 0), Rectangle(extent = {{0, -46}, {66, -62}}, lineColor = {0, 0, 0}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, radius = 0)}));
        end WhileTimePeriod;