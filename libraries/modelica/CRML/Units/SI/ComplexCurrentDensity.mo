within CRML.Units.SI;

operator record ComplexCurrentDensity = Complex(redeclare CRML.Units.SI.CurrentDensity re, redeclare CRML.Units.SI.CurrentDensity im) "Complex electrical current density";