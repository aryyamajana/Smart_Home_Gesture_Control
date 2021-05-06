# -*- coding: utf-8 -*-
"""
Created on Thu Jan 28 00:44:25 2021

@author: chakati
"""
import os
import cv2
import tensorflow as tf
import numpy as np
import frameextractor as frameEx
from numpy import dot
from numpy.linalg import norm
## import the handfeature extractor class
import handshape_feature_extractor

def cos_sim(a, b):
    """Takes 2 vectors a, b and returns the cosine similarity
    """
    return dot( a, b ) / (norm( a ) * norm( b ))


def list_unhiddendir(path):
    fileList = []
    for f in os.listdir(path):
        if f.endswith( '.txt' ):
            continue
        if not f.startswith('.'):
            fileList.append( os.path.join( path, f ) )
    return (fileList)


root = os.path.dirname( os.path.abspath( __file__ ) )
train = root + "/" + 'traindata'
test = root + "/" + 'test'
framesTrain = root + "/" + 'Frames_Train'
framesTest = root + "/" + 'Frames_Test'
Train_map = root + "/" + 'map.csv'
gesMap = np.genfromtxt(Train_map, delimiter=',', skip_header=1, dtype = (np.str))
result = root + "/" + 'Results.csv'

# =============================================================================
# Get the penultimate layer for trainig data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video
print( 'Extracting the middle frame of each training gesture video...' )
fileList = list_unhiddendir(train)
for f1 in fileList:
    frameEx.frameExtractor( f1, framesTrain, 1)
fileList = list_unhiddendir( framesTrain )
trainX = []
trainY = []
trainFilesArray = []
print( 'Build feature vector and Assign label as per map to each training frame...' )
for imgfile in fileList:
    img = cv2.imread( imgfile, cv2.IMREAD_UNCHANGED )
    gray = cv2.cvtColor( img, cv2.COLOR_BGR2GRAY )
    f = handshape_feature_extractor.HandShapeFeatureExtractor.get_instance()
    feature_vector = f.extract_feature( gray )
    trainX.append( feature_vector )
    trainFilesArray.append( os.path.basename( imgfile ).split( "." )[0] )
    rows = np.where( gesMap[:, 1] == os.path.basename( imgfile ).split( "." )[0] )
    result1 = gesMap[rows]
    trainY.append(int(result1[0][2]))

# =============================================================================
# Get the penultimate layer for test data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video
print( 'Extracting the middle frame of each test gesture video...' )
fileList = list_unhiddendir(test)
for f2 in fileList:
    frameEx.frameExtractor( f2, framesTest, 1)
fileList = list_unhiddendir( framesTest )
testX = []
testFilesArray = []
print( 'Build test feature vector...' )
for imgfile in fileList:
    img = cv2.imread( imgfile, cv2.IMREAD_UNCHANGED )
    gray = cv2.cvtColor( img, cv2.COLOR_BGR2GRAY )
    f = handshape_feature_extractor.HandShapeFeatureExtractor.get_instance()
    feature_vector = f.extract_feature( gray )
    testX.append( feature_vector )
    testFilesArray.append( os.path.basename( imgfile ).split( "." )[0] )

# =============================================================================
# Recognize the gesture (use cosine similarity for comparing the vectors)
# =============================================================================
predictedLabel = []
lossY = []
print( 'Recognizing the gesture using cosine similarity for vector comparison..' )
for test_vector in testX:
    loss = []
    label = []
    it = 0
    for files in trainFilesArray:
        train_vector = trainX[it]
        print(train_vector)
        tr1 = np.squeeze( np.asarray( train_vector ) )
        print(tr1)
        ts1 = np.squeeze(np.asarray(test_vector))
        train_label = trainY[it]
        # loss.append( tf.keras.losses.CosineSimilarity( axis=1 )( ts1, tr1 ).numpy() )
        loss.append(cos_sim(ts1, tr1))
        label.append( train_label )
        it = it + 1
    # Add min cosine loss to array
    lossY.append(min(dict(zip(label, loss)).values()))
    # Determine class pertaining to minimum loss
    predictedLabel.append(min(dict(zip(label, loss)), key=dict(zip(label, loss)).get))

print( 'Saving predictions to output csv file..')
print(predictedLabel)
np.savetxt(result, predictedLabel, fmt='%i', delimiter=',')
