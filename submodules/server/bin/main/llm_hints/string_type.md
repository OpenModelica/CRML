## String Type

The `String` type represents character sequences.

### Constructors

| Form | Syntax |
|---|---|
| String literal | `String s is "text";` |
| Convert from Integer | `String s is new String n;` |
| Convert from Real | `String s is new String x;` |
| Convert from Boolean | `String s is new String b;` |

**String literal format:** characters enclosed in double quotes.
Supported escape sequences: `\\`, `\"`, `\'`, `\?`, `\a`, `\b`, `\f`, `\n`, `\r`, `\t`, `\v`.

```crml
model StringConstructors is {
    // String literals
    String s1 is "hello";
    String s2 is "line1\nline2";
    String s3 is "quote: \"hi\"";

    // Convert from other types
    Integer n is 42;
    String s4 is new String n;

    Real x is 3.14;
    String s5 is new String x;

    Boolean b is true;
    String s6 is new String b;
};
```

### Operators

| Name | Syntax | Result |
|---|---|---|
| Concatenation | `String s is s1 + s2;` | `String` |
| Concatenation with Integer | `String s is s1 + n;` | `String` |
| Concatenation with Real | `String s is s1 + x;` | `String` |
| Integer prefix | `String s is n + s1;` | `String` |
| Real prefix | `String s is x + s1;` | `String` |

```crml
model StringOperators is {
    String s1 is "value: ";
    String s2 is " units";
    Integer n is 42;
    Real x is 3.14;

    // Concatenation
    String r1 is s1 + s2;
    String r2 is s1 + n;
    String r3 is s1 + x;
    String r4 is n + s2;
    String r5 is x + s2;
};
```
