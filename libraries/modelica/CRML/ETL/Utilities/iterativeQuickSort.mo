within CRML.ETL.Utilities;

function iterativeQuickSort "Quick sort algorithm"
        input Real A[:];
        input Integer first = 1;
        input Integer last = size(A, 1);
        output Real B[size(A, 1)];
        output Integer I[size(A, 1)];
      protected
        parameter Integer MAX = size(A, 1);
        Integer p;
        // Start index
        Integer q;
        // Pivot
        Integer r;
        // End index
        Integer stack[MAX, 2];
        // Stack
        Integer n;
        // Top of stack
      algorithm
        /* Initialize vector of indices */
        for i in 1:size(I, 1) loop
          I[i] := i;
        end for;
        /* Initialize stack */
        n := 0;
        /* Push start and end index on stack */
        p := first;
        r := last;
        n := n + 1;
        stack[n, 1] := p;
        stack[n, 2] := r;
        B := A;
        /* Cf. Introduction to Algorithms by Cormen et al. */
        while n > 0 loop
          /* Pop start and end index from stack */
          p := stack[n, 1];
          r := stack[n, 2];
          n := n - 1;
          if p < r then
            (q, B, I) := partition(B, p, r, I);
            /* Push start and end index on stack */
            assert(n < MAX, "Stack overflow");
            n := n + 1;
            stack[n, 1] := p;
            stack[n, 2] := q;
            /* Push start and end index on stack */
            assert(n < MAX, "Stack overflow");
            n := n + 1;
            stack[n, 1] := q + 1;
            stack[n, 2] := r;
          end if;
        end while;
      end iterativeQuickSort;