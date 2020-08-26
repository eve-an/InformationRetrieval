import sys
import math
import re
import numpy as np
from os import walk, path

from sklearn.metrics import ndcg_score


def score(qRelsContent, fileContent, k):
    if len(fileContent) == 0:
        return 0

    truths = []
    guesses = []

    # calculate the dcg score based on our output
    for lineData in fileContent:
        topicNum, id, guess = lineData['topicNum'], lineData['id'], lineData['guess']
        guesses.append(float(guess.replace(',', '.')))
        if topicNum in qRelsContent and id in qRelsContent[topicNum]:
            truths.append(float(qRelsContent[topicNum][id]))
        else:
            truths.append(float(0))

    truth = np.asarray([truths])
    guess = np.asarray([guesses])

    return ndcg_score(truth, guess, k=k)


def score_old(qRelsContent, fileContent, k):
    if len(fileContent) == 0:
        return 0

    # dcg score
    dcg = 0.0
    # extracted topic number
    _topicNum = 0
    # calculate the dcg score based on our output
    for lineNum, lineData in enumerate(fileContent):
        topicNum, id = lineData['topicNum'], lineData['id']
        # safe the topic number so it can be retieved in the qRelsContent dict
        _topicNum = topicNum
        if topicNum in qRelsContent and id in qRelsContent[topicNum]:
            weight = qRelsContent[topicNum][id]
            dcg += weight / math.log(lineNum + 1, 2)

    # dcgStart score
    dcgStar = 0.0
    # get all scores and sort them in descending order
    values = [d for d in qRelsContent[_topicNum].values()]
    values.sort(reverse=True)
    # turn all negative numbers into zeros
    map(lambda val: min(0, val), values)
    # add some additional zeros so that there are k rows
    if len(values) < int(k):
        values = values + [0 for i in range(k-len(values))]

    # limit the size
    values = values[:int(k)]
    # calculate dcg*
    for i, v in enumerate(values):
        l = math.log((i + 1), 2)
        if l > 0:
            dcgStar += v / l

    # return the normalized score
    return dcg / dcgStar


def readRun(pathToFile, lineCount):
    fileContent = []
    with open(pathToFile) as f:
        content = [l.lstrip() for l in f.readlines()]
        for i, line in enumerate(content):
            s = line.split(' ')
            if len(s) == 0 or len(s[0]) == 0:
                continue
            fileContent.append({'topicNum': s[0], 'id': s[2], 'guess': s[4]})
            if i+1 == lineCount:
                break
    return fileContent


def readQrelsFile(pathToFile):
    qRelsContent = {}
    with open(pathToFile) as f:
        content = [l.strip() for l in f.readlines()]
        for line in content:
            topicNum, _, argId, weight = line.split(' ')
            if topicNum not in qRelsContent:
                qRelsContent[topicNum] = {}
            if argId not in qRelsContent[topicNum]:
                qRelsContent[topicNum][argId] = {}

            qRelsContent[topicNum][argId] = float(weight)
    return qRelsContent
