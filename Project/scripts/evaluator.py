from matplotlib import cm
from mpl_toolkits.mplot3d import Axes3D
import evaluator_base as eval
import numpy as np
import sys
import os

import matplotlib.pyplot as plt
plt.rcdefaults()


def main():
    if len(sys.argv) < 5:
        print("sm/pr qrelsFilePath testRunDir diagramOutputDir")
        exit(1)

    evaluationType = sys.argv[1]
    qrelsFilePath = sys.argv[2]
    testRunDir = sys.argv[3]
    diagramOutputDir = sys.argv[4]

    result = eval.collect(qrelsFilePath, testRunDir)

    if evaluationType.lower() == 'sm':
        singleMulti(result, diagramOutputDir)
    if evaluationType.lower() == 'pr':
        parameterRun(result, diagramOutputDir)


def singleMulti(result, diagramOutputDir):
    for modelName in result:
        objects = [name[:-4] for name in result[modelName].keys()]
        y_pos = np.arange(len(objects))
        performance = [value for value in (
            result[modelName][comparisonName] for comparisonName in result[modelName])]

        plt.bar(y_pos, performance, align='center', alpha=0.5)
        plt.xticks(y_pos, objects)
        plt.ylabel('NDCG')
        plt.title('Retrieval Models')

        plt.savefig(os.path.join(diagramOutputDir,
                                 modelName + '.png'), bbox_inches='tight')


def parameterRun(result, diagramOutputDir, maxCount=None):
    for modelName in result:
        print(modelName)
        print('-------------------------------------------------------------')
        count = len(result[modelName])
        if maxCount:
            count = maxCount
        for k, v in sorted(result[modelName].items(), key=lambda k_v: k_v[1])[:min(count, len(result[modelName]))]:
            print(k, v)
        print('-------------------------------------------------------------')


def _parameterRun(result, diagramOutputDir):
    for modelName in result:
        X = set()
        Y = set()
        C = []
        for comparisonName in result[modelName]:
            dMul, pMul, aMul = comparisonName[:-4].split('-')
            X.add(float(dMul))
            Y.add(float(pMul))

        X = list(X)
        X.sort()
        Y = list(Y)
        Y.sort()
        Z = np.zeros((len(X), len(Y)))

        for comparisonName in result[modelName]:
            dMul, pMul, aMul = comparisonName[:-4].split('-')
            Z[X.index(float(dMul))][Y.index(float(pMul))] = float(aMul)
            C.append(float(result[modelName][comparisonName]))

        X = np.asarray(X)
        Y = np.asarray(Y)
        X, Y = np.meshgrid(X, Y)

        # create the figure, add a 3d axis, set the viewing angle
        ax = plt.axes(projection='3d')
        ax.plot3D(X, Y, Z, 'grey')
        plt.show()


if __name__ == '__main__':
    main()
