within CheckOver;
model CheckOver_verif
  extends CheckOver;
  CheckOver_externals externals
    annotation (Placement(transformation(extent={{-200,0},{-140,60}})));
  // P1 is a set of periods (CRMLPeriods), each from b1 becoming true to b1 becoming
  // false, built as the compiler builds 'new Periods [new Clock b1, new Clock not b1]'.
  // externals.P1 is a single period, of another library, so it cannot be bound directly.
  CRMLtoModelica.Types.CRMLClock P1_start(b = CRMLtoModelica.Functions.cvBooleanToBoolean4(externals.b1.y));
  CRMLtoModelica.Types.CRMLClock_build P1_start_init(clock = P1_start);
  CRMLtoModelica.Types.CRMLClock P1_close(b = CRMLtoModelica.Functions.cvBooleanToBoolean4(not externals.b1.y));
  CRMLtoModelica.Types.CRMLClock_build P1_close_init(clock = P1_close);
  CRMLtoModelica.Types.CRMLPeriods_build P1_init(P = P1);
equation
  // Bindings. P1 field by field: OpenModelica fails to build a model with a record
  // equation over CRMLPeriods, which holds an array of records.
  P1.isLeftBoundaryIncluded = externals.P1.isLeftBoundaryIncluded;
  P1.isRightBoundaryIncluded = externals.P1.isRightBoundaryIncluded;
  P1.start_event = P1_start;
  P1.close_event = P1_close;
  phi1 = externals.phi1;
    annotation (Placement(transformation(extent={{0,-20},{60,40}})),
              Icon(coordinateSystem(preserveAspectRatio=false,
        extent={{-200,-100},{100,100}},
        initialScale=0.1),                                      graphics={
        Ellipse(lineColor = {75,138,73},
                fillColor={255,255,255},
                fillPattern = FillPattern.Solid,
                extent = {{-100,-100},{100,100}}),
        Polygon(lineColor = {0,0,255},
                fillColor = {75,138,73},
                pattern = LinePattern.None,
                fillPattern = FillPattern.Solid,
                points = {{-36,60},{64,0},{-36,-60},{-36,60}})}), Diagram(
        coordinateSystem(preserveAspectRatio=false,
        extent={{-200,-100},{100,100}},
        initialScale=0.1)),
    experiment(StopTime=14));
end CheckOver_verif;
