from trectools import TrecQrel, TrecRun, procedures, misc
import os
import numpy as np


def collect(qrelsFilePath, baseDir):
    qrels = TrecQrel(qrelsFilePath)

    result = {}
    for i, [topicPath, topicNum] in enumerate(sorted(_getDirectoryContent(baseDir, directory=True), key=lambda a_b: int(a_b[1]))):
        for modelPath, modelName in _getDirectoryContent(topicPath, directory=True):
            modelName = modelName[:-4]
            if modelName not in result:
                result[modelName] = {}

            for filePath, fileName in _getDirectoryContent(modelPath, file=True):
                score = 0

                # only evaluate non empty files
                if os.path.getsize(filePath) > 0:
                    run = TrecRun(filePath)
                    runResult = run.evaluate_run(qrels, True)
                    rs = list(runResult.get_results_for_metric(
                        'P_10').values())
                    score = np.mean(rs)

                if fileName not in result[modelName]:
                    result[modelName][fileName] = [score]
                else:
                    result[modelName][fileName].append(score)
            print("Finished processing model {} of topic {}".format(
                modelName, topicNum))
        print("Finished processing topic: ", topicNum)

    # Calculate average over all topics
    for modelName in result:
        for comparisonName in result[modelName]:
            result[modelName][comparisonName] = sum(
                result[modelName][comparisonName]) / len(result[modelName][comparisonName])

    return result


def _getDirectoryContent(path, file=False, directory=False):
    return [(os.path.join(path, o), o) for o in os.listdir(path)
            if (directory and os.path.isdir(os.path.join(path, o)) or (file and os.path.isfile(os.path.join(path, o))))]
