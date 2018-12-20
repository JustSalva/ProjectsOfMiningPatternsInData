correct = []
numberOfLines = 0
task1 = "task2.txt"
task2 = "task3.txt"
# complete match ( with frequency and confidence)
with open('./results/'+task1, 'r') as dataset:
    for line in dataset:
        correct.append(line)
        numberOfLines += 1

ours = []
numberOfLinesOurs = 0
with open('./results/'+task2, 'r') as dataset:
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
        print(line + "not found!")

print(matches)
print(numberOfLines)
print(numberOfLinesOurs)

# complete match ( without frequency and confidence)
with open('./results/'+task1, 'r') as dataset:
    for line in dataset:
        correct.append(line)
        numberOfLines += 1

ours = []
numberOfLinesOurs = 0
with open('./results/'+task2, 'r') as dataset:
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
        print(line + "not found!")

print(matches)
print(numberOfLines)
print(numberOfLinesOurs)
