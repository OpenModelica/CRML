within CRML.ETL.Requirements.Functions.MathInteger;

partial block IntegerFunction
            import CRML.ETL.Types.Boolean4;
            parameter String name = "Model name";
          public
            Connectors.Boolean4Input u(start = Boolean4.false4, fixed = true) "Boolean4 condition" annotation(
              Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
            Connectors.IntegerOutput y annotation(
              Placement(transformation(extent = {{100, -10}, {120, 10}})));
            Connectors.TimeLocatorInput tl annotation(
              Placement(transformation(extent = {{-10, 90}, {10, 110}})));
            Connectors.FunctionTypeOutput ft "Function type (monotonic increasing, monotonic decreasing, non monotonic)" annotation(
              Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -110})));
          initial equation
            pre(tl.timePeriod) = false;
            annotation(
              Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-92, 44}, {88, -36}}, lineColor = {244, 125, 35}, textString = "f", textStyle = {TextStyle.Italic})}),
              Diagram(coordinateSystem(preserveAspectRatio = false)));
          end IntegerFunction;