within CRML.Blocks.Logical;

block Or_n "Or operator to on N Boolean: u1 or u2 or ... or uN"
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
      protected
        Boolean z[N];
      public
        ETL.Connectors.BooleanInput[N] u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        z[1] = u[1];
        for i in 2:N loop
          z[i] = z[i - 1] or u[i];
        end for;
        y = z[N];
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{71, 7}, {85, -7}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "or")}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{71, 7}, {85, -7}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "or")}));
      end Or_n;