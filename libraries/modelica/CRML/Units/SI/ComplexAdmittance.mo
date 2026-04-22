within CRML.Units.SI;

operator record ComplexAdmittance = Complex(redeclare Conductance re, redeclare Susceptance im) "Complex electrical admittance";