within CRML.CompilerCompliancy;

block CRMLClockClock "Converts Clock into a CRMLClock"
      ETL.Connectors.CRMLClockOutput y
        annotation (Placement(transformation(extent={{100,-10},{120,10}})));
      ETL.Connectors.ClockInput c annotation (Placement(transformation(extent={{-120,
                -10},{-100,10}}), iconTransformation(extent={{-120,-10},{-100,10}})));

      CRML.Blocks.Events.ClockToBoolean clockToBoolean
        annotation (Placement(transformation(extent={{-10,-10},{10,10}})));

    protected
      Boolean e;

    public
      Modelica.Clocked.BooleanSignals.Sampler.Hold hold1
        annotation (Placement(transformation(extent={{44,-6},{56,6}})));
    algorithm
      e := (y.b == CRML.ETL.Types.Boolean4.true4 and change(y.b));

      when (e) then
        y.ticks[y.counter] := time;
        y.counter := pre(y.counter)+1;
      end when;

    equation
      y.b = CRML.ETL.Types.cvBooleanToBoolean4(hold1.y);
      y.out = CRML.ETL.Types.cvBooleanToBoolean4(e);
      connect(clockToBoolean.u, c) annotation (Line(
          points={{-12,0},{-110,0}},
          color={175,175,175},
          pattern=LinePattern.Dot,
          thickness=0.5));
      connect(clockToBoolean.y, hold1.u)
        annotation (Line(points={{12,0},{42.8,0}}, color={217,67,180}));
      annotation(
        Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, 100}, {100, -100}}),                                                                                                                                                                                                        Text(origin = {2, 52}, extent = {{-58, 16}, {58, -16}},
              textString="Clock -> CRMLClock",
              textColor={0,0,0})}),
        Diagram(coordinateSystem(preserveAspectRatio = false)));
    end CRMLClockClock;