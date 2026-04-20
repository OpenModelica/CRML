within CRML.ETL.Utilities;

function firstTrueIndex "Returns the index of the first true element of a Boolean vector"
        extends Modelica.Icons.Function;
        input Boolean b[:];
        input Boolean reverse = false "If true, backward scanning";
        output Integer index;
      protected
        parameter Integer n1 = if not reverse then 1 else size(b, 1);
        parameter Integer n2 = if not reverse then size(b, 1) else 1;
        parameter Integer s = if not reverse then +1 else -1;
      algorithm
        index := 0;
        for i in n1:s:n2 loop
          if b[i] then
            index := i;
            return;
          end if;
        end for;
        annotation(
          Documentation(info = "<html>
<h4>Syntax</h4>
<p style=\"margin-left: 40px;\"><code><b>firstTrueIndex</b>(b);</code> </p>
<h4>Description</h4>
<p>Returns the index of the first <b>true</b> element of the Boolean vector b. If no element is <b>true</b> or b is an empty vector (i.e., size(b,1)=0) the function returns 0. </p>
<h4>Example</h4>
<pre>  Boolean b1[3] = {false, false, false};
  Boolean b2[3] = {false, true, false};
  Boolean b3[4] = {false, true, false, true};
  Integer r1, r2, r3;
<b>algorithm</b>
  r1 = firstTrueIndex(b1);  // r1 = 0
  r2 = firstTrueIndex(b2);  // r2 = 2</pre>
<p style=\"margin-left: 40px;\"><code>r3 = firstTrueIndex(b3); // r3 = 2</code> </p>
<p><br>The property is also demonstrated with the following <a href=\"modelica://ReqSysPro.Examples.Utilities.firstTrueIndex\">example</a> calling the function as: </p>
<p style=\"margin-left: 30px;\">Integer firstTrueIndexOutput = firstTrueIndex(b) </p>
<p>results in </p>
<p><img src=\"modelica://ReqSysPro/Resources/Images/Utilities/firstTrueIndex.png\"/></p>
<p><br><h4>See also</h4></p>
<p><a href=\"modelica://Modelica.Math.BooleanVectors.allTrue\">allTrue</a>, <a href=\"modelica://Modelica.Math.BooleanVectors.anyTrue\">anyTrue</a>, <a href=\"modelica://Modelica.Math.BooleanVectors.countTrue\">countTrue</a>, <a href=\"modelica://Modelica.Math.BooleanVectors.enumerate\">enumerate</a>, <a href=\"modelica://Modelica.Math.BooleanVectors.index\">index</a>, and <a href=\"modelica://Modelica.Math.BooleanVectors.oneTrue\">oneTrue</a>. </p>
</html>"));
      end firstTrueIndex;