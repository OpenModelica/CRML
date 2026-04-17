within CRML.Blocks.Events;

block EventFilter "Filters events depending on condition"
      protected
        Boolean x(start = false, fixed = true);
      public
        ETL.Connectors.BooleanInput u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanOutput y(start = false, fixed = true) annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}}), iconTransformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanInput cond "Condition" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 0, origin = {-110, 80})));
      equation
        x = u and cond;
        y = edge(x);
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-80, -60}, {-62, -60}, {-62, -60}, {-20, -60}, {-20, -60}, {88, -60}}, color = {255, 0, 255}), Line(points = {{-62, -16}, {-62, -60}}, color = {217, 67, 180}), Line(points = {{-20, -16}, {-20, -60}}, color = {217, 67, 180}), Line(points = {{38, -16}, {38, -60}}, color = {217, 67, 180}), Line(points = {{68, -16}, {68, -60}}, color = {217, 67, 180}), Line(points = {{-78, 38}, {-50, 38}, {-50, 82}, {48, 82}, {48, 38}, {68, 38}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-80, -60}, {-62, -60}, {-62, -60}, {-20, -60}, {-20, -60}, {88, -60}}, color = {255, 0, 255}), Line(points = {{-62, -16}, {-62, -60}}, color = {217, 67, 180}), Line(points = {{-20, -16}, {-20, -60}}, color = {217, 67, 180}), Line(points = {{38, -16}, {38, -60}}, color = {217, 67, 180}), Line(points = {{68, -16}, {68, -60}}, color = {217, 67, 180}), Line(points = {{-78, 38}, {-50, 38}, {-50, 82}, {48, 82}, {48, 38}, {68, 38}})}));
      end EventFilter;