within CRML.ETL.Utilities;

function vectorMin "Returns the smallest element of a vector"
        input Real A[:];
        input Integer first = 1;
        input Integer last = size(A, 1);
        output Real b;
        output Integer i;
      protected
        Real B[size(A, 1)];
        Integer I[size(A, 1)];
      algorithm
        (B, I) := sort(A, first, last);
        b := B[first];
        i := I[first];
        annotation(
          Documentation(info = "<html>
<h4>Syntax</h4>
<p style=\"margin-left: 30px;\">(b, i) = v<b>ectorMin </b>(A, first, last); </p>
<h4>Description</h4>
<p><b>b</b> is the smallest element of vector <b>A</b> between indices <b>first</b> and <b>last</b>. </p>
<p><b>i</b> contains the index of <b>b</b> in vector <b>A</b>.</p>
<p><b>First</b> and <b>last</b> are optional. If <b>first</b> is omitted, then <b>first</b> = 1. If <b>last</b> is omitted, then <b>last</b> equals the size of <b>A</b>.</p>
<h4>Examples</h4>
<p style=\"margin-left: 30px;\">(b, i) = vectorMin(<code>{1.3,&nbsp;1.2,&nbsp;7.4,&nbsp;3.6,&nbsp;20.9,&nbsp;0,&nbsp;-6.7,&nbsp;-100,&nbsp;5,&nbsp;0.1});</code></p>
<pre>returns 
b = -100;
i = 8;

    (b, i) = vectorMax({1.3, 1.2, 7.4, 3.6, 20.9, 0, -6.7, -100, 5, 0.1}, 2, 6);

returns 
b = 0;
i = 6;</pre>
</html>"));
      end vectorMin;