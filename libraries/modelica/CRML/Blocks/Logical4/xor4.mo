within CRML.Blocks.Logical4;

function xor4 "Boolean4 xor operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
algorithm
        y := or4(and4(x1, not4(x2)), and4(not4(x1), x2));
end xor4;
