## Boolean Type

The `Boolean` type is 4-valued: `{true, false, undecided, undefined}`. This extends classical logic to handle partial knowledge and undefined signals.

### Constructors

| Form | Syntax |
|---|---|
| Literal | `Boolean b is true;` / `false` / `undecided` / `undefined` |
| Convert from Clock (true at tick, false otherwise) | `Boolean b is new Boolean c;` |

```crml
model BooleanConstructors is {
    Boolean b1 is true;
    Boolean b2 is false;
    Boolean b3 is undecided;
    Boolean b4 is undefined;

    Clock c is external;
    Boolean b5 is new Boolean c;
};
```

### Operators

| Name | Syntax | Result |
|---|---|---|
| Conjunction | `Boolean b is b1 and b2;` | `Boolean` |
| Conjunction with Event | `Boolean b is b1 and e;` | `Boolean` |
| Disjunction | `Boolean b is b1 or b2;` | `Boolean` |
| Disjunction with Event | `Boolean b is b1 or e;` | `Boolean` |
| Negation | `Boolean b is not b1;` | `Boolean` |
| Equality | `Boolean b is b1 == b2;` | `Boolean` |
| Difference | `Boolean b is b1 <> b2;` | `Boolean` |
| Accumulation | `Boolean b is b1 + b2;` | `Boolean` |
| Filter | `Boolean b is b1 * b2;` | `Boolean` |
| If-then-else | `Boolean b is if bc then b1 else b2;` | `Boolean` |
| At event (clock sample) | `Boolean b is b1 at c;` | `Boolean` |
| Duration | `Real d is duration b on P;` | `Real` |
| Integration | `Boolean b is integrate b1 on P;` | `Boolean` |

`duration` returns the cumulative time `b` holds `true` over period `P`.
`integrate` evaluates `b1` over period `P` and returns a Boolean verdict.
Accumulation (`+`) and filter (`*`) apply 4-valued Boolean arithmetic, not numeric arithmetic.

```crml
model BooleanOperators is {
    Boolean b1 is external;
    Boolean b2 is external;
    Boolean bc is external;
    Event e is external;
    Clock c is external;
    Period P is external;

    // Logical
    Boolean r_and     is b1 and b2;
    Boolean r_and_ev  is b1 and e;
    Boolean r_or      is b1 or b2;
    Boolean r_or_ev   is b1 or e;
    Boolean r_not     is not b1;

    // Comparison
    Boolean r_eq  is b1 == b2;
    Boolean r_neq is b1 <> b2;

    // 4-valued arithmetic
    Boolean r_acc    is b1 + b2;
    Boolean r_filter is b1 * b2;

    // Conditional and clock-sampled
    Boolean r_ite is if bc then b1 else b2;
    Boolean r_at  is b1 at c;

    // Period-based
    Real    r_dur      is duration b1 on P;
    Boolean r_integrate is integrate b1 on P;
};
```