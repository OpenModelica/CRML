within CRML.Units.SI;

operator record ComplexElectricFieldStrength = Complex(redeclare CRML.Units.SI.ElectricFieldStrength re, redeclare CRML.Units.SI.ElectricFieldStrength im) "Complex electric field strength";