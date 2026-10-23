# 5. ETL library

## 5.1 Operators on Booleans

**Logical disjunction**

$$
\begin{aligned}
\mathbb{B}: b_1,\ \mathbb{B}: b_2 \\
\mathbb{B}: b_1 \vee b_2 = \neg(\neg b_1 \wedge \neg b_2)
\end{aligned}
$$

```
Template b1 or b2 = not (not b1 and not b2);
```

**Exclusive logical disjunction**

$$
\begin{aligned}
\mathbb{B}: b_1,\ \mathbb{B}: b_2 \\
\mathbb{B}: b_1 \oplus b_2 = (b_1 \vee b_2) \wedge \neg(b_1 \wedge b_2)
\end{aligned}
$$

```
Template b1 xor b2 = (b1 or b2) and not (b1 and b2);
```

**Logical inference**

$$
\begin{aligned}
\mathbb{B}: b_1,\ \mathbb{B}: b_2 \\
\mathbb{B}: b_1 \Rightarrow b_2 = \neg b_1 \vee b_2
\end{aligned}
$$

```
Template b1 implies b2 = not b1 or b2;
```

## 5.2 Operators on clocks

**Filter clock ticks inside a time period**

$$
\begin{aligned}
\mathcal{D}: C,\ \mathcal{P}: P \\
\mathcal{D}: \mathit{inside}(C, P) = C(\mathrm{rank}(*) \geq P{\uparrow} \wedge \mathrm{rank}(*) \leq P{\downarrow})
\end{aligned}
$$

```
Operator [ Clock ] Clock C inside Period P = C filter (tick >= P start) and (tick <= P end);
```

**Count the occurrences of events inside a time period**

$$
\begin{aligned}
\mathcal{D}: C,\ \mathcal{P}: P \\
\mathbb{N}: \mathit{count.inside}(C, P) = \vert \mathit{inside}(C, P) \vert
\end{aligned}
$$

```
Operator [ Integer ] count Clock C inside Period P = card (C inside P);
```

## 5.3 Operators on events

**Events generated when a Boolean becomes true**

$$
\begin{aligned}
\mathbb{B}: b,\ \mathcal{D}: c \\
\mathcal{D}: \mathit{becomes.true}(b) = c(b)
\end{aligned}
$$

```
Operator [ Clock ] Boolean b becomes true = new Clock b;
```

**Events generated when a Boolean becomes false**

$$
\begin{aligned}
\mathbb{B}: b,\ \mathcal{D}: c \\
\mathcal{D}: \mathit{becomes.false}(b) = c(\neg b)
\end{aligned}
$$

```
Operator [ Clock] Boolean b becomes false = new Clock not b;
```

**Events generated when a Boolean becomes true inside a time period**

$$
\begin{aligned}
\mathbb{B}: b,\ \mathcal{P}: P \\
\mathcal{D}: \mathit{becomes.true.inside}(b, P) = \mathit{inside}(\mathit{becomes.true}(b), P)
\end{aligned}
$$

```
Operator [ Clock] Boolean b becomes true inside Period P = (b becomes true) inside P;
```

**Events generated when a Boolean becomes false inside a time period**

$$
\begin{aligned}
\mathbb{B}: b,\ \mathcal{P}: P \\
\mathcal{D}: \mathit{becomes.false.inside}(b, P) = \mathit{inside}(\mathit{becomes.false}(b), P)
\end{aligned}
$$

```
Operator [ Clock] Boolean b becomes false inside Period P = (b becomes false) inside P;
```

## 5.4 Operators for the evaluation of requirements

**Check**

$$
\begin{aligned}
\mathbb{B}: \varphi,\ 2^{\mathcal{P}}: P = \{P_i\}_{1 \leq i \leq \vert P \vert} \\
\mathbb{B}: \mathit{check.over}(\varphi, P) = \varphi \otimes P := \bigwedge_{i=1}^{\vert P \vert} \varphi \otimes \{P_i\}
\end{aligned}
$$

```
Operator [ Boolean ] check Boolean phi over Periods P = and (evaluate phi over P);
```

**Evaluate**

$$
\begin{aligned}
\mathcal{P}: P,\ \mathbb{B}: \varphi \\
\mathbb{B}: \mathit{evaluate.over}(\varphi, P) = \varphi \otimes P := \int_P a(\varphi, P) \times \varphi
\end{aligned}
$$

```
Operator [ Boolean ] evaluate Boolean phi over Period P = integrate ((decide phi over P) * phi) on P;
```

**Decide**

$$
\begin{aligned}
\mathcal{P}: P,\ \mathbb{B}: \varphi \\
\mathbb{B}: \mathit{decide.over}(\varphi, P) = a(\varphi, P) := \varphi \vee P{\downarrow}
\end{aligned}
$$

```
Operator decide is Operator [ Boolean ] decide Boolean phi over Period P = phi or (P end));
```

$$
\begin{aligned}
&\mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1},\ \mathcal{C}(\mathbb{R}^2 \longrightarrow \mathbb{B}): \mathit{increasing2},\ \mathcal{C}(\mathbb{B} \longrightarrow \mathbb{B}): \mathit{varying1},\ \mathcal{C}(\mathbb{B} \longrightarrow \mathbb{B}): \mathit{varying2} \\
&\{\mathit{increasing1}, \mathit{increasing2}, \mathit{varying1}, \mathit{varying2}\} \subset \mathcal{C}(\mathit{decide.over}(\varphi, P))
\end{aligned}
$$

```
Category c1 is Category increasing1 = { (>, >), (>=, >=), (<, >=), (<=, >), (==, >), (<>, >) };
Category {} C1 is associate increasing1 with decide;
Category c2 is Category increasing2 = { (>, >), (>=, >=), (<, >=), (<=, >) };
Category {} C2 is associate increasing2 with decide;
Operator [ Boolean ] id Boolean b = b;
Operator [ Boolean ] cte_false Boolean b = false;
Operator [ Boolean ] cte_true Boolean b = true;
Category c3 is Category varying1 = { (id, cte_false) };
Category {} C3 is associate varying1 with decide;
Category c4 is Category varying2 = { (id, cte_true) };
Category {} C4 is associate varying2 with decide;
```
