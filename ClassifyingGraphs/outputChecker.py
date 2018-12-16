correct = []
numberOfLines = 0
with open('./examples/task1_small_5_5.txt', 'r') as dataset:
    for line in dataset:
        correct.append(line)
        numberOfLines += 1

ours = []
numberOfLinesOurs = 0
with open('./solution1', 'r') as dataset:
    for line in dataset:
        ours.append(line)
        numberOfLinesOurs += 1
matches = 0
for line in correct:
    found = False
    for l in ours:
        if line == l:
            found = True
            matches += 1
            break
    if not found:
        print( line + "not found!")

print(matches)
print(numberOfLines)
print(numberOfLinesOurs)
