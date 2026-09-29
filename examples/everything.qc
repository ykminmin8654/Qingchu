!! ============================================
!! Qingchu — Everything Test
!! ============================================

!! --- 1. Comments --- !!
!! This is a single-line comment !!

!!! This is a multi-line comment
    it continues here
!!!

!! --- 2. Simple let + print --- !!
let greeting = Hello
    greeting ts string

print greeting_string .

!! --- 3. Math context --- !!
math let price = 10 .
math let quantity = 5 .
math let total = price * quantity .
math print total .

!! --- 4. String context --- !!
string let first = Alice .
string let last = Smith .
string print Full name: first last .

!! --- 5. Constant --- !!
let pi = 3.14
    pi ts constant number

print pi_constant_number .

!! --- 6. If / Else --- !!
let age = 25
    age ts number

math if age > 18
    print You are an adult .
else
    print You are a minor .

!! --- 7. Nested if --- !!
let score = 15
    score ts number

math if score > 18
    print grade A .
else
    math if score > 12
        print grade B .
    else
        print grade C .

!! --- 8. When loop --- !!
let i = 0
    i ts number

math when i < 3
    print Count: i .
    new i = i + 1 .

!! --- 9. Countdown --- !!
let n = 5
    n ts number

math when n > 0
    print n .
    new n = n - 1 .

print Liftoff .

!! --- 10. Define a function --- !!
define double(x) as x * 2
    x ts number
    double ts number

!! --- 11. Define with block body --- !!
define square(x) as
    x ts number
    square ts number

    let result = x_number * x_number
        result ts number

    calculate result_number .

!! --- 12. Ask for input --- !!
ask What is your name? .
    response = name
        name ts string

string print Nice to meet you, name .

!! --- 13. Final --- !!
print All tests complete .