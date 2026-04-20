within CRML.Blocks.Events;

block Event3ToEvent
        import CRML.ETL.Types.Boolean3;
      protected
        Boolean x(start = false, fixed = true) = (u == Boolean3.true3);
      public
        ETL.Connectors.Boolean3Input u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanOutput y(start = false, fixed = true) annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        y = edge(x);
        annotation(
          Icon(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {0, -100}}, fillColor = {213, 255, 170}, fillPattern = FillPattern.Solid, lineColor = {158, 158, 0}), Rectangle(extent = {{0, 100}, {100, -100}}, lineColor = {175, 175, 175}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid), Line(points = {{-58, 0}, {68, 0}, {48, 20}}, color = {0, 0, 0}), Line(points = {{68, 0}, {48, -20}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(initialScale = 0.04), graphics = {Rectangle(extent = {{-100, 100}, {0, -100}}, fillColor = {213, 255, 170}, fillPattern = FillPattern.Solid, lineColor = {158, 158, 0}), Rectangle(extent = {{0, 100}, {100, -100}}, lineColor = {175, 175, 175}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid), Line(points = {{-58, 0}, {68, 0}, {48, 20}}, color = {0, 0, 0}), Line(points = {{68, 0}, {48, -20}}, color = {0, 0, 0})}));
      end Event3ToEvent;