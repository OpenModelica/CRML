within CRML.Blocks.Math;

block InsidePolygon4 "Determines whether a point is inside a polygon"
        import CRML.ETL.Types.Boolean4;
        parameter Real polygon[:, 2] = {{-60, 40}, {-30, 54}, {60, 54}, {52, -8}, {12, -54}, {-52, -30}};
      protected
        Real insidePolygon = CRML.ETL.Utilities.isInsidePolygon({x1, x2}, polygon, tolerance);
      public
        ETL.Connectors.Boolean4Output y "True if point is inside polygon" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.RealInput x2 "Second coordinate of the point" annotation(
          Placement(transformation(extent = {{-120, -70}, {-100, -50}}), iconTransformation(extent = {{-120, -70}, {-100, -50}})));
        ETL.Connectors.RealInput x1 "First coordinate of the point" annotation(
          Placement(transformation(extent = {{-120, 50}, {-100, 70}}), iconTransformation(extent = {{-120, 50}, {-100, 70}})));
        ETL.Connectors.RealInput tolerance "Half-width uncertainty range" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
      equation
        if (cardinality(tolerance) == 0) then
          tolerance = 0;
        end if;
        y = if insidePolygon > 0.5 then Boolean4.true4 else if insidePolygon < -0.5 then Boolean4.undecided else Boolean4.false4;
        annotation(
          defaultComponentName = "polygon",
          Icon(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-51, 150}, {51, 114}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Ellipse(extent = {{-24, -6}, {-22, -8}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Polygon(points = {{-60, 40}, {-30, 54}, {60, 54}, {52, -8}, {12, -54}, {-52, -30}, {-60, 40}}, lineColor = {0, 0, 0}), Polygon(points = {{-80, 90}, {-86, 68}, {-74, 68}, {-80, 90}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-80, 68}, {-80, -80}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {82, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid)}),
          Diagram(coordinateSystem(preserveAspectRatio = true, extent = {{-100, -100}, {100, 100}}), graphics = {Ellipse(extent = {{-24, -6}, {-22, -8}}, lineColor = {28, 108, 200}, fillColor = {28, 108, 200}, fillPattern = FillPattern.Solid), Polygon(points = {{-60, 40}, {-30, 54}, {60, 54}, {52, -8}, {12, -54}, {-52, -30}, {-60, 40}}, lineColor = {28, 108, 200}), Text(extent = {{-83, 92}, {-30, 74}}, lineColor = {0, 0, 0}, textString = "x[2]"), Line(points = {{-80, 68}, {-80, -80}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {82, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{-80, 90}, {-86, 68}, {-74, 68}, {-80, 90}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{70, -80}, {94, -100}}, lineColor = {0, 0, 0}, textString = "x[1]")}),
          Documentation(info = "<html>
<p>Returns true if the point is inside the polygon. </p>
<p> </p>
</html>"));
      end InsidePolygon4;