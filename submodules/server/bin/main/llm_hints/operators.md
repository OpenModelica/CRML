## Operators

Operators are named functions mapping typed inputs to a typed output (`f: T1 × T2 × ... × Tn → T`). 
They are the primary abstraction mechanism for reusable computations in CRML. No categories are associated by default (C(f) = ∅).

Avaialble syntax styles:
* Natural language — the function name is interleaved with its arguments as readable keywords:
`Operator [ ReturnType ] keyword1 Type1 arg1 keyword2 Type2 arg2 = expr;`

* Mathematical — standard functional notation with a single name and parenthesised arguments:
`Operator f = new Operator [ ReturnType ] (Type1 arg1, Type2 arg2) = expr;`

* Mixed — named with natural language syntax but also bound to an identifier for mathematical-style calls:
`Operator f is new Operator [ ReturnType ] keyword1 Type1 arg1 keyword2 Type2 arg2 = expr;`

Operator overloading is supported: two operators sharing the same keyword(s) but with different argument types have distinct signatures.

```crml
model Example is {
	// Redefine <= for integers from < and ==
	Operator [ Boolean ] Integer n1 '<=' Integer n2 = (n1 < n2) or (n1 == n2);

	// Absolute value of a Real
	Operator [ Real ] 'abs' Real x = if x < 0.0 then -x else x;

	// Concatenate two Strings with a separator
	Operator [ String ] String s1 'joinWith' String s2 = s1 + ", " + s2;

	// Usage examples
	Boolean lessOrEq is 3 <= 5;
	Real magnitude is 'abs' -2.7;
	String result is "hello" 'joinWith' "world";
};
```

## Templates

Templates are a restricted form of operator that work exclusively on Boolean arguments and return a Boolean. 
They are syntactically identical to operators, but with no type annotations on the arguments.

Syntax styles mirror those of Operator (natural language, mathematical, mixed), simply omitting all type keywords.

```crml
model Example is {
	// Logical disjunction
	//Template b1 'or' b2 = not (not b1 and not b2); // or is a built in keyword (ETL library also derives a definiton with De Morgan's Laws)
	
	// Logical inference (implication)
	Template b1 implies b2 = not b1 or b2;

	// Exclusive or
	Template b1 xor b2 = (b1 or b2) and not (b1 and b2);
	
	// 'xor' template usage
	Boolean modeA;
	Boolean modeB;
	Boolean exactlyOne is modeA 'xor' modeB;
	
	// Nested template usage
	Boolean a;
	Boolean b;
	Boolean c;
	Boolean d;
	Boolean complex is (a or b) 'implies' (c 'xor' d);
};
```

## Summary
|                  | Operator      | Template     |
|------------------|---------------|--------------|
| Argument types   | Any CRML type | Boolean only | 
| Return type      | Any CRML type | Boolean only | 
| Type annotations | Required      | Omitted      | 

