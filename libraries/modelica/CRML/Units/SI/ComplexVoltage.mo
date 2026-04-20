within CRML.Units.SI;

operator record ComplexVoltage = Complex(redeclare CRML.Units.SI.Voltage re, redeclare CRML.Units.SI.Voltage im) "Complex electrical voltage";