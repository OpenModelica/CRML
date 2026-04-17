within CRML.ETL.Utilities;

function partition "Partition function for the quick sort algorithm"
        input Real A[:];
        input Integer p;
        input Integer r;
        input Integer I[:];
        output Integer n;
        output Real B[size(A, 1)];
        output Integer J[size(A, 1)];
      protected
        Real x;
        Real y;
        Integer i;
        Integer j;
        Integer k;
      algorithm
        x := A[p];
        i := p - 1;
        j := r + 1;
        B := A;
        J := I;
        /* Cf. Introduction to Algorithms by Cormen et al. */
        while true loop
          while true loop
            j := j - 1;
            if j == 0 then
              break;
            end if;
            if B[j] <= x then
              break;
            end if;
          end while;
          while true loop
            i := i + 1;
            if i == size(B, 1) + 1 then
              break;
            end if;
            if B[i] >= x then
              break;
            end if;
          end while;
          /* Exchange elements B[i] and B[j] */
          if i < j then
            y := B[i];
            B[i] := B[j];
            B[j] := y;
            k := J[i];
            J[i] := J[j];
            J[j] := k;
          else
            n := j;
            return;
          end if;
        end while;
      end partition;