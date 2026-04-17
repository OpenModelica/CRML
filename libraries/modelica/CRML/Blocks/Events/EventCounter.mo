within CRML.Blocks.Events;

block EventCounter "Counts events on input u"
      protected
        Integer x(start = 0, fixed = true);
      public
        ETL.Connectors.BooleanInput u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.IntegerOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}}), iconTransformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanInput reset "Reset counter to zero" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      algorithm
        when reset then
          x := 0;
        end when;
        when u then
          x := x + 1;
        end when;
        y := x;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-82, 12}, {-62, 12}, {-62, 32}, {-22, 32}, {-22, 52}, {38, 52}, {38, 72}, {68, 72}, {68, 91.9805}, {88, 92}}, color = {244, 125, 35}), Line(points = {{-80, -60}, {-62, -60}, {-62, -60}, {-20, -60}, {-20, -60}, {88, -60}}, color = {255, 0, 255}), Line(points = {{-62, -16}, {-62, -60}}, color = {217, 67, 180}), Line(points = {{-20, -16}, {-20, -60}}, color = {217, 67, 180}), Line(points = {{38, -16}, {38, -60}}, color = {217, 67, 180}), Line(points = {{68, -16}, {68, -60}}, color = {217, 67, 180})}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Line(points = {{-82, 12}, {-62, 12}, {-62, 32}, {-22, 32}, {-22, 52}, {38, 52}, {38, 72}, {68, 72}, {68, 91.981}, {88, 92}}, color = {244, 125, 35}), Line(points = {{-80, -60}, {-62, -60}, {-62, -60}, {-20, -60}, {-20, -60}, {88, -60}}, color = {255, 0, 255}), Line(points = {{-62, -16}, {-62, -60}}, color = {217, 67, 180}), Line(points = {{-20, -16}, {-20, -60}}, color = {217, 67, 180}), Line(points = {{38, -16}, {38, -60}}, color = {217, 67, 180}), Line(points = {{68, -16}, {68, -60}}, color = {217, 67, 180})}));
      end EventCounter;