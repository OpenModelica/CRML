within CRML.ETL.Utilities;

function getRandom
        input Integer s;
        output Real x;
      algorithm
        x := Modelica.Math.Random.Utilities.impureRandom(id = s);
      end getRandom;