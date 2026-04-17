within CRML.Blocks.Logical;

partial block PartialRandom "Partial random"
      protected
        outer Logical.GlobalSeed globalSeed "Definition of global seed via inner/outer";
        parameter Integer seed = globalSeed.seed "Seed for the generation of random numbers";
      public
        ETL.Connectors.BooleanOutput y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.BooleanInput failureIsPresent annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Text(extent = {{-56, 132}, {56, 106}}, lineColor = {0, 0, 0}, textString = "%name"), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid)}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {210, 210, 210}, lineThickness = 5.0, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised), Text(extent = {{-56, 132}, {56, 106}}, lineColor = {0, 0, 0}, textString = "%name"), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid)}),
          experiment(StartTime = 0, StopTime = 1, Tolerance = 1e-6, Interval = 0.002));
      end PartialRandom;