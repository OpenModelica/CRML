within CRML.Blocks.Logical4;

function or4 "Boolean4 or operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
algorithm
        y := not4(and4(not4(x1), not4(x2)));
end or4;
