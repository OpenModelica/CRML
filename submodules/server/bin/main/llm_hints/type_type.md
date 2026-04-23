## Type

In CRML, types and domains are synonymous. A user-defined type is a domain extension or domain specialization of an existing type. Types can be `partial` (not directly instantiable), carry string `alias` names (useful for physical units), and use `forbid` to restrict which operators apply to values of that type.

### Syntax forms

| Form | Syntax |
|---|---|
| Domain extension (new type with constructor and attributes) | `type T is (BaseType v is expr) { attributes }` |
| Domain specialization (fix attributes of a parent type) | `type T is Parent (attr = value, ...)` |
| Partial type (cannot be instantiated directly) | `partial type T is ...` |
| String alias | `type T is ... alias name` or `alias [name with special chars]` |
| Forbidden operators | `type T is ... forbid { op1, op2, ... }` |
| Type equality test | `Boolean b is T1 == T2;` |

### Key concepts

- **`partial`** — marks a type that cannot be used directly; it must be further extended or specialized before use.
- **`alias`** — attaches a string identifier (e.g., `bar`, `Pa`, `K`) to a type, typically used for physical unit names. Use bracket notation `alias [m/s2]` for aliases containing special characters.
- **`forbid`** — prevents specific operators from being applied to values of the type. Forbidden operators propagate to subtypes unless explicitly re-allowed.
- **Type equality** (`==`) — a Boolean expression that evaluates whether two type expressions refer to the same domain.

### Examples

#### Physical quantity hierarchy with unit conversion

```crml
model TypeQuantityHierarchy is {
    // Base partial type with constructor expression
    partial type Quantity is (Real q is rate*u + offset) {
        String SIUnit;    // SI unit for quantity q
        String userUnit;  // User unit for quantity q
        Real u;           // Quantity q expressed in user units
        Real rate;        // Conversion rate between user units and SI units
        Real offset;      // Offset between user units and SI units
    };

    // Pressure specialization fixing the SI unit
    partial type Pressure is Quantity (SIUnit = "Pa");

    // Concrete types for pressure in different units
    type PressurePa  is Pressure (userUnit = "Pa",  rate = 1,    offset = 0) alias Pa;
    type PressureBar is Pressure (userUnit = "bar", rate = 1.e5, offset = 0) alias bar;
};
```

#### String aliases (including special-character aliases)

```crml
model TypeAlias is {
    partial type Quantity is (Real q is rate*u + offset) {
        String SIUnit;
        String userUnit;
        Real u;
        Real rate;
        Real offset;
    };

    // Time units with string aliases
    partial type Time is Quantity (SIUnit = "s");
    type TimeSecond is Time (userUnit = "s", rate = 1,    offset = 0) alias s;
    type Hour       is Time (userUnit = "h", rate = 3600, offset = 0) alias h;

    // Alias containing special characters requires bracket notation
    type Acceleration is Quantity (SIUnit = "m/s2", userUnit = "m/s2", rate = 1, offset = 0) alias [m/s2];
};
```

#### Forbidding operators (absolute temperature)

```crml
model TypeForbid is {
    partial type Quantity is (Real q is rate*u + offset) {
        String SIUnit;
        String userUnit;
        Real u;
        Real rate;
        Real offset;
    };

    // Absolute temperature forbids addition and multiplication (physically meaningless)
    partial type AbsoluteTemperature is Quantity (SIUnit = "K") forbid { +, * };

    type AbsoluteTemperatureKelvin  is AbsoluteTemperature (userUnit = "K",       rate = 1, offset = 0)      alias K;
    type AbsoluteTemperatureCelsius is AbsoluteTemperature (userUnit = "Celsius", rate = 1, offset = 273.15) alias Celsius;
};
```

#### Requirement type (forbid temporal operators on Boolean)

```crml
model TypeRequirement is {
    // Requirement is a Boolean type with forbidden operators
    type Requirement is Boolean forbid { *, +, integrate };
};
```

#### Type equality test

```crml
model TypeEquality is {
    partial type Quantity is (Real q is rate*u + offset) {
        String SIUnit;
        String userUnit;
        Real u;
        Real rate;
        Real offset;
    };

    partial type Pressure    is Quantity (SIUnit = "Pa");
    partial type Temperature is Quantity (SIUnit = "K");

    // Type equality operator: evaluates whether two types are the same
    Boolean sameType is Pressure == Temperature;
};
```
