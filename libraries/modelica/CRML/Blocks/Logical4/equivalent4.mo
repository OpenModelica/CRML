within CRML.Blocks.Logical4;
function equivalent4 "Boolean4 equivalent operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
algorithm
        y := and4(implies4(x1, x2), implies4(x2, x1));
end equivalent4;
