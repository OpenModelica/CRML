within CRML.CompilerCompliancy;

block CRMLPeriodTimePeriod
      "Extracts the CRML time period of a single time locator"
      ETL.Connectors.CRMLPeriodOutput y annotation(
        Placement(transformation(extent = {{100, -10}, {120, 10}})));
      ETL.Connectors.TimeLocatorInput tl annotation(
        Placement(transformation(extent = {{-10, 90}, {10, 110}}), iconTransformation(extent = {{-10, 90}, {10, 110}})));
      ETL.TimeLocators.Attributes.PeriodStart periodStart annotation (Placement(
            transformation(origin={-70,50}, extent={{-10,-10},{10,10}})));
      ETL.TimeLocators.Attributes.PeriodEnd periodEnd annotation (Placement(
            transformation(origin={-30,10}, extent={{-10,-10},{10,10}})));
      ETL.TimeLocators.Attributes.PeriodTimePeriod periodTimePeriod
        annotation (Placement(transformation(extent={{0,-60},{20,-40}})));
    initial equation
      y.start_event.t = -1;
      y.close_event.t = -1;
    equation
      y.isLeftBoundaryIncluded = tl.isLeftBoundaryIncluded;
      y.isRightBoundaryIncluded = tl.isRightBoundaryIncluded;

      when (y.start_event.b == CRML.ETL.Types.Boolean4.true4) then
        y.start_event.t = time;
      end when;

      when (y.close_event.b == CRML.ETL.Types.Boolean4.true4) then
        y.close_event.t = time;
      end when;

      y.start_event.b = CRML.ETL.Types.cvBooleanToBoolean4(periodStart.y);
      y.close_event.b = CRML.ETL.Types.cvBooleanToBoolean4(periodEnd.y);
      y.is_open = periodTimePeriod.y;
      connect(
      periodStart.tl, tl) annotation (
        Line(points={{-70,60},{-70,86},{0,86},{0,100}},          color = {0, 0, 255}));
      connect(
      periodEnd.tl, tl) annotation (
        Line(points={{-30,20},{-30,80},{0,80},{0,100}},                    color = {0, 0, 255}));
      connect(periodTimePeriod.tl, tl) annotation (Line(points={{10,-40},{12,-40},{12,
              82},{0,82},{0,100}}, color={0,0,255}));
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, 100}, {100, -100}}), Line(points = {{-86, -24}, {-50, -24}, {-50, 20}, {60, 20}, {60, -26}, {88, -26}}), Rectangle(fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, extent = {{-66, -46}, {0, -62}}), Rectangle(fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid, extent = {{0, -46}, {66, -62}}), Text(origin = {2, 52}, extent = {{-58, 16}, {58, -16}}, textString = "tl -> Period")}),
        Diagram(coordinateSystem(preserveAspectRatio = false)));
    end CRMLPeriodTimePeriod;