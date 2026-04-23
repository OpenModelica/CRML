within CRML.Units.SI;

operator record ComplexReluctance = Complex(redeclare CRML.Units.SI.Reluctance re, redeclare CRML.Units.SI.Reluctance im) "Complex reluctance" annotation(
        Documentation(info = "<html>
<p>
Since magnetic material properties like reluctance and permeance often are anisotropic resp. salient,
a special operator instead of multiplication (compare: tensor vs. vector) is required.
<a href=\"modelica://Modelica.Magnetic.FundamentalWave\">Modelica.Magnetic.FundamentalWave</a> uses a
special record <a href=\"modelica://Modelica.Magnetic.FundamentalWave.Types.Salient\">Salient</a>
which is only valid in the rotor-fixed coordinate system.
</p>
<p>
<b>Note:</b> To avoid confusion, no magnetic material properties should be defined as Complex units.
</p>
</html>"));