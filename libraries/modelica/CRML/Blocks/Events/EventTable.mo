within CRML.Blocks.Events;

block EventTable "Table for events"
        parameter Boolean option_width = true "true: width is used - false: time instant is used";
        parameter CRML.Units.SI.Time width[:](each min = 0) = {1} "Vector of widths";
        parameter CRML.Units.SI.Time instant[:](each min = 0) = {1} "Vector of time instants";
      protected
        parameter Integer iMax = if option_width then size(width, 1) else size(instant, 1);
        CRML.Units.SI.Time t[iMax];
        Integer i(start = 1, fixed = true);
        discrete Boolean val(start = false, fixed = true);
      public
        ETL.Connectors.BooleanOutput y annotation(
          extent = [100, -10; 120, 10],
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      algorithm
        for i in 1:iMax loop
          if option_width then
            assert(width[i] > 0, "Width must be strictly positive");
          else
            assert(instant[1] > 0, "First instant must be strictly positive");
            if (i > 1) then
              assert(instant[i] > instant[i - 1], "Instants time series must be stricly increasing");
            end if;
          end if;
        end for;
        t[1] := if option_width then width[1] else instant[1];
        for i in 2:iMax loop
          t[i] := if option_width then t[i - 1] + width[i] else instant[i];
        end for;
        when (time > t[i]) then
          if (i <= iMax) then
            val := not (pre(val));
          end if;
          if (i < iMax) then
            i := pre(i) + 1;
          end if;
        end when;
        y := change(val);
        annotation(
          Coordsys(extent = [-100, -100; 100, 100], grid = [2, 2], component = [20, 20]),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-60, -74}, {-19, -82}}, lineColor = {0, 0, 0}, textString = "instant"), Line(points = {{-70, 20}, {-41, 20}}, color = {95, 95, 95}), Text(extent = {{-95, 26}, {-66, 17}}, lineColor = {0, 0, 0}, textString = "true"), Text(extent = {{-96, -60}, {-75, -69}}, lineColor = {0, 0, 0}, textString = "false"), Polygon(points = {{-70, 92}, {-76, 70}, {-64, 70}, {-70, 92}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-70, 70}, {-70, -88}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {68, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-40, 20}, {-40, -70}}, color = {28, 108, 200}, thickness = 0.5), Line(points = {{0, 20}, {0, -70}}, color = {28, 108, 200}, thickness = 0.5), Line(points = {{60, 20}, {60, -70}}, color = {28, 108, 200}, thickness = 0.5)}),
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Polygon(points = {{-80, 88}, {-88, 66}, {-72, 66}, {-80, 88}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 66}, {-80, -82}}, color = {255, 0, 255}), Line(points = {{-90, -70}, {72, -70}}, color = {255, 0, 255}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, 60}, {30, 30}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, 30}, {30, 0}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, 0}, {30, -30}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, -30}, {30, -60}}, lineColor = {0, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid)}),
          Window(x = 0.1, y = 0.22, width = 0.6, height = 0.6),
          Documentation(info = "<html>
<h4>Adapted from the Modelica.Blocks.Sources library </h4>
<p><b>Version 1.0</b> </p>
</html>"));
      end EventTable;