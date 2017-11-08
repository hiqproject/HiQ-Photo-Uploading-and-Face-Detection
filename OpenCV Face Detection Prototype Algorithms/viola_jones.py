# -*- coding: utf-8 -*-
"""
Created on Thu Mar 23 09:27:56 2017

A prototype implementation of the Viola-Jones algorithm.

@author: Vincenzo Iandolo
"""

import cv2
import sys
import time

# Get user supplied values from the command-line (Idea is to pass image and classifer through command-line)
imagePath = sys.argv[1]
classifierPath = sys.argv[2]

# Begin a timer
t0 = time.time()

# Create the boosted classifier
# The classifier XML file contains data to detect a face
faceCascade = cv2.CascadeClassifier(classifierPath)

# Read the image
image = cv2.imread(imagePath)

# Convert image to greyscale as including the colours means having exponentially far more variables﻿
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# Detect faces in the image
# The detectMultiScale function is a general function that detects objects via the Viola-Jones method.
faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor = 1.2,   # worth experimenting with 
        minNeighbors = 5,  	 # worth experimenting with
        minSize = (30, 30),  # worth experimenting with
)

'''
# Uncomment to get confidence level readings
faces = faceCascade.detectMultiScale3(
        gray,
        scaleFactor = 1.4,  
        minNeighbors = 5,  
        minSize = (30, 30), 
        outputRejectLevels = True
)

rects = faces[0]
neighbours = faces[1]
weights = faces[2]
#print(rects)
#print(neighbours)
print(weights)
'''

# print the number of faces found in an image.
print ("Found {0} faces!".format(len(faces)))

# This function returns 4 values: the x and y location of the rectangle, and the rectangle’s width and height (w , h).
for (x, y, w, h) in faces:
    # (x, y) is the starting points and (x+w, y+h) are the ending points
    cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)    # (0, 255, 0) is the colour green in RGB
    
# Display the image
cv2.imshow("Faces found", image)

# End the timer    
t1 = time.time()

# Calculate the difference between start and end times in order to get a final time of execution.
print ("Algorithm took ",t1-t0, ' seconds')

# Wait for the user to press a key.
cv2.waitKey(0)