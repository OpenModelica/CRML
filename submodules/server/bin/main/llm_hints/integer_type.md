## Integer Type

The `Integer` type represents integer numbers (ℤ).

### Constructors

| Form | Syntax |
|---|---|
| Integer literal | `Integer n is integer_value;` |
| Convert from Real (truncates toward zero) | `Integer n is new Integer x;` |

**Integer literal format:** `[sign] [{spaces}] {digits} [exponent]`
- Exponent: `E` or `e` followed by optional sign and digits (no decimal point allowed)

```crml
model IntegerConstructors is {
    // Integer literals
    Integer n1 is 25;
    Integer n2 is + 25;
    Integer n3 is -25;
    Integer n4 is -28E3;

    // Convert from Real (truncates toward zero: -25.8 → -25)
    Real x is -25.8;
    Integer n5 is new Integer x;
};
```

### Operators

| Name | Syntax | Result |
|---|---|---|
| Addition | `Integer n is n1 + n2;` | `Integer` |
| Subtraction | `Integer n is n1 - n2;` | `Integer` |
| Unary plus | `Integer n is +n1;` | `Integer` |
| Unary minus | `Integer n is -n1;` | `Integer` |
| Multiplication | `Integer n is n1 * n2;` | `Integer` |
| Division | `Integer n is n1 / n2;` | `Integer` |
| Modulo | `Integer n is n1 mod n2;` | `Integer` |
| Exponentiation | `Integer n is n1 ^ n2;` | `Integer` |
| Greater or equal | `Boolean b is n1 >= n2;` | `Boolean` |
| Lesser or equal | `Boolean b is n1 <= n2;` | `Boolean` |
| Strictly greater | `Boolean b is n1 > n2;` | `Boolean` |
| Strictly lesser | `Boolean b is n1 < n2;` | `Boolean` |
| Equal to | `Boolean b is n1 == n2;` | `Boolean` |
| Different from | `Boolean b is n1 <> n2;` | `Boolean` |
| If-then-else | `Integer n is if b then n1 else n2;` | `Integer` |
| At event (clock sample) | `Integer n is n1 at c;` | `Integer` |

Comparison operators return a 4-valued `Boolean` (`true`, `false`, `undecided`, `undefined`).

The `at` operator samples `n1` at each tick of clock `c`; the result is defined only at clock ticks.

```crml
model IntegerOperators is {
    Integer n1 is external;
    Integer n2 is external;
    Boolean b is external;
    Clock c is external;

    // Arithmetic
    Integer r_add  is n1 + n2;
    Integer r_sub  is n1 - n2;
    Integer r_upos is +n1;
    Integer r_uneg is -n1;
    Integer r_mul  is n1 * n2;
    Integer r_div  is n1 / n2;
    Integer r_mod  is n1 mod n2;
    Integer r_exp  is n1 ^ n2;

    // Comparison (return Boolean)
    Boolean b_ge  is n1 >= n2;
    Boolean b_le  is n1 <= n2;
    Boolean b_gt  is n1 > n2;
    Boolean b_lt  is n1 < n2;
    Boolean b_eq  is n1 == n2;
    Boolean b_neq is n1 <> n2;

    // Conditional and clock-sampled
    Integer r_ite is if b then n1 else n2;
    Integer r_at  is n1 at c;
};
```
