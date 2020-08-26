import evaluator_base as eval
import sys
from os import path, walk

# qrels path
# path to root folder of all runs


def main():
    if len(sys.argv) < 3:
        exit(1)

    qrelsFileDir = sys.argv[1]
    testRunDir = sys.argv[2]
    k = int(sys.argv[3])

    scores = {}
    qRelsContent = eval.readQrelsFile(qrelsFileDir)

    # go through all files topicNum/retrievalModel/dMul-pMul-aMul.txt
    for (root, _, files) in walk(testRunDir):
        for file in files:
            filePath = path.join(root, file)
            # extract the multipliers from the file name
            dMul, pMul, aMul = file[:-4].split('-')
            # caluclate the score of that file
            calcuatedScore = eval.score(
                qRelsContent, eval.readRun(filePath, k), k)
            # add the score to the right place in the scores dict
            if dMul not in scores:
                scores[dMul] = {}
            if pMul not in scores[dMul]:
                scores[dMul][pMul] = {}
            if aMul not in scores[dMul][pMul]:
                scores[dMul][pMul][aMul] = []
            scores[dMul][pMul][aMul].append(calcuatedScore)

    # calculate the average score for the topics for the given multipliers
    for dMul in scores:
        for pMul in scores[dMul]:
            for aMul in scores[dMul][pMul]:
                avgScore = sum(scores[dMul][pMul][aMul]) / \
                    len(scores[dMul][pMul][aMul])
                scores[dMul][pMul][aMul] = avgScore

    bestMults = (0, 0, 0)
    bestScore = -1
    for dMul in scores:
        for pMul in scores[dMul]:
            for aMul in scores[dMul][pMul]:
                if avgScore > bestScore:
                    bestScore = scores[dMul][pMul][aMul]
                    bestMults = (dMul, pMul, aMul)
    print(bestMults, bestScore)


if __name__ == '__main__':
    main()
