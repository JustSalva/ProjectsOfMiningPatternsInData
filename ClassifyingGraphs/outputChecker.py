correct = []
correctAccuracy = []
correctClassifications = []
numberOfLines = 0
task1 = "task4.txt"
task2 = "task2.txt"
# complete match ( with frequency and confidence)
foldIndex = -1
with open('./results/'+task2, 'r') as dataset:
    for line in dataset:
        if line != "\n":
            if line.split()[0] == "fold":
                foldIndex += 1
                correct.append([])
            elif line.split()[0] == "accuracy:":
                correctAccuracy.append(float(line.split()[1]))
            elif line.split()[0] == "[-1," or line.split()[0] == "[1," or line.split()[0] == "[-1" or line.split()[0] == "[":
                line = line.replace("[", "")
                line = line.replace("]", "")
                line = line.replace(",", "")
                list = line.split()
                correctClassifications.append(list)
            else:
                correct[foldIndex].append(line)
                numberOfLines += 1
oursAccuracy = []
oursClassifications = []
ours = []
numberOfLinesOurs = 0
foldIndex = -1
with open('./results/'+task1, 'r') as dataset:
    for line in dataset:
        if line != "\n":
            if line.split()[0] == "fold":
                foldIndex += 1
                ours.append([])
            elif line.split()[0] == "accuracy:":
                oursAccuracy.append(float(line.split()[1]))
            elif line.split()[0] == "[-1," or line.split()[0] == "[1," or line.split()[0] == "[-1" or line.split()[0] == "[":
                line = line.replace("[", "")
                line = line.replace("]", "")
                line = line.replace(",", "")
                list = line.split()
                oursClassifications.append(list)
            else:
                ours[foldIndex].append(line)
                numberOfLinesOurs += 1
numberOfTimesFirstTaskIsBetter = 0
numberOfTimesSecondTaskIsBetter = 0
numberOfTimesTasksAreEqual = 0
for fold in range(0, foldIndex+1):
    matches = 0
    notFoundPatterns = 0
    notFoundPatternsWithoutStatistics = 0
    matchesWithoutStatistics = 0
    for line in correct[fold]:
        foundStatistic = False
        found = False
        for l in ours[fold]:
            if line != "\n":
                if line == l and not foundStatistic:
                    foundStatistic = True
                    matches += 1
                    break
                if line.split("_")[0] == l.split("_")[0] and not found:
                    found = True
                    matchesWithoutStatistics += 1
                    break
        if not foundStatistic:
            notFoundPatterns += 1
            # print(line + "not found!")
        if not found:
            notFoundPatternsWithoutStatistics += 1
    classificationMatches= 0
    for i in range(0, len(correctClassifications[fold])):
        if correctClassifications[fold][i] == oursClassifications[fold][i]:
            classificationMatches += 1
    if correctAccuracy[fold] > oursAccuracy[fold]:
        numberOfTimesFirstTaskIsBetter += 1
    elif correctAccuracy[fold] < oursAccuracy[fold]:
        numberOfTimesSecondTaskIsBetter += 1
    else:
        numberOfTimesTasksAreEqual += 1
    print("fold: "+str(fold+1))
    print("number of matches with statistics: " + str(matches))
    print("number of matches without statistics: " + str(matchesWithoutStatistics))

    print("notFoundPatterns with statistics: " + str(notFoundPatterns))
    print("notFoundPatternsWithoutStatistics: " + str(notFoundPatternsWithoutStatistics))

    print("classificationMatches: " + str(classificationMatches))
print()
print("numberOfTimesFirstTaskIsBetter: " + str(numberOfTimesFirstTaskIsBetter))
print("numberOfTimesSecondTaskIsBetter: " + str(numberOfTimesSecondTaskIsBetter))
print("numberOfTimesTasksAreEqual: " + str(numberOfTimesTasksAreEqual))
print()

print("numberOfLines1: " + str(numberOfLines))
print("numberOfLines2: " + str(numberOfLinesOurs))
