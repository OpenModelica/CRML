## Period and Periods Types

A `Period` is a single time interval defined by an opening and a closing event. A `Periods` is a collection of periods sharing the same opening/closing pattern, defined by clocks or by an explicit set of `Period` values.

### Period Constructors

A `Period` is constructed using interval notation. The bracket side determines whether the boundary event is included (`[`/`]` closed) or excluded (`]`/`[` open).

| Form | Syntax | Boundaries |
|---|---|---|
| Closed-closed | `Period P is [ e1, e2 ];` | Both endpoints included |
| Open-closed | `Period P is ] e1, e2 ];` | Open at start, closed at end |
| Closed-open | `Period P is [ e1, e2 [;` | Closed at start, open at end |
| Open-open | `Period P is ] e1, e2 [;` | Both endpoints excluded |

```crml
model PeriodConstructors is {
    Event e1 is external;
    Event e2 is external;

    Period P1 is [ e1, e2 ];  // closed-closed
    Period P2 is ] e1, e2 ];  // open-closed
    Period P3 is [ e1, e2 [;  // closed-open
    Period P4 is ] e1, e2 [;  // open-open
};
```

### Period Operators

| Name | Syntax | Result |
|---|---|---|
| Opening event | `Event e is P start;` | `Event` |
| Closing event | `Event e is P end;` | `Event` |

```crml
model PeriodOperators is {
    Period P is external;

    Event e_start is P start;
    Event e_end   is P end;
};
```

### Periods Constructors

A `Periods` can be built from an explicit set of `Period` values or from two clocks using the same interval notation as `Period`.

| Form | Syntax |
|---|---|
| Set of periods | `Periods PS is { P1, P2, Pn };` |
| Closed-closed (clocks) | `Periods PS is [ c1, c2 ];` |
| Open-closed (clocks) | `Periods PS is ] c1, c2 ];` |
| Closed-open (clocks) | `Periods PS is [ c1, c2 [;` |
| Open-open (clocks) | `Periods PS is ] c1, c2 [;` |

```crml
model PeriodsConstructors is {
    Period P1 is external;
    Period P2 is external;
    Period Pn is external;
    Clock c1 is external;
    Clock c2 is external;

    // From an explicit set of Period values
    Periods PS1 is { P1, P2, Pn };

    // From two clocks — each tick of c1 opens a period, each tick of c2 closes it
    Periods PS2 is [ c1, c2 ];
    Periods PS3 is ] c1, c2 ];
    Periods PS4 is [ c1, c2 [;
    Periods PS5 is ] c1, c2 [;
};
```

### Periods Operators

| Name | Syntax | Result |
|---|---|---|
| While (restriction) | `Periods P is P2 while P1;` | `Periods` |

`while` restricts `P2` to the sub-intervals that overlap with `P1`.

```crml
model PeriodsOperators is {
    Periods P1 is external;
    Periods P2 is external;

    Periods P is P2 while P1;
};
```
