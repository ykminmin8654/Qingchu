# Qingchu (清楚)

**A programming language where human notation comes first.**

Qingchu is a language built around one principle: **code should read like
human speech and mathematical notation.** It combines speech-like keywords,
math syntax, typed bindings, and context markers into a language that aims
to be readable at a glance.

清楚 (qīngchu) means *clear*, *distinct*, *easy to understand*. That's the goal.

---

## A Quick Look
!! A simple Qingchu program !!

let name = Bob
name ts string

print Hello name_string .

math let price = 10 .
math let quantity = 5 .
math let total = price * quantity .
math print total .

text

Output:
Hello Bob
50

text

An interactive program:
ask What is your name? .
response = name
name ts string

string print Nice to meet you, name .

text

---

## Design Principles

### Human notation, everywhere

Qingchu uses words and math symbols — not programmer punctuation.

- `print`, `ask`, `when`, `define` — speech-like keywords
- `>`, `<`, `+`, `*`, `=` — mathematical notation
- `.` ends a statement, like a sentence
- Indentation opens and closes blocks

### Typed bindings

Every binding has a type, visible at the point of use:
let name = Bob
name ts string

print Hello name_string .

text

A binding is referenced as `name_type` — so `name_string`, `name_number`,
`name_boolean`. The type is part of the name.

### Context markers

`math` and `string` drop the type suffix, so code reads naturally:
math let total = price * quantity .
math print total .

text

Inside `math`, `total` means `total_number`. Inside `string`, `name` means
`name_string`. This is the whole purpose of the context markers.

### Constants
let pi = 3.14
pi ts constant number

text

### Speech-like control flow
math when i < 3
print Count: i .
new i = i + 1 .

text

---

## Language Reference

### Declarations
let name = Bob
name ts string

text

`let` creates a binding. The `ts` line declares its type.

**Abbreviations:** `ts` = `type_is`, `ats` = `above_type_is`, `const` = `constant`.

**Modifiers:** `constant` / `const` — the binding can't be reassigned.

### Context markers

| Keyword | Effect |
|---|---|
| `math` | Bare names resolve to `_number` |
| `string` | Bare names resolve to `_string` |

### Output
print Hello World .

text

Everything between `print` and ` .` is content. Words that match a declared
binding are resolved; everything else prints as text.

### Input
ask What is your name? .
response = name
name ts string

text

`ask` prints the question, waits for input, and binds the answer.

### Reassignment
math new count = count + 1 .

text

`new` reassigns an existing binding. It requires a context marker.

### Conditionals
math if age > 18
print You are an adult .
else
print You are a minor .

text

### Loops
math when i < 3
print i .
new i = i + 1 .

text

### Functions
define double(x) as x * 2
x ts number
double ts number

text
define square(x) as
x ts number
square ts number

let result = x_number * x_number
result ts number

calculate result_number .

text

### Comments
!! This is a single-line comment !!

!!! This is a
multi-line comment
!!!

text

### Escapes

`\` escapes the next character:

| Write | Prints |
|---|---|
| `\.` | `.` |
| `\_` | `_` |
| `\\` | `\` |

---

## Types

| Type | Description |
|---|---|
| `number` | Numeric values |
| `string` | Text |
| `boolean` | `true` / `false` |

Modifiers: `constant` / `const` — cannot be reassigned.

---

## Errors

Qingchu errors are specific and readable:
error missing terminator on line 1(print statement)
error undefined variable on line 5(print statement)
error cannot reassign constant on line 12(new statement)

text

The format is:
error <what went wrong> on line <N>(<which statement>)

text

---

## Building and Running

Requires **Java 17 or higher**.

### Compile

```sh
javac -encoding UTF-8 -d out src/main/java/qingchu/**/*.java
On Windows PowerShell:

powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })
Run a file
sh
java -cp out qingchu.Main run examples/hello.qc
Run the REPL
sh
java -cp out qingchu.Main
Examples
See the examples/ folder:

File	What it shows
hello.qc	Hello world
vars.qc	Variables and types
loop.qc	A when loop
if.qc	Branching
grade_calculator.qc	An interactive program that asks for scores and computes a grade
everything.qc	Exercises every language feature
The grade calculator in full:

text
!! Grade Calculator !!

print Grade Calculator .
print Enter scores one at a time. Type 0 to finish. .

math let total = 0 .
math let count = 0 .

ask Enter a score (0 to finish): .
    response = score
        score ts number

math when score > 0
    math new total = total + score .
    math new count = count + 1 .

    ask Enter a score (0 to finish): .
        response = score
            score ts number

math let average = total / count .

print .
print Your results: .
math print Total points: total .
math print Number of scores: count .
math print Average: average .

math if average > 89
    print Letter grade: A .
else
    math if average > 79
        print Letter grade: B .
    else
        math if average > 69
            print Letter grade: C .
        else
            math if average > 59
                print Letter grade: D .
            else
                print Letter grade: F .

print Done. .
Current Status
v0.1.0 — Core language complete.

Working
let — declarations with ts type blocks

math / string — context markers

print — output (raw content until .)

ask — input

new — reassignment

if / else — branching

when — loops

define / calculate — functions

constant modifier

Comments — !! and !!!

Escapes — \

Error messages with line numbers and statement context

Planned
Function calling (parsing works; execution pending)

Lists and maps

code_* embedding modes (LaTeX, Python, Java, HTML, CSS)

LaTeX ↔ Unicode conversion

Standard library (rounding, string functions)

Why "Qingchu"?
清楚 (qīngchu) means clear, distinct, easy to understand.

That's the goal: a language where the code is clear at a glance.

Project Structure
text
qingchu/
├── src/main/java/qingchu/
│   ├── Main.java              — entry point
│   ├── lexer/                 — text → tokens
│   ├── error/                 — error types
│   ├── parse/                 — tokens → AST
│   └── runtime/               — runs the AST
├── examples/                  — sample .qc programs
├── docs/                      — website and documentation
└── README.md
License
MIT — see LICENSE.

Links
GitHub: github.com/ykminmin8654/Qingchu

Website: innovationearthprojects.com/qingchu