within CRML.Blocks.Logical;

block RandomFailure "Generates failure events depending on repair rates"
        extends Logical.PartialRandom;
        parameter Boolean perfect = false "true: failures are inhibited - false: failures are not inhibited";
      protected
        Boolean independent "Failures are independent from other components";
        Boolean componentIsWorking(start = true, fixed = true);
        Real r(start = 0, fixed = true) "Random number" annotation(
          HideResult = false);
        Real F(start = 0, fixed = true) "Cumulative distribution function" annotation(
          HideResult = true);
        Real hazardRate "Hazard rate";
      public
        ETL.Connectors.BooleanInput failureIsPresent annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = 90, origin = {0, -110})));
        ETL.Connectors.RealInput failureRate "Failure rate (nb of failures per time unit, active if perfect is false)" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}}), visible = not perfect));
        ETL.Connectors.RealInput repairRate "Repair rate (nb of repairs per time unit, active if perfect is false)" annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), visible = not perfect));
      equation
        if (cardinality(failureIsPresent)) == 0 then
          failureIsPresent = not componentIsWorking;
          independent = true;
        else
          independent = false;
        end if;
        /* Component with failures */
        if not perfect then
          hazardRate = if componentIsWorking then failureRate else repairRate;
          der(F) = (1 - F)*hazardRate;
          /* Generate a new random number when initial() or each time a transition occurs */
          when {initial(), componentIsWorking <> pre(componentIsWorking)} then
            r = CRML.ETL.Utilities.getRandom(s = seed);
          end when;
          /* Allow transition if it is a repair (not componentIsWorking) or if no failure is present
       in a group of mutually exclusive failure modes or if it is independent */
          when (F > pre(r)) and (not pre(componentIsWorking) or not pre(failureIsPresent) or independent) then
            reinit(F, 0);
            componentIsWorking = not pre(componentIsWorking);
          end when;
          /* Component without failures */
        else
          {r, F, hazardRate} = {0.0, 0.0, 0.0};
          componentIsWorking = true;
        end if;
        y = not componentIsWorking;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-56, 132}, {56, 106}}, lineColor = {0, 0, 0}, textString = "%name"), Ellipse(extent = {{60, 10}, {80, -10}}, lineColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillColor = DynamicSelect({235, 235, 235}, if y > 0.5 then {0, 255, 0} else {235, 235, 235}), fillPattern = FillPattern.Solid), Line(points = {{-80, 0}, {40, 0}}, color = {0, 0, 0}), Line(points = {{-70, 40}, {-70, 0}}, color = {0, 0, 0}), Line(points = {{-54, 40}, {-54, 0}}, color = {0, 0, 0}), Line(points = {{-32, 40}, {-32, 0}}, color = {0, 0, 0}), Line(points = {{-20, 40}, {-20, 0}}, color = {0, 0, 0}), Line(points = {{-4, 40}, {-4, 0}}, color = {0, 0, 0}), Line(points = {{26, 40}, {26, 0}}, color = {0, 0, 0}), Line(points = {{-70, 40}, {-54, 40}}, color = {0, 0, 0}), Line(points = {{-32, 40}, {-20, 40}}, color = {0, 0, 0}), Line(points = {{-4, 40}, {26, 40}}, color = {0, 0, 0})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StartTime = 0, StopTime = 1, Tolerance = 1e-6, Interval = 0.002));
      end RandomFailure;