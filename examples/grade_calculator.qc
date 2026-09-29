!! ============================================
!! Grade Calculator
!! Asks for scores, computes the average,
!! and gives a letter grade.
!! ============================================

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

print  .
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