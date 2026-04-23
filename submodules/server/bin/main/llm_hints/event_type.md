## Event Type

An `Event` represents a single time instant at which a Boolean expression becomes `true` (a rising edge). Events always occur in the continuous time domain. An event captures the *first* moment a condition transitions from not-true to `true`. Events are the atomic building blocks for clocks and periods.

### Constructors

| Form | Syntax |
|---|---|
| Create event from Boolean (fires when `b` first becomes true) | `Event e is new Event b;` |

```crml
model EventConstructors is {
    // An event that fires the moment the sensor becomes active
    Boolean sensor_active is external;
    Event sensor_on is new Event sensor_active;
};
```

### Operators

| Name | Syntax | Result |
|---|---|---|
| Projection | `Event e2 is e1 proj c;` | `Event` |
| Bounded projection | `Event e2 is e1 proj(d) c;` | `Event` |
| Delay | `Event e2 is e1 + d;` | `Event` |
| Elapsed time between events | `Real d is e2 - e1;` | `Real` |
| Elapsed physical time since event | `Real d is time from e;` | `Real` |
| Elapsed physical time since event (alt) | `Real d is time - e;` | `Real` |
| Before (non-strict) | `Boolean b is e1 <= e2;` | `Boolean` |
| After (non-strict) | `Boolean b is e1 >= e2;` | `Boolean` |
| Strictly before | `Boolean b is e1 < e2;` | `Boolean` |
| Strictly after | `Boolean b is e1 > e2;` | `Boolean` |
| Conjunction with event | `Clock c is e1 and e2;` | `Clock` |
| Disjunction with event | `Clock c is e1 or e2;` | `Clock` |

- **Projection** (`e1 proj c`): the first tick of clock `c` that occurs after event `e1`.
- **Bounded projection** (`e1 proj(d) c`): projection with a maximum delay of `d` ticks of `c`.
- **Delay** (`e1 + d`): shifts event `e1` forward in time by duration `d`.
- **Elapsed** (`e2 - e1`): the physical time elapsed between two events (returns a `Real` duration).
- **Time from** (`time from e` / `time - e`): the physical time elapsed since event `e` up to the current instant.
- **Ordering operators** (`<`, `<=`, `>`, `>=`): compare when two events occurred; result is `true` once the relationship is established.
- **Conjunction** (`and`): creates a `Clock` whose ticks are the instants that belong to both events (set intersection).
- **Disjunction** (`or`): creates a `Clock` whose ticks are the instants that belong to either event (set union).

```crml
model EventOperators is {
    Boolean request is external;
    Boolean response is external;
    Boolean tick_signal is external;

    // Events from Booleans
    Event req_event is new Event request;
    Event resp_event is new Event response;

    // Clock used for projection
    Clock base_clock is new Clock tick_signal;

    // Projection: first tick of base_clock after req_event
    Event projected is req_event proj base_clock;

    // Bounded projection: first tick within 10 ticks of base_clock
    Event bounded is req_event proj(10) base_clock;

    // Delay: response expected 5.0 time units after request
    Event expected_resp is req_event + 5.0;

    // Elapsed time between request and response
    Real latency is resp_event - req_event;

    // Time elapsed since the last request
    Real age is time from req_event;

    // Ordering: check that request occurred before response
    Boolean ordered is req_event <= resp_event;

    // Strictly before: request must precede response
    Boolean strict_order is req_event < resp_event;

    // Disjunction: a clock that ticks at either request or response
    Clock any_activity is req_event or resp_event;
};
```
