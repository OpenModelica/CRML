within CRML.ETL.Evaluator.TemporalOperators;

function mul4 "Boolean filter"
          import CRML.ETL.Types.Boolean4;
          input Boolean4 x1;
          input Boolean4 x2;
          output Boolean4 y;
        algorithm
          y := TruthTables.mul4[Integer(x1), Integer(x2)];
        end mul4;