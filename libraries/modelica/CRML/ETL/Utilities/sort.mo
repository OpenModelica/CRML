within CRML.ETL.Utilities;

function sort "Sorts elements in a vector using the quick sort algorithm"
        input Real A[:];
        input Integer first = 1;
        input Integer last = size(A, 1);
        output Real B[size(A, 1)];
        output Integer I[size(A, 1)];
      algorithm
        (B, I) := iterativeQuickSort(A, max(first, 1), min(last, size(A, 1)));
        annotation(
          Documentation(info = "<html>
<h4>Syntax</h4>
<p style=\"margin-left: 30px;\">(B, I) = <b>sort </b>(A, first, last); </p>
<h4>Description</h4>
<p>Vector<b> B</b> contains the elements of vector <b>A</b> sorted by increasing values between indices <b>first</b> and <b>last</b>. </p>
<p>Vector <b>I</b> contains the indices of vector <b>A</b> such that <b>I</b>[i] = j means that <b>B</b>[i] = <b>A</b>[j].</p>
<p><b>First</b> and <b>last</b> are optional. If <b>first</b> is omitted, then <b>first</b> = 1. If <b>last</b> is omitted, then <b>last</b> equals the size of <b>A</b>.</p>
<h4>Examples</h4>
<p style=\"margin-left: 30px;\">(B, I) = sort (<code>{1.3,&nbsp;1.2,&nbsp;7.4,&nbsp;3.6,&nbsp;20.9,&nbsp;0,&nbsp;-6.7,&nbsp;-100,&nbsp;5,&nbsp;0.1});</code></p>
<pre>returns 
B = { -100, -6.7, 0, 0.1, 1.2, 1.3,&nbsp;3.6, 5, 7.4,&nbsp;20.9 };
I = { 8, 7, 6, 2, 1, 4, 9, 3, 5 };

(B, I) = sort({1.3, 1.2, 7.4, 3.6, 20.9, 0, -6.7, -100, 5, 0.1}, 2, 7);

returns 
B = { 1.3, 1.2, -6.7, 0, 3.6, 7.4, 20.9, -100, 5, 0.1 };
I = { 1, 2, 7, 6, 4, 3, 5, 8, 9, 10 };</pre>
</html>"));
      end sort;