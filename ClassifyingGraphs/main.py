"""The main program that runs gSpan. Two examples are provided"""
# -*- coding=utf-8 -*-
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os
import sys
from math import log

import numpy
from sklearn import metrics
from sklearn import *

from gspan_mining import gSpan
from gspan_mining import GraphDatabase

import time


class PatternGraphs:
    """
    This template class is used to define a task for the gSpan implementation.
    You should not modify this class but extend it to define new tasks
    """

    def __init__(self, database):
        # A list of subsets of graph identifiers.
        # Is used to specify different groups of graphs (classes and training/test sets).
        # The gid-subsets parameter in the pruning and store function will contain for each subset, all the occurrences
        # in which the examined pattern is present.
        self.gid_subsets = []

        self.database = database  # A graphdatabase instance: contains the data for the problem.

    def store(self, dfs_code, gid_subsets):
        """
        Code to be executed to store the pattern, if desired.
        The function will only be called for patterns that have not been pruned.
        In correlated pattern mining, we may prune based on confidence, but then check further conditions before storing.
        :param dfs_code: the dfs code of the pattern (as a string).
        :param gid_subsets: the cover (set of graph ids in which the pattern is present) for each subset in self.gid_subsets
        """
        print("Please implement the store function in a subclass for a specific mining task!")

    def prune(self, gid_subsets):
        """
        prune function: used by the gSpan algorithm to know if a pattern (and its children in the search tree)
        should be pruned.
        :param gid_subsets: A list of the cover of the pattern for each subset.
        :return: true if the pattern should be pruned, false otherwise.
        """
        print("Please implement the prune function in a subclass for a specific mining task!")


class FrequentPositiveGraphs(PatternGraphs):
    """
    Finds the frequent (support >= minSup) subGraphs among the positive graphs.
    This class provides a method to build a feature matrix for each subset.
    """

    def __init__(self, minSup, database, subsets):
        """
        Initialize the task.
        :param minSup: the minimum positive support
        :param database: the graph database
        :param subsets: the subsets (train and/or test sets for positive and negative class) of graph ids.
        """
        super().__init__(database)
        self.patterns = []
        # The patterns found in the end (as dfs codes represented by strings) with their cover (as a list of graph ids).
        self.minSup = minSup
        self.gid_subsets = subsets  # i.e. transaction number

    # Stores any pattern found that has not been pruned
    def store(self, dfs_code, gid_subsets):
        self.patterns.append((dfs_code, gid_subsets))

    # Prunes any pattern that is not frequent in the positive class
    def prune(self, gid_subsets):
        # first subset is the set of positive ids
        return len(gid_subsets[0]) < self.minSup

    # creates a column for a feature matrix
    def create_fm_col(self, all_gids, subset_gids):
        subset_gids = set(subset_gids)
        bools = []
        for i, val in enumerate(all_gids):
            if val in subset_gids:
                bools.append(1)
            else:
                bools.append(0)
        return bools

    # return a feature matrix for each subset of examples, in which the columns correspond to patterns
    # and the rows to examples in the subset.
    def get_feature_matrices(self):
        matrices = [[] for _ in self.gid_subsets]
        for pattern, gid_subsets in self.patterns:
            for i, gid_subset in enumerate(gid_subsets):
                matrices[i].append(self.create_fm_col(self.gid_subsets[i], gid_subset))
        return [numpy.array(matrix).transpose() for matrix in matrices]


