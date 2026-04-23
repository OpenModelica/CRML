## Real Type

The `Real` type represents real numbers.

### Constructors

| Form | Syntax |
|---|---|
| Decimal literal | `Real x is decimal_value;` |
| Convert from Integer | `Real x is new Real n;` |

**Decimal literal format:** `[sign] [{spaces}] {digits} ['.'] [{digits}] [exponent]`
- Exponent: `E` or `e` followed by optional sign and digits

```crml
model RealConstructors is {
    // Decimal literals
    Real x1 is -28.775E+3;
    Real x2 is 0.28e-7;
    Real x3 is +3.E10;
    Real x4 is 25;
    Real pi is 3.141592;

    // Convert from Integer
    Integer n is 2;
    Real x5 is new Real n;
};
```

### Operators

| Name | Syntax | Result |
|---|---|---|
| Addition | `Real x is x1 + x2;` | `Real` |
| Subtraction | `Real x is x1 - x2;` | `Real` |
| Unary plus | `Real x is +x1;` | `Real` |
| Unary minus | `Real x is -x1;` | `Real` |
| Multiplication | `Real x is x1 * x2;` | `Real` |
| Division | `Real x is x1 / x2;` | `Real` |
| Exponentiation | `Real x is x1 ^ x2;` | `Real` |
| Greater or equal | `Boolean b is x1 >= x2;` | `Boolean` |
| Lesser or equal | `Boolean b is x1 <= x2;` | `Boolean` |
| Strictly greater | `Boolean b is x1 > x2;` | `Boolean` |
| Strictly lesser | `Boolean b is x1 < x2;` | `Boolean` |
| Sine | `Real x is sin x1;` | `Real` |
| Cosine | `Real x is cos x1;` | `Real` |
| Arc sine | `Real x is asin x1;` | `Real` |
| Arc cosine | `Real x is acos x1;` | `Real` |
| Exponential | `Real x is exp x1;` | `Real` |
| Natural logarithm | `Real x is log x1;` | `Real` |
| Base-10 logarithm | `Real x is log10 x1;` | `Real` |
| If-then-else | `Real x is if b then x1 else x2;` | `Real` |
| At event (clock sample) | `Real x is x1 at c;` | `Real` |

Comparison operators return a 4-valued `Boolean` (`true`, `false`, `undecided`, `undefined`).

The `at` operator samples `x1` at each tick of clock `c`; the result is defined only at clock ticks.

```crml
model RealOperators is {
    Real x1 is external;
    Real x2 is external;
    Boolean b is external;
    Clock c is external;

    // Arithmetic
    Real r_add  is x1 + x2;
    Real r_sub  is x1 - x2;
    Real r_upos is +x1;
    Real r_uneg is -x1;
    Real r_mul  is x1 * x2;
    Real r_div  is x1 / x2;
    Real r_exp  is x1 ^ x2;

    // Comparison (return Boolean)
    Boolean b_ge is x1 >= x2;
    Boolean b_le is x1 <= x2;
    Boolean b_gt is x1 > x2;
    Boolean b_lt is x1 < x2;

    // Math functions
    Real r_sin   is sin x1;
    Real r_cos   is cos x1;
    Real r_asin  is asin x1;
    Real r_acos  is acos x1;
    Real r_expf  is exp x1;
    Real r_log   is log x1;
    Real r_log10 is log10 x1;

    // Conditional and clock-sampled
    Real r_ite is if b then x1 else x2;
    Real r_at  is x1 at c;
};
```
