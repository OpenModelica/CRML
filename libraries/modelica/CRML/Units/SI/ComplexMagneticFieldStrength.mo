within CRML.Units.SI;

operator record ComplexMagneticFieldStrength = Complex(redeclare CRML.Units.SI.MagneticFieldStrength re, redeclare CRML.Units.SI.MagneticFieldStrength im) "Complex magnetic field strength";