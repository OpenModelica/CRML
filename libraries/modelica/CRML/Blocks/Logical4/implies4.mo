within CRML.Blocks.Logical4;

function implies4 "Boolean4 implies operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
      algorithm
        y := or4(not4(x1), x2);
      end implies4;