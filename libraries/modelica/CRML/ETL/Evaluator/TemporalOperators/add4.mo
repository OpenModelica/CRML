within CRML.ETL.Evaluator.TemporalOperators;

function add4 "Boolean4 accumulation operator for requirements"
          import CRML.ETL.Types.Boolean4;
          input Boolean4 x1;
          input Boolean4 x2;
          output Boolean4 y;
        algorithm
          y := TruthTables.add4[Integer(x1), Integer(x2)];
        end add4;