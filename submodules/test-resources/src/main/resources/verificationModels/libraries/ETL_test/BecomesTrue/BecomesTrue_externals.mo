within BecomesTrue;
model BecomesTrue_externals
  block PeriodicEvents
    "Events every period from startTime, as CRML.Blocks.Events.EventPeriodic, but portable"
    // EventPeriodic pulses with y = edge(time > tick) after setting tick = time, which
    // needs time > tick to be false at the instant time - tick = 0. Dymola evaluates it
    // so; OpenModelica keeps the relation true, and pulses only once. This block pulses
    // on change(count) instead, true exactly at each event instant in either tool.
    parameter Real period = 1 "Time between events (s)";
    parameter Real startTime "Time of the first event (s); must be after the start of the simulation";
    CRML.ETL.Connectors.BooleanOutput y;
  protected
    discrete Real tick(start = startTime - period, fixed = true) "Time of the last event";
    Integer count(start = 0, fixed = true) "Number of events so far";
  equation
    when time >= pre(tick) + period then
      tick = time;
      count = pre(count) + 1;
    end when;
    y = change(count);
  end PeriodicEvents;

  PeriodicEvents eventPeriodic1(period=3, startTime=2)
    annotation (Placement(transformation(extent={{-60,-10},{-40,10}})));
  CRML.ETL.Connectors.Boolean4Output b1
    annotation (Placement(transformation(extent={{100,-10},{120,10}})));
  CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1
    annotation (Placement(transformation(extent={{-4,-4},{4,4}})));
  CRML.Blocks.Events.ShowEvent showEvent_eventPeriodic_b1
    annotation (Placement(transformation(extent={{-20,-44},{-12,-36}})));
equation
  connect(eventPeriodic1.y, booleanToBoolean4_1.u)
    annotation (Line(points={{-39,0},{-4.4,0}}, color={217,67,180}));
  connect(booleanToBoolean4_1.y, b1)
    annotation (Line(points={{4.4,0},{110,0}}, color={162,29,33}));
  connect(showEvent_eventPeriodic_b1.u, eventPeriodic1.y) annotation (Line(
        points={{-20.4,-40},{-30,-40},{-30,0},{-39,0}}, color={217,67,180}));
  annotation (Icon(coordinateSystem(preserveAspectRatio=false)), Diagram(
        coordinateSystem(preserveAspectRatio=false)));
end BecomesTrue_externals;
