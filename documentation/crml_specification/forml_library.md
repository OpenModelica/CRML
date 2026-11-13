# 6. FORM-L library

## 6.1 Time periods

**From events occur**

$$
\begin{aligned}
\mathcal{D}: E \\
2^{\mathcal{P}}: \mathit{from}(E) = [E, \emptyset]
\end{aligned}
$$

```
Operator [ Periods ] from Clock E = Periods [ E, new Clock false ];
```

**After events occur**

$$
\begin{aligned}
\mathcal{D}: E \\
2^{\mathcal{P}}: \mathit{after}(E) = {]}E, \emptyset]
\end{aligned}
$$

```
Operator [ Periods ] after Clock E = Periods ] E, new Clock false ];
```

**Before events occur**

$$
\begin{aligned}
\mathcal{D}: E \\
2^{\mathcal{P}}: \mathit{before}(E) = [\emptyset, E[
\end{aligned}
$$

```
Operator [ Periods ] before Clock E = Periods [ new Clock false, E [;
```

**Until events occur**

$$
\begin{aligned}
\mathcal{D}: E \\
2^{\mathcal{P}}: \mathit{until}(E) = [\emptyset, E]
\end{aligned}
$$

```
Operator [ Periods ] until Clock E = Periods [ new Clock false, E ];
```

**While a Boolean is true**

$$
\begin{aligned}
\mathbb{B}: b,\ \mathcal{D}: c \\
2^{\mathcal{P}}: \mathit{during}(b) = [c(b), c(\neg b)]
\end{aligned}
$$

```
Operator [ Periods ] during Boolean b = Periods [ new Clock b, new Clock not b ];
```

**After events occur and before events occur**

$$
\begin{aligned}
\mathcal{D}: E_1,\ \mathcal{D}: E_2 \\
2^{\mathcal{P}}: \mathit{after.before}(E_1, E_2) = {]}E_1, E_2[
\end{aligned}
$$

```
Operator [ Periods ] after Clock E1 before Clock E2 = Periods ] E1, E2 [;
```

**After events occur and until events occur**

$$
\begin{aligned}
\mathcal{D}: E_1,\ \mathcal{D}: E_2 \\
2^{\mathcal{P}}: \mathit{after.until}(E_1, E_2) = {]}E_1, E_2]
\end{aligned}
$$

```
Operator [ Periods ] after Clock E1 until Clock E2 = Periods ] E1, E2 ];
```

**After events occur and for an elapsed time**

$$
\begin{aligned}
\mathcal{D}: E,\ \mathbb{R}: d \\
2^{\mathcal{P}}: \mathit{after.for}(E, d) = {]}E, E + d]
\end{aligned}
$$

```
Operator [ Periods ] after Clock E for Real d = Periods ] E, E + d ];
```

**After events occur and within an elapsed time**