class K_MostConfidentAndFrequentPositiveSubGraphs(PatternGraphs):
    """
    Finds the frequent (support >= minSup) subGraphs among the positive graphs.
    This class provides a method to build a feature matrix for each subset.
    """

    def __init__(self, minFrequency, database, subsets, k, considerAlsoNegative=False, useInfoGain=False):
        """
        Initialize the task.
        :param minSup: the minimum positive support
        :param database: the graph database
        :param subsets: the subsets (train and/or test sets for positive and negative class) of graph ids.
        """
        super().__init__(database)
        self.patterns = []
        # The patterns found in the end (as dfs codes represented by strings) with their cover (as a list of graph ids).
        self.minFrequency = minFrequency
        self.gid_subsets = subsets  # i.e. transaction number
        self.k = k
        self.numberOfKMost = 0
        self.mostConfidentAndFrequentKValues = dict()  # key = confident, value ordered set of frequencies
        self.minConfidence = 0
        self.orderedListOfConfidenceValues = []
        self.orderedListOfFrequencyValuesForMinConfidence = []
        self.initialFrequency = True
        self.considerAlsoNegative = considerAlsoNegative
        self.useInfoGain = useInfoGain
        if self.useInfoGain:
            self.P = len(subsets[0])
            self.N = len(subsets[2])
            self.P_PLUS_N = self.P + self.N
            self.IMP_P_DIVIDED_BY_P_PLUS_N = self.imp(float(self.P) / float(self.P_PLUS_N))

    def scoringFunction(self, numerator, p, n):
        if not self.useInfoGain:
            return float(numerator) / float(p + n)
        else:
            # info gain

            # if self.P == p and self.N == n:
            #     return 0
            # b = self.IMP_P_DIVIDED_BY_P_PLUS_N - (float(p + n) / float(self.P_PLUS_N)) * self.imp(
            #     (float(p) / float(p + n)))
            # c = (float(self.P_PLUS_N - p - n) / float(self.P_PLUS_N))
            # d = float(self.P - p) / float(self.P_PLUS_N - p - n)
            # a = (b - c * self.imp(d))
            # return a

            # laplace

            return float(numerator + 1) / float(p + n + 2)

            # if numerator == n:
            #     return float(n * self.N) / float((p + n)*self.P_PLUS_N)
            # else:
            #     return float(p *self.P) / float((p + n) * self.P_PLUS_N)

    def imp(self, x):
        if x == 0 or x == 1:
            return 0
        return -(x * log(x, 2)) - ((1 - x) * log(1 - x, 2))

    # Stores any pattern found that has not been pruned
    def store(self, dfs_code, gid_subsets):
        p = len(gid_subsets[0])
        n = len(gid_subsets[len(gid_subsets) // 2])
        isPositivePattern = True
        confidence = self.scoringFunction(p, p, n)
        if self.considerAlsoNegative:
            confidenceNegative = self.scoringFunction(n, p, n)
            if confidenceNegative > confidence:
                confidence = confidenceNegative
                isPositivePattern = False
        frequency = n + p
        p_test = len(gid_subsets[len(gid_subsets) // 2 - 1])
        n_test = len(gid_subsets[len(gid_subsets) - 1])
        if self.numberOfKMost == 0:
            self.minConfidence = confidence
            self.orderedListOfFrequencyValuesForMinConfidence.append(frequency)

        if self.numberOfKMost < self.k:
            self.patterns.append((dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern))
            if confidence < self.minConfidence:
                self.minConfidence = confidence
                self.orderedListOfFrequencyValuesForMinConfidence.clear()
                self.orderedListOfFrequencyValuesForMinConfidence.append(frequency)
                self.initialFrequency = False
            self.addNewConfidenceAndFrequencyValues(frequency, confidence)

        elif confidence in self.mostConfidentAndFrequentKValues:
            if frequency in self.mostConfidentAndFrequentKValues[confidence]:
                self.patterns.append((dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern))
            else:
                if self.updateFrequency(frequency, confidence):
                    self.patterns.append((dfs_code, gid_subsets, confidence, frequency,
                                          p_test, n_test, isPositivePattern))
        else:
            if self.updateConfidence(frequency, confidence):
                self.patterns.append((dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern))

    def addNewConfidenceAndFrequencyValues(self, newFrequency, newConfidence):
        if newConfidence in self.mostConfidentAndFrequentKValues:
            if newFrequency not in self.mostConfidentAndFrequentKValues[newConfidence]:
                self.numberOfKMost += 1
                self.mostConfidentAndFrequentKValues[newConfidence].add(newFrequency)
                if newConfidence == self.minConfidence:
                    self.insertElementInOrderedList(self.orderedListOfFrequencyValuesForMinConfidence, newFrequency)
        else:
            self.numberOfKMost += 1
            self.mostConfidentAndFrequentKValues[newConfidence] = set()
            self.mostConfidentAndFrequentKValues[newConfidence].add(newFrequency)
            self.insertElementInOrderedList(self.orderedListOfConfidenceValues, newConfidence)
            if newConfidence == self.minConfidence and \
                    newFrequency not in self.mostConfidentAndFrequentKValues[newConfidence]:
                self.insertElementInOrderedList(self.orderedListOfFrequencyValuesForMinConfidence, newFrequency)

    def updateConfidence(self, newFrequency, newConfidence):
        if newConfidence > self.minConfidence:
            self.mostConfidentAndFrequentKValues[newConfidence] = set()
            self.mostConfidentAndFrequentKValues[newConfidence].add(newFrequency)
            self.insertElementInOrderedList(self.orderedListOfConfidenceValues, newConfidence)

            # remove old mins
            self.removeMinConfidenceAndFrequency()
            return True
        else:
            return False

    def removeMinConfidenceAndFrequency(self):
        set = self.mostConfidentAndFrequentKValues[self.minConfidence]
        set.remove(self.orderedListOfFrequencyValuesForMinConfidence[0])
        self.orderedListOfFrequencyValuesForMinConfidence.pop(0)

        if len(self.mostConfidentAndFrequentKValues[self.minConfidence]) == 0:
            del self.mostConfidentAndFrequentKValues[self.minConfidence]
            self.orderedListOfConfidenceValues.pop(0)
            self.minConfidence = self.orderedListOfConfidenceValues[0]
            self.orderedListOfFrequencyValuesForMinConfidence = []
            # create new list of min frequency elements
            for element in self.mostConfidentAndFrequentKValues[self.minConfidence]:
                self.insertElementInOrderedList(self.orderedListOfFrequencyValuesForMinConfidence, element)

    def updateFrequency(self, newFrequency, confidence):

        if newFrequency > self.minFrequency:
            # add new frequency in the set and eventually in the min list
            self.mostConfidentAndFrequentKValues[confidence].add(newFrequency)
            if confidence == self.minConfidence:
                self.insertElementInOrderedList(self.orderedListOfFrequencyValuesForMinConfidence, newFrequency)

            # remove old min frequency and confidence values
            self.removeMinConfidenceAndFrequency()

        if newFrequency >= self.minFrequency:
            return True
        else:
            return False

    def insertElementInOrderedList(self, list, newElement):
        if len(list) == 0:
            list.append(newElement)
        else:
            inserted = False
            for i in range(0, len(list)):
                if newElement < list[i]:
                    list.insert(i, newElement)
                    inserted = True
                    break
            if not inserted:
                list.append(newElement)

    # Prunes any pattern that is not frequent in the positive class
    def prune(self, gid_subsets):
        p = len(gid_subsets[0])
        n = len(gid_subsets[len(gid_subsets) // 2])
        p_test = len(gid_subsets[len(gid_subsets) // 2 - 1])
        n_test = len(gid_subsets[len(gid_subsets) - 1])
        frequency = n + p
        if self.numberOfKMost < self.k:
            return frequency < self.minFrequency
        if p + n > 0:
            confidence = self.scoringFunction(p, p, n)
            if self.considerAlsoNegative:
                confidenceNegative = self.scoringFunction(n, p, n)
                if confidenceNegative > confidence:
                    confidence = confidenceNegative
        else:
            confidence = -1

        if confidence == self.minConfidence and frequency < self.minFrequency:
            return frequency < self.orderedListOfFrequencyValuesForMinConfidence[0]
        if frequency < self.minFrequency:
            return True
        #  confidence does not have anti-monotone property.
        # if confidence < self.minConfidence:
        #     return True
        return False
        # return frequency < self.minFrequency

    # creates a column for a feature matrix
    def create_fm_col(self, all_gids, subset_gids):
        subset_gids = set(subset_gids)
        bools = []
        for i, val in enumerate(all_gids):
            if val in subset_gids:
                bools.append(1)
            else:
                bools.append(0)
        return bools

    # return a feature matrix for each subset of examples, in which the columns correspond to patterns
    # and the rows to examples in the subset.
    def get_feature_matrices(self):
        matrices = [[] for _ in self.gid_subsets]
        for pattern, gid_subsets, _, _, _, _, _ in self.patterns:
            for i, gid_subset in enumerate(gid_subsets):
                matrices[i].append(self.create_fm_col(self.gid_subsets[i], gid_subset))
        return [numpy.array(matrix).transpose() for matrix in matrices]


def task1():
    """
    Runs gSpan with the specified positive and negative graphs, finds all frequent subGraphs in the positive class
    with a minimum positive support of minSup and prints them.
    """

    args = sys.argv
    database_file_name_pos = args[1]  # First parameter: path to positive class file
    database_file_name_neg = args[2]  # Second parameter: path to negative class file
    k = int(args[4])  # Third parameter: k
    minFrequency = int(args[4])  # Fourth parameter: minimum frequency

    if not os.path.exists(database_file_name_pos):
        print('{} does not exist.'.format(database_file_name_pos))
        sys.exit()
    if not os.path.exists(database_file_name_neg):
        print('{} does not exist.'.format(database_file_name_neg))
        sys.exit()

    graph_database = GraphDatabase()  # Graph database object
    # Reading positive graphs, adding them to database and getting ids
    pos_ids = graph_database.read_graphs(database_file_name_pos)
    # Reading negative graphs, adding them to database and getting ids
    neg_ids = graph_database.read_graphs(database_file_name_neg)

    task = K_MostConfidentAndFrequentPositiveSubGraphs(minFrequency, graph_database, [pos_ids, neg_ids], k, False)

    gSpan(task).run()  # Running gSpan

    # with open('./solution1', 'w') as file:
    firstLine = True
    result = ""
    # Printing frequent patterns along with their positive support:
    with open('./results/task1.txt', 'w') as dataset:
        for confidenceLevel in reversed(task.orderedListOfConfidenceValues):
            for pattern, gid_subsets, confidence, frequency, _, _, _ in task.patterns:
                if confidence == confidenceLevel:
                    toPrint = False
                    if confidence > task.minConfidence:
                        toPrint = True
                    elif confidence == task.minConfidence:
                        if frequency >= task.orderedListOfFrequencyValuesForMinConfidence[0]:
                            toPrint = True

                    if toPrint:
                        if not firstLine:
                            result += '\n'
                        else:
                            firstLine = False
                        result += '{}_{}_{}'.format(pattern, confidence, frequency)
            # print(result, file=file, end='')
        print(result, end='', file= dataset)


def task2():
    """
    Runs gSpan with the specified positive and negative graphs; finds all frequent sub-graphs in the training subset of
    the positive class with a minimum support of minSup.
    Uses the patterns found to train a naive bayesian classifier using Scikit-learn and evaluates its performances on
    the test set.
    Performs a k-fold cross-validation.
    """

    args = sys.argv
    database_file_name_pos = args[1]  # First parameter: path to positive class file
    database_file_name_neg = args[2]  # Second parameter: path to negative class file
    k = int(args[3])  # Third parameter: minimum support (note: this parameter will be k in case of top-k mining) 0
    minFrequency = int(args[4])
    nfolds = int(args[5])  # Fourth parameter: number of folds to use in the k-fold cross-validation.
    with open('./results/task2.txt', 'w') as dataset:
        if not os.path.exists(database_file_name_pos):
            print('{} does not exist.'.format(database_file_name_pos))
            sys.exit()
        if not os.path.exists(database_file_name_neg):
            print('{} does not exist.'.format(database_file_name_neg))
            sys.exit()

        graph_database = GraphDatabase()  # Graph database object
        pos_ids = graph_database.read_graphs(
            database_file_name_pos)  # Reading positive graphs, adding them to database and getting ids
        neg_ids = graph_database.read_graphs(
            database_file_name_neg)  # Reading negative graphs, adding them to database and getting ids

        # If less than two folds: using the same set as training and test set
        # (note this is not an accurate way to evaluate the performances!)
        if nfolds < 2:
            subsets = [
                pos_ids,  # Positive training set
                pos_ids,  # Positive test set
                neg_ids,  # Negative training set
                neg_ids  # Negative test set
            ]
            # Printing fold number:
            print('fold {}'.format(1))
            train_and_evaluate(minFrequency, graph_database, subsets, k, dataset)

        # Otherwise: performs k-fold cross-validation:
        else:
            pos_fold_size = len(pos_ids) // nfolds
            neg_fold_size = len(neg_ids) // nfolds
            for i in range(nfolds):
                # Use fold as test set, the others as training set for each class;
                # identify all the subsets to be maintained by the graph mining algorithm.
                subsets = [
                    numpy.concatenate((pos_ids[:i * pos_fold_size], pos_ids[(i + 1) * pos_fold_size:])),
                    # Positive training set
                    pos_ids[i * pos_fold_size:(i + 1) * pos_fold_size],  # Positive test set
                    numpy.concatenate((neg_ids[:i * neg_fold_size], neg_ids[(i + 1) * neg_fold_size:])),
                    # Negative training set
                    neg_ids[i * neg_fold_size:(i + 1) * neg_fold_size],  # Negative test set
                ]
                # Printing fold number:
                print('fold {}'.format(i + 1), file= dataset)
                train_and_evaluate(minFrequency, graph_database, subsets, k, dataset)


def train_and_evaluate(minFrequency, database, subsets, k, dataset):
    task = K_MostConfidentAndFrequentPositiveSubGraphs(minFrequency, database, subsets, k, False)

    gSpan(task).run()  # Running gSpan

    # Creating feature matrices for training and testing:
    features = task.get_feature_matrices()
    train_fm = numpy.concatenate((features[0], features[2]))  # Training feature matrix
    train_labels = numpy.concatenate(
        (numpy.full(len(features[0]), 1, dtype=int), numpy.full(len(features[2]), -1, dtype=int)))  # Training labels
    test_fm = numpy.concatenate((features[1], features[3]))  # Testing feature matrix
    test_labels = numpy.concatenate(
        (numpy.full(len(features[1]), 1, dtype=int), numpy.full(len(features[3]), -1, dtype=int)))  # Testing labels

    classifier = tree.DecisionTreeClassifier(random_state=1)  # Creating model object
    classifier.fit(train_fm, train_labels)  # Training model

    predicted = classifier.predict(test_fm)  # Using model to predict labels of testing data

    accuracy = metrics.accuracy_score(test_labels, predicted)  # Computing accuracy:

    # Printing frequent patterns along with their positive support:
    firstLine = True
    result = ""
    # Printing frequent patterns along with their positive support:
    for confidenceLevel in reversed(task.orderedListOfConfidenceValues):
        for pattern, gid_subsets, confidence, frequency, _, _, _ in task.patterns:
            if confidence == confidenceLevel:
                toPrint = False
                if confidence > task.minConfidence:
                    toPrint = True
                elif confidence == task.minConfidence:
                    if frequency >= task.orderedListOfFrequencyValuesForMinConfidence[0]:
                        toPrint = True

                if toPrint:
                    if not firstLine:
                        result += '\n'
                    else:
                        firstLine = False
                    result += '{}_{}_{}'.format(pattern, confidence, frequency)

    print(result, file= dataset)
    # printing classification results:
    print(predicted, file= dataset)
    print('accuracy: {}'.format(accuracy), file= dataset)
    print("",file= dataset)  # Blank line to indicate end of fold.


def task3():
    args = sys.argv
    database_file_name_pos = args[1]  # First parameter: path to positive class file
    database_file_name_neg = args[2]  # Second parameter: path to negative class file
    k = int(args[3])  # Third parameter: minimum support (note: this parameter will be k in case of top-k mining) 0
    minFrequency = int(args[4])
    nfolds = int(args[5])  # Fifth parameter: number of folds to use in the k-fold cross-validation.
    with open('./results/task3.txt', 'w') as dataset:
        if not os.path.exists(database_file_name_pos):
            print('{} does not exist.'.format(database_file_name_pos))
            sys.exit()
        if not os.path.exists(database_file_name_neg):
            print('{} does not exist.'.format(database_file_name_neg))
            sys.exit()

        graph_database = GraphDatabase()  # Graph database object
        pos_ids = graph_database.read_graphs(
            database_file_name_pos)  # Reading positive graphs, adding them to database and getting ids
        neg_ids = graph_database.read_graphs(
            database_file_name_neg)  # Reading negative graphs, adding them to database and getting ids

        # If less than two folds: using the same set as training and test set
        # (note this is not an accurate way to evaluate the performances!)
        if nfolds < 2:
            subsets = [
                pos_ids,  # Positive training set
                pos_ids,  # Positive test set
                neg_ids,  # Negative training set
                neg_ids  # Negative test set
            ]
            # Printing fold number:
            print('fold {}'.format(1), file=dataset)
            train_and_evaluate(minFrequency, graph_database, subsets, k, dataset)

        # Otherwise: performs k-fold cross-validation:
        else:
            pos_fold_size = len(pos_ids) // nfolds
            neg_fold_size = len(neg_ids) // nfolds
            for i in range(0, nfolds):
                # Use fold as test set, the others as training set for each class;
                # identify all the subsets to be maintained by the graph mining algorithm.
                subsets = [
                    numpy.concatenate((pos_ids[:i * pos_fold_size], pos_ids[(i + 1) * pos_fold_size:])),
                    # Positive training set
                    pos_ids[i * pos_fold_size:(i + 1) * pos_fold_size],  # Positive test set
                    numpy.concatenate((neg_ids[:i * neg_fold_size], neg_ids[(i + 1) * neg_fold_size:])),
                    # Negative training set
                    neg_ids[i * neg_fold_size:(i + 1) * neg_fold_size],  # Negative test set
                ]
                # Printing fold number:
                print('fold {}'.format(i + 1), file= dataset)
                train_and_evaluate_task3(minFrequency, graph_database, subsets, k, dataset)


def sortList(list):
    maxConfidence = 0
    maxFrequency = 0
    for element in list:
        if maxConfidence < element[2]:
            maxConfidence = element[2]
            maxFrequency = element[3]
        if maxConfidence == element[2]:
            if maxFrequency < element[3]:
                maxFrequency = element[3]
    result = []
    for elem in list:
        if maxConfidence == elem[2] and maxFrequency == elem[3]:
            result.append(elem)
    return result


def train_and_evaluate_task3(minFrequency, database, subsets, k, dataset):
    rules = []
    task = K_MostConfidentAndFrequentPositiveSubGraphs(minFrequency, database, subsets, 1, True)
    pos_ids = subsets[0]
    pos_idsTest = subsets[1]
    neg_ids = subsets[2]
    neg_idsTest = subsets[3]
    for _ in range(0, k):
        gSpan(task).run()  # Running gSpan

        numberOfPatternsFound = len(task.patterns)
        if numberOfPatternsFound == 0:
            break
        patterns = sortList(task.patterns)
        numberOfPatternsFound = len(patterns)
        pattern = patterns[0]
        if numberOfPatternsFound == 1:
            #  N.B. rule format: (dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern)
            rules.append(pattern)
        elif numberOfPatternsFound > 1:
            for i in range(1, numberOfPatternsFound):
                if patterns[i] < pattern:
                    pattern = patterns[i]
            rules.append(pattern)

        nextPosIds = []
        for transaction in pos_ids:
            if int(transaction) not in pattern[1][0]:  # == gid_subsets
                nextPosIds.append(int(transaction))
        nextPosIdsTest = []
        for transaction in pos_idsTest:
            if int(transaction) not in pattern[1][1]:  # == gid_subsets
                nextPosIdsTest.append(int(transaction))
        nextNegIds = []
        for transaction in neg_ids:
            if int(transaction) not in pattern[1][2]:  # == gid_subsets
                nextNegIds.append(int(transaction))
        nextNegIdsTest = []
        for transaction in neg_idsTest:
            if int(transaction) not in pattern[1][3]:  # == gid_subsets
                nextNegIdsTest.append(int(transaction))
        projectedSubset = [nextPosIds, nextPosIdsTest, nextNegIds, nextNegIdsTest]
        pos_ids = nextPosIds
        pos_idsTest = nextPosIdsTest
        neg_ids = nextNegIds
        neg_idsTest = nextNegIdsTest
        if len(pos_ids) == 0 and len(neg_ids) == 0:
            break
        task = K_MostConfidentAndFrequentPositiveSubGraphs(minFrequency, database, projectedSubset, 1, True)
    # default class is positive if there are more remaining positive examples, or if there are no remaining patterns
    isDefaultPositive = len(nextPosIds) >= len(nextNegIds)

    # classification
    predicted = []
    correctPredictions = 0
    testPositive = subsets[1]
    testNegative = subsets[3]
    for transaction in testPositive:
        isTransactionPositive = isDefaultPositive
        for rule in rules:
            # N.B. rule format: (dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern)
            if transaction in rule[1][1]:
                if rule[6]:
                    isTransactionPositive = True
                else:
                    isTransactionPositive = False
                break

        if isTransactionPositive:
            predicted.append(1)
            correctPredictions += 1
        else:
            predicted.append(-1)
    for transaction in testNegative:
        isTransactionPositive = isDefaultPositive
        for rule in rules:
            # N.B. rule format: (dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern)
            if transaction in rule[1][3]:
                if rule[6]:
                    isTransactionPositive = True
                else:
                    isTransactionPositive = False
                break

        if isTransactionPositive:
            predicted.append(1)
        else:
            predicted.append(-1)
            correctPredictions += 1
    accuracy = correctPredictions / (len(testPositive) + len(testNegative))

    # Printing frequent patterns along with their positive support:
    firstLine = True
    result = ""
    # Printing frequent patterns along with their positive support:
    for pattern, gid_subsets, confidence, frequency, _, _, _ in rules:
        if not firstLine:
            result += '\n'
        else:
            firstLine = False
        result += '{}_{}_{}'.format(pattern, confidence, frequency)

    print(result, file= dataset)
    # printing classification results:
    print(predicted, file= dataset)
    print('accuracy: {}'.format(accuracy), file= dataset)
    print("", file= dataset)  # Blank line to indicate end of fold.


def task4():
    args = sys.argv
    database_file_name_pos = args[1]  # First parameter: path to positive class file
    database_file_name_neg = args[2]  # Second parameter: path to negative class file
    nfolds = int(args[5]) # Third parameter: number of folds to use in the k-fold cross-validation.

    with open('./results/task4.txt', 'w') as dataset:
        if not os.path.exists(database_file_name_pos):
            print('{} does not exist.'.format(database_file_name_pos))
            sys.exit()
        if not os.path.exists(database_file_name_neg):
            print('{} does not exist.'.format(database_file_name_neg))
            sys.exit()

        graph_database = GraphDatabase()  # Graph database object
        pos_ids = graph_database.read_graphs(
            database_file_name_pos)  # Reading positive graphs, adding them to database and getting ids
        neg_ids = graph_database.read_graphs(
            database_file_name_neg)  # Reading negative graphs, adding them to database and getting ids
        minFrequency = (len(pos_ids) + len(neg_ids)) // 8
        k = minFrequency
        # If less than two folds: using the same set as training and test set
        # (note this is not an accurate way to evaluate the performances!)
        if nfolds < 2:
            subsets = [
                pos_ids,  # Positive training set
                pos_ids,  # Positive test set
                neg_ids,  # Negative training set
                neg_ids  # Negative test set
            ]
            # Printing fold number:
            print('fold {}'.format(1), file= dataset)
            train_and_evaluate(minFrequency, graph_database, subsets, k)

        # Otherwise: performs k-fold cross-validation:
        else:
            pos_fold_size = len(pos_ids) // nfolds
            neg_fold_size = len(neg_ids) // nfolds
            for i in range(0, nfolds):
                # Use fold as test set, the others as training set for each class;
                # identify all the subsets to be maintained by the graph mining algorithm.
                subsets = [
                    numpy.concatenate((pos_ids[:i * pos_fold_size], pos_ids[(i + 1) * pos_fold_size:])),
                    # Positive training set
                    pos_ids[i * pos_fold_size:(i + 1) * pos_fold_size],  # Positive test set
                    numpy.concatenate((neg_ids[:i * neg_fold_size], neg_ids[(i + 1) * neg_fold_size:])),
                    # Negative training set
                    neg_ids[i * neg_fold_size:(i + 1) * neg_fold_size],  # Negative test set
                ]
                # Printing fold number:
                print('fold {}'.format(i + 1), file= dataset)
                train_and_evaluate_task4(minFrequency, graph_database, subsets, k, dataset)


def train_and_evaluate_task4(minFrequency, database, subsets, k, dataset):
    rules = []
    task = K_MostConfidentAndFrequentPositiveSubGraphs(minFrequency, database, subsets, 5, True, True)
    pos_ids = subsets[0]
    pos_idsTest = subsets[1]
    neg_ids = subsets[2]
    neg_idsTest = subsets[3]
    for _ in range(0, k):
        gSpan(task).run()  # Running gSpan

        numberOfPatternsFound = len(task.patterns)
        if numberOfPatternsFound == 0:
            break
        patterns = sortList(task.patterns)
        numberOfPatternsFound = len(patterns)
        pattern = patterns[0]
        if numberOfPatternsFound == 1:
            #  N.B. rule format: (dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern)
            rules.append(pattern)
        elif numberOfPatternsFound > 1:
            for i in range(1, numberOfPatternsFound):
                if patterns[i] < pattern:
                    pattern = patterns[i]
            rules.append(pattern)

        nextPosIds = []
        for transaction in pos_ids:
            if int(transaction) not in pattern[1][0]:  # == gid_subsets
                nextPosIds.append(int(transaction))
        nextPosIdsTest = []
        for transaction in pos_idsTest:
            if int(transaction) not in pattern[1][1]:  # == gid_subsets
                nextPosIdsTest.append(int(transaction))
        nextNegIds = []
        for transaction in neg_ids:
            if int(transaction) not in pattern[1][2]:  # == gid_subsets
                nextNegIds.append(int(transaction))
        nextNegIdsTest = []
        for transaction in neg_idsTest:
            if int(transaction) not in pattern[1][3]:  # == gid_subsets
                nextNegIdsTest.append(int(transaction))
        projectedSubset = [nextPosIds, nextPosIdsTest, nextNegIds, nextNegIdsTest]
        pos_ids = nextPosIds
        pos_idsTest = nextPosIdsTest
        neg_ids = nextNegIds
        neg_idsTest = nextNegIdsTest
        if len(pos_ids) == 0 and len(neg_ids) == 0:
            break
        task = K_MostConfidentAndFrequentPositiveSubGraphs(minFrequency, database, projectedSubset, 5, True, True)
    # default class is positive if there are more remaining positive examples, or if there are no remaining patterns
    isDefaultPositive = len(nextPosIds) >= len(nextNegIds)

    # classification
    predicted = []
    correctPredictions = 0
    testPositive = subsets[1]
    testNegative = subsets[3]
    for transaction in testPositive:
        isTransactionPositive = isDefaultPositive
        for rule in rules:
            # N.B. rule format: (dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern)
            if transaction in rule[1][1]:
                if rule[6]:
                    isTransactionPositive = True
                else:
                    isTransactionPositive = False
                break

        if isTransactionPositive:
            predicted.append(1)
            correctPredictions += 1
        else:
            predicted.append(-1)
    for transaction in testNegative:
        isTransactionPositive = isDefaultPositive
        for rule in rules:
            # N.B. rule format: (dfs_code, gid_subsets, confidence, frequency, p_test, n_test, isPositivePattern)
            if transaction in rule[1][3]:
                if rule[6]:
                    isTransactionPositive = True
                else:
                    isTransactionPositive = False
                break

        if isTransactionPositive:
            predicted.append(1)
        else:
            predicted.append(-1)
            correctPredictions += 1
    accuracy = correctPredictions / (len(testPositive) + len(testNegative))

    # Printing frequent patterns along with their positive support:
    firstLine = True
    result = ""
    # Printing frequent patterns along with their positive support:
    for pattern, gid_subsets, confidence, frequency, _, _, _ in rules:
        if not firstLine:
            result += '\n'
        else:
            firstLine = False
        result += '{}_{}_{}'.format(pattern, confidence, frequency)

    print(result, file= dataset)
    # printing classification results:
    print(predicted, file= dataset)
    print('accuracy: {}'.format(accuracy), file= dataset)
    print("", file= dataset)  # Blank line to indicate end of fold.


if __name__ == '__main__':


    start_time = time.time()
    task2()
    print("--- %s seconds ---" % (time.time() - start_time))
    start_time = time.time()
    task3()
    print("--- %s seconds ---" % (time.time() - start_time))
    start_time = time.time()
    task4()
    print("--- %s seconds ---" % (time.time() - start_time))
