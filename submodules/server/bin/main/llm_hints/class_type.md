## Class

In CRML, a **class** is a domain of all objects sharing the same set of named attributes. Unlike primitive types or simple domains, a class groups multiple attributes (which may themselves be of any CRML type, including other classes) under a single named entity. Instances of a class are called **objects** and are created with the `new` keyword. Classes support **inheritance** via `extends`, **multiple inheritance**, `partial` (abstract) classes, and **attribute redeclaration** with `redeclare`.

### Syntax forms

| Form | Syntax |
|---|---|
| Basic class definition | `class C is { Type a1; Type a2; ... };` |
| Attribute with fixed value | `Type a is expr;` |
| External attribute (value supplied by binding) | `Type a is external;` |
| Partial class (cannot be instantiated directly) | `partial class C is { ... };` |
| Class extending one parent | `class C2 is { ... } extends C1;` |
| Class extending multiple parents | `class C is { ... } extends { C1, C2, ..., Cn };` |
| Attribute redeclaration in subclass | `redeclare a1 Type b1 [ is [ value \| external ] ];` |
| Object instantiation | `C obj is new C (attr1 = value1, ...);` |

### Key concepts

- **`partial`** — marks a class that cannot be instantiated directly; it must be extended before use. Useful for defining shared attribute sets.
- **`extends`** — a subclass inherits all attributes of its parent class(es). Multiple parent classes can be listed in braces.
- **`external`** — an attribute whose value is not defined inside the class but must be supplied via binding at instantiation or usage site.
- **`redeclare`** — allows a subclass to rename or retype an inherited attribute, provided the new type is compatible with the original.
- **Object (`new`)** — creates an instance of a class, binding external and value attributes to concrete expressions.

### Examples

#### Partial base class and extension

```crml
model ClassPumpSystem is {
    // Abstract base class — cannot be instantiated directly
    partial class Equipment is {
        String id;
        Boolean inOperation is external;
    };

    // Pump extends Equipment, adding its own external attributes
    class Pump is {
        Real c is external;
        Real efficiency is external;
        Boolean cav is external;
        // Derived attribute: requirement expressed as a formula
        Boolean nocav = during inOperation ensure not cav;
    } extends Equipment;

    // CoolingSystem groups three pumps, itself extending Equipment
    class CoolingSystem is {
        Pump P1;
        Pump P2;
        Pump P3;
    } extends Equipment;
};
```

#### Object instantiation

```crml
model ClassInstantiation is {
    partial class Equipment is {
        String id;
        Boolean inOperation is external;
    };

    class Pump is {
        Real c is external;
        Real efficiency is external;
        Boolean cav is external;
        Boolean nocav = during inOperation ensure not cav;
    } extends Equipment;

    // Instantiate a Pump object, binding the 'id' external attribute
    Pump pump1 is new Pump (id = "Pump1");
};
```

#### Multiple inheritance

```crml
model ClassMultipleInheritance is {
    partial class Named is {
        String name;
    };

    partial class Timed is {
        Real startTime is external;
    };

    // Inherits attributes from both Named and Timed
    class TimedDevice is {
        Boolean active is external;
    } extends { Named, Timed };
};
```

#### Attribute redeclaration

```crml
model ClassRedeclare is {
    partial class Sensor is {
        Real rawValue is external;
    };

    // Subclass renames 'rawValue' to 'temperature' with a compatible type
    class TemperatureSensor is {
        redeclare rawValue Real temperature is external;
    } extends Sensor;
};
```
