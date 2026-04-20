within CRML.Units.SI;

operator record ComplexElectricPotential = Complex(redeclare CRML.Units.SI.ElectricPotential re, redeclare CRML.Units.SI.ElectricPotential im) "Complex electric potential";