$$
\begin{aligned}
\mathcal{D}: E,\ \mathbb{R}: d \\
2^{\mathcal{P}}: \mathit{after.within}(E, d) = {]}E, E + d[
\end{aligned}
$$

```
Operator [ Periods ] after Clock E within Real d = Periods ] E, E + d [;
```

**From events occur and before events occur**

$$
\begin{aligned}
\mathcal{D}: E_1,\ \mathcal{D}: E_2 \\
2^{\mathcal{P}}: \mathit{from.before}(E_1, E_2) = [E_1, E_2[
\end{aligned}
$$

```
Operator [ Periods ] from Clock E1 before Clock E2 = Periods [ E1, E2 [;
```

**From events occur and until events occur**

$$
\begin{aligned}
\mathcal{D}: E_1,\ \mathcal{D}: E_2 \\
2^{\mathcal{P}}: \mathit{from.until}(E_1, E_2) = [E_1, E_2]
\end{aligned}
$$

```
Operator [ Periods ] from Clock E1 until Clock E2 = Periods [ E1, E2 ];
```

**From events occur and for an elapsed time**

$$
\begin{aligned}
\mathcal{D}: E,\ \mathbb{R}: d \\
2^{\mathcal{P}}: \mathit{from.for}(E, d) = [E, E + d]
\end{aligned}
$$

```
Operator [ Periods ] from Clock E for Real d = Periods [ E, E + d ];
```

**From events occur and within an elapsed time**

$$
\begin{aligned}
\mathcal{D}: E,\ \mathbb{R}: d \\
2^{\mathcal{P}}: \mathit{from.within}(E, d) = [E, E + d[
\end{aligned}
$$

```
Operator [ Periods ] from Clock E within Real d = Periods [ E, E + d [;
```

**When events occur**

$$
\begin{aligned}
\mathcal{D}: E \\
2^{\mathcal{P}}: \mathit{when}(E) = [E, E]
\end{aligned}
$$

```
Operator [ Periods ] when Clock E = Periods [ E, E ];
```

## 6.2 Requirements

**Checking that a requirement is satisfied at the end of a time period**

$$
\begin{aligned}
2^{\mathcal{P}}: P,\ \mathbb{B}: b,\ \mathcal{C}(\mathbb{B} \longrightarrow \mathbb{B}): \mathit{varying1} \\
\mathbb{B}: \mathit{check.at.end}(P, b) = \mathit{check.over}(\mathit{varying1}(b), P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check at end Boolean b = check varying1 id b over P;
```

**Checking that a requirement is satisfied at any time instant of a time period**

$$
\begin{aligned}
2^{\mathcal{P}}: P,\ \mathbb{B}: b,\ \mathcal{C}(\mathbb{B} \longrightarrow \mathbb{B}): \mathit{varying2} \\
\mathbb{B}: \mathit{check.anytime}(P, b) = \mathit{check.over}(\mathit{varying2}(b), P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check anytime Boolean b = check varying2 id b over P;
```

**Ensuring that a requirement is satisfied all along a time period**

$$
\begin{aligned}
2^{\mathcal{P}}: P,\ \mathbb{B}: b \\
\mathbb{B}: \mathit{ensure}(P, b) = \mathit{check.over}(\mathit{count.over}(\mathit{becomes.true}(b), P) = 0, P) \wedge \mathit{check.anytime}(b, P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P ensure Boolean b = (check (count (b becomes true) == 0) over P) and (P check anytime b);
```

**Checking that the number of event occurrences at the end of a time period is lower or higher than a threshold**

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathcal{D}: E,\ \mathbb{N}: n,\ \mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1} \\
&\mathbb{B}: \mathit{check.count}(P, E, n) = \mathit{check.over}(\mathit{count.inside}(E, P)\ \mathit{increasing1}(<)\ n,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check count Clock E '<' constant Integer n = check ((count E inside P) increasing1 < n) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathcal{D}: E,\ \mathbb{N}: n,\ \mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1} \\
&\mathbb{B}: \mathit{check.count}(P, E, n) = \mathit{check.over}(\mathit{count.inside}(E, P)\ \mathit{increasing1}(\leq)\ n,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check count Clock E '<=' constant Integer n = check ((count E inside P) increasing1 <= n) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathcal{D}: E,\ \mathbb{N}: n,\ \mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1} \\
&\mathbb{B}: \mathit{check.count}(P, E, n) = \mathit{check.over}(\mathit{count.inside}(E, P)\ \mathit{increasing1}(>)\ n,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check count Clock E '>' constant Integer n = check ((count E inside P) increasing1 > n) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathcal{D}: E,\ \mathbb{N}: n,\ \mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1} \\
&\mathbb{B}: \mathit{check.count}(P, E, n) = \mathit{check.over}(\mathit{count.inside}(E, P)\ \mathit{increasing1}(\geq)\ n,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check count Clock E '>=' constant Integer n = check ((count E inside P) increasing1 >= n) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathcal{D}: E,\ \mathbb{N}: n,\ \mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1} \\
&\mathbb{B}: \mathit{check.count}(P, E, n) = \mathit{check.over}(\mathit{count.inside}(E, P)\ \mathit{increasing1}(=)\ n,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check count Clock E '==' constant Integer n = check ((count E inside P) increasing1 == n) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathcal{D}: E,\ \mathbb{N}: n,\ \mathcal{C}(\mathbb{N}^2 \longrightarrow \mathbb{B}): \mathit{increasing1} \\
&\mathbb{B}: \mathit{check.count}(P, E, n) = \mathit{check.over}(\mathit{count.inside}(E, P)\ \mathit{increasing1}(\neq)\ n,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check count Clock E '<>' constant Integer n = check ((count E inside P) increasing1 <> n) over P;
```

**Checking that the duration of a condition at the end of a time period is lower or higher than a threshold**

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathbb{B}: b,\ \mathbb{R}: d,\ \mathcal{C}(\mathbb{R}^2 \longrightarrow \mathbb{B}): \mathit{increasing2} \\
&\mathbb{B}: \mathit{check.duration}(P, b, d) = \mathit{check.over}(\mathit{duration}(b, P)\ \mathit{increasing2}(<)\ d,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check duration Boolean b '<' constant Real d = check ((duration b on P) increasing2 < d) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathbb{B}: b,\ \mathbb{R}: d,\ \mathcal{C}(\mathbb{R}^2 \longrightarrow \mathbb{B}): \mathit{increasing2} \\
&\mathbb{B}: \mathit{check.duration}(P, b, d) = \mathit{check.over}(\mathit{duration}(b, P)\ \mathit{increasing2}(\leq)\ d,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check duration Boolean b '<=' constant Real d = check ((duration b on P) increasing2 <= d) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathbb{B}: b,\ \mathbb{R}: d,\ \mathcal{C}(\mathbb{R}^2 \longrightarrow \mathbb{B}): \mathit{increasing2} \\
&\mathbb{B}: \mathit{check.duration}(P, b, d) = \mathit{check.over}(\mathit{duration}(b, P)\ \mathit{increasing2}(>)\ d,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check duration Boolean b '>' constant Real d = check ((duration b on P) increasing2 > d) over P;
```

$$
\begin{aligned}
&2^{\mathcal{P}}: P,\ \mathbb{B}: b,\ \mathbb{R}: d,\ \mathcal{C}(\mathbb{R}^2 \longrightarrow \mathbb{B}): \mathit{increasing2} \\
&\mathbb{B}: \mathit{check.duration}(P, b, d) = \mathit{check.over}(\mathit{duration}(b, P)\ \mathit{increasing2}(\geq)\ d,\ P)
\end{aligned}
$$

```
Operator [ Boolean ] Periods P check duration Boolean b '>=' constant Real d = check ((duration b on P) increasing2 >= d) over P;
```
