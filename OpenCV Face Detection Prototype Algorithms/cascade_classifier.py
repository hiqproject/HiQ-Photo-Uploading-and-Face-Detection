# -*- coding: utf-8 -*-
"""
Created on Mon Mar 30 21:06:47 2017

A prototype implementation of the Cascade Classifier method for face detection.

@author: Vincenzo Iandolo
"""

import cv2
import sys
import time

# Get user supplied values from the command-line (Idea is to pass image and classifer through command-line)
imagePath = sys.argv[1]
faceClassifier = sys.argv[2]
eyeClassifier = sys.argv[3]

# Begin a timer
t0 = time.time()

# Create the haar cascade
# Load the cascade classifier used to detect frontal facial features
faceCascade = cv2.CascadeClassifier(faceClassifier)

# Load the cascade classifier used to detect eyes
eyeCascade = cv2.CascadeClassifier(eyeClassifier)

# Read the image
image = cv2.imread(imagePath)

# Convert image to greyscale as including the colours means having exponentially far more variables﻿
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# Detect faces in the image
# The detectMultiScale function is a general function that detects objects via the Viola-Jones method.
faces = faceCascade.detectMultiScale(
        gray,
        scaleFactor = 1.2,  
        minNeighbors = 5,  
        minSize = (30, 30), 
)

print ("Found {0} faces!".format(len(faces)))

# This function returns 4 values: the x and y location of the face, as well as width and height
# Improved accuracy is attained by using the eye classifier cascaded with the face classifier
for (x, y, w, h) in faces:
    #(x, y) is the starting points and (x+w, y+h) are the ending points
    cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)   
    roi_gray = gray[y:y+h, x:x+w]
    roi_colour = image[y:y+h, x:x+w]
    eyes = eyeCascade.detectMultiScale(roi_gray)
    for (ex, ey, ew, eh) in eyes:
        cv2.rectangle(roi_colour, (ex, ey), (ex+ew, ey+eh), (0, 255, 0), 2)
    
# Display the image
cv2.imshow("Faces found", image)

# End the timer    
t1 = time.time()

# Calculate the difference between start and end times in order to get a final time of execution.
print ("Algorithm took ",t1-t0, ' seconds')

# Wait for the user to press a key.
cv2.waitKey(0)