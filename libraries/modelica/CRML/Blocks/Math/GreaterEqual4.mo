within CRML.Blocks.Math;

block GreaterEqual4 "Output y is true if input u1 is greater or equal than input u2"
        import CRML.ETL.Types.Boolean4;
      public
        ETL.Connectors.RealInput u1 annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.RealInput u2 annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}})));
        ETL.Connectors.Boolean4Output y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.RealInput tolerance "Half-width uncertainty range" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      equation
        if (cardinality(tolerance) == 0) then
          tolerance = 0;
        end if;
        if u1 >= u2 + tolerance then
          y = Boolean4.true4;
        elseif u1 < u2 - tolerance then
          y = Boolean4.false4;
        else
          y = Boolean4.undecided;
        end if;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {0, 0, 0}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {0, 0, 0}), Line(points = {{-54, 22}, {-8, 2}, {-54, -18}}, thickness = 0.5, color = {0, 0, 0}), Line(points = {{-52, -36}, {-6, -14}}, thickness = 0.5)}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{32, 10}, {52, -10}}, lineColor = {0, 0, 0}), Line(points = {{-100, -80}, {42, -80}, {42, 0}}, color = {0, 0, 0}), Line(points = {{-54, 22}, {-8, 2}, {-54, -18}}, thickness = 0.5, color = {0, 0, 0}), Line(points = {{-52, -36}, {-6, -14}}, thickness = 0.5)}));
      end GreaterEqual4;