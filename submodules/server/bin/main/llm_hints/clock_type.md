## Clock Type

A `Clock` (also called a *discrete clock* or *multiple event*) represents an ordered set of time instants called *ticks*. Clocks are used to synchronize and sample signals. A clock is constructed from a Boolean expression: a new tick is added each time the Boolean becomes `true`.

### Constructors

| Form | Syntax |
|---|---|
| Create clock from Boolean (ticks when `b` becomes true) | `Clock c is new Clock b;` |

```crml
model ClockConstructors is {
    // A clock that ticks every time sensor_active becomes true
    Boolean sensor_active is external;
    Clock sensor_clock is new Clock sensor_active;
};
```

### Operators

| Name | Syntax | Result |
|---|---|---|
| Delay | `Clock c2 is c1 + d;` | `Clock` |
| Projection | `Clock c is c1 proj c2;` | `Clock` |
| Bounded projection | `Clock c is c1 proj (d) c2;` | `Clock` |
| Current tick (as Event) | `Event e is tick c;` | `Event` |
| Filter | `Clock c2 is c1 filter cond tick;` | `Clock` |
| Conjunction of clocks | `Clock c is c1 and c2;` | `Clock` |
| Conjunction of event and clock | `Clock c2 is e and c1;` | `Clock` |
| Conjunction of clock and event | `Clock c2 is c1 and e;` | `Clock` |
| Disjunction of clocks | `Clock c is c1 or c2;` | `Clock` |
| Disjunction of event and clock | `Clock c2 is e or c1;` | `Clock` |
| Disjunction of clock and event | `Clock c2 is c1 or e;` | `Clock` |
| Number of ticks (cardinality) | `Integer n is card c;` | `Integer` |

- **Delay** (`c1 + d`): shifts all ticks of `c1` forward by `d` ticks of `c1` (Integer `d`).
- **Projection** (`c1 proj c2`): for each tick of `c1`, keeps the first tick of `c2` that follows it.
- **Bounded projection** (`c1 proj (d) c2`): projection with a maximum delay of `d` ticks.
- **Filter** (`c1 filter cond tick`): retains only the ticks of `c1` for which the Boolean condition `cond` holds at the tick instant.
- **Conjunction** (`and`): set intersection — the result ticks at instants that belong to both operands.
- **Disjunction** (`or`): set union — the result ticks at instants that belong to either operand.
- **Cardinality** (`card c`): the total number of ticks accumulated so far.

```crml
model ClockOperators is {
    Boolean request is external;
    Boolean ack is external;
    Boolean valid is external;
    Event deadline is external;

    // Base clocks derived from Booleans
    Clock req_clock is new Clock request;
    Clock ack_clock is new Clock ack;

    // Delay: ticks 3 req_clock ticks after each req_clock tick
    Clock delayed_req is req_clock + 3;

    // Projection: for each request tick, the first ack tick that follows
    Clock matched_ack is req_clock proj ack_clock;

    // Bounded projection: ack must follow within 5 ticks of req_clock
    Clock bounded_ack is req_clock proj (5) ack_clock;

    // Current tick as an event
    Event last_req is tick req_clock;

    // Filter: keep only ack ticks where valid is true
    Clock valid_ack is ack_clock filter valid tick;

    // Conjunction: ticks that are both a request and an ack
    Clock simultaneous is req_clock and ack_clock;

    // Disjunction: ticks that are either a request or an ack
    Clock any_event is req_clock or ack_clock;

    // Conjunction with a single event
    Clock after_deadline is ack_clock and deadline;

    // Cardinality: count how many requests have occurred
    Integer req_count is card req_clock;
};
```
