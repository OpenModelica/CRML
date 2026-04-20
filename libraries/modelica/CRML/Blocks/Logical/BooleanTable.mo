within CRML.Blocks.Logical;

block BooleanTable "Generate a time series signal of type Boolean"
        parameter Boolean y0 = false "Initial output value";
        parameter Boolean option_width = true "true: pulse width is used - false: time instant is used";
        parameter CRML.Units.SI.Time width[:](each min = 0) = {1} "Vector of pulse widths";
        parameter CRML.Units.SI.Time instant[:](each min = 0) = {1} "Vector of time instants";
      protected
        parameter Integer iMax = if option_width then size(width, 1) else size(instant, 1);
        CRML.Units.SI.Time t[iMax];
        Integer i(start = 1, fixed = true);
        discrete Boolean val(start = y0, fixed = true);
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
        when (time >= t[i]) then
          if (i <= iMax) then
            val := not (pre(val));
          end if;
          if (i < iMax) then
            i := pre(i) + 1;
          end if;
        end when;
        y := val;
        annotation(
          Coordsys(extent = [-100, -100; 100, 100], grid = [2, 2], component = [20, 20]),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-60, -74}, {-19, -82}}, lineColor = {0, 0, 0}, textString = "instant"), Line(points = {{-78, -70}, {-40, -70}, {-40, 20}, {20, 20}, {20, -70}, {50, -70}, {50, 20}, {100, 20}}, color = {0, 0, 255}, thickness = 0.5), Line(points = {{-40, 61}, {-40, 21}}, color = {95, 95, 95}), Line(points = {{20, 44}, {20, 20}}, color = {95, 95, 95}), Line(points = {{50, 58}, {50, 20}}, color = {95, 95, 95}), Line(points = {{-40, 35}, {20, 35}}, color = {95, 95, 95}), Text(extent = {{-33, 47}, {14, 37}}, lineColor = {0, 0, 0}, textString = "width"), Line(points = {{-70, 20}, {-41, 20}}, color = {95, 95, 95}), Polygon(points = {{-40, 35}, {-31, 37}, {-31, 33}, {-40, 35}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Polygon(points = {{20, 35}, {12, 37}, {12, 33}, {20, 35}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Text(extent = {{-95, 26}, {-66, 17}}, lineColor = {0, 0, 0}, textString = "true"), Text(extent = {{-96, -60}, {-75, -69}}, lineColor = {0, 0, 0}, textString = "false"), Polygon(points = {{-70, 92}, {-76, 70}, {-64, 70}, {-70, 92}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid), Line(points = {{-70, 70}, {-70, -88}}, color = {95, 95, 95}), Line(points = {{-90, -70}, {68, -70}}, color = {95, 95, 95}), Polygon(points = {{90, -70}, {68, -64}, {68, -76}, {90, -70}}, lineColor = {95, 95, 95}, fillColor = {95, 95, 95}, fillPattern = FillPattern.Solid)}),
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name"), Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Polygon(points = {{-80, 88}, {-88, 66}, {-72, 66}, {-80, 88}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Line(points = {{-80, 66}, {-80, -82}}, color = {255, 0, 255}), Line(points = {{-90, -70}, {72, -70}}, color = {255, 0, 255}), Polygon(points = {{90, -70}, {68, -62}, {68, -78}, {90, -70}}, lineColor = {255, 0, 255}, fillColor = {255, 0, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{71, 7}, {85, -7}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, 60}, {30, 30}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, 30}, {30, 0}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, 0}, {30, -30}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Rectangle(extent = {{-50, -30}, {30, -60}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Window(x = 0.1, y = 0.22, width = 0.6, height = 0.6),
          Documentation(info = "<html>
<h4>Adapted from the Modelica.Blocks.Sources library </h4>
<p><b>Version 1.0</b> </p>
</html>"));
      end BooleanTable;