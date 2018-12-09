correct = []
with open('./examples/task1_small_5_5.txt', 'r') as dataset:
    for line in dataset:
        correct.append(line)

ours = []
with open('./solution1', 'r') as dataset:
    for line in dataset:
        ours.append(line)
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