within CRML.Blocks.Logical;

block BooleanDelay "Boolean signal delay"
      protected
        parameter Integer Nmax = 10;
        // Maximum number of elements in the stack
        Integer N;
        // Number of elements in the stack
        Real T[Nmax](each start = 0, each fixed = true);
        // Time stack
        Boolean V[Nmax](each start = true, each fixed = true);
        // Value stack
        Integer n1t;
        // Stack top (points above the last element pushed on the stack)
        Integer n2t(start = 1, fixed = true);
        // Stack top
        Integer n1b;
        // Stack bottom (points to the first element pushed on the stack)
        Integer n2b(start = 1, fixed = true);
        // Stack bottom
        Integer n;
        // Number of stack pushes
      public
        ETL.Connectors.BooleanInput u(start = false, fixed = true) annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanOutput y(start = false, fixed = true) annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}}), iconTransformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.RealInput delay "Shift delay" annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}})));
      initial equation
        n = if u then 1 else 0;
        n1t = if u then 2 else 1;
        n1b = if u then 1 else 0;
      algorithm
        assert(N <= Nmax, "Stack overflow");
        N := if (n2t == n2b) then n1t - n1b else Nmax - (n1b - n1t);
        when change(u) then
          T[n1t] := time;
          V[n1t] := u;
          n1t := if (pre(n1t) < Nmax) then pre(n1t) + 1 else 1;
          n2t := if (pre(n1t) < Nmax) then pre(n2t) else pre(n2t) + 1;
          n := pre(n) + 1;
        end when;
        when n > 0 then
          n1b := 1;
        end when;
        when (n1b > 0) and (time >= T[n1b] + delay) then
          y := V[n1b];
          n1b := if (pre(n1b) < Nmax) then pre(n1b) + 1 else 1;
          n2b := if (pre(n1b) < Nmax) then pre(n2b) else pre(n2b) + 1;
        end when;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -58}, {-4, -58}, {-4, -14}, {38, -14}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 76}, {-20, 76}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, lineThickness = 5.0, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, -58}, {-4, -58}, {-4, -14}, {38, -14}, {38, -58}, {66, -58}}, color = {0, 0, 0}), Line(points = {{-80, 32}, {-62, 32}, {-62, 76}, {-20, 76}, {-20, 32}, {66, 32}}, color = {255, 0, 255}), Text(extent = {{-60, 148}, {62, 112}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%name")}));
      end BooleanDelay;