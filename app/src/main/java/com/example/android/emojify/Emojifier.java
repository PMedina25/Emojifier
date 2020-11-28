package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import timber.log.Timber;

public class Emojifier {

    private static final String LOG_TAG = Emojifier.class.getSimpleName();

    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final Double SMILING_THRESHOLD = 0.5;
    private static final Double RIGHT_EYE_OPENED_THRESHOLD = 0.5;
    private static final Double LEFT_EYE_OPENED_THRESHOLD = 0.5;

    /**
     * Method for detecting faces in a bitmap.
     *
     * @param context The application context.
     * @param picture The picture in which to detect the faces.
     */
    static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap picture) {

        // Create the face detector, disable tracking and enable classifications
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        // Build the frame
        Frame frame = new Frame.Builder().setBitmap(picture).build();

        // Detect the faces
        SparseArray<Face> faces = detector.detect(frame);

        // Log the number of faces
        Timber.d("detectFaces: number of faces = " + faces.size());

        // Initialize result bitmap to original picture
        Bitmap resultBitmap = picture;

        // If there are no faces detected, show a Toast message
        if (faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        } else {
            // Iterate through the faces, calling getClassifications() for each face
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                Bitmap emojiBitmap;
                switch (whichEmoji(face)) {
                    case SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.smile);
                        break;
                    case FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_frown);
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                }

                // Add the emojiBitmap to the proper position in the original image
                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);
            }


        }

        // Release the detector
        detector.release();

        return resultBitmap;
    }

    /**
     * Method for logging the classification probabilities.
     *
     * @param face The face to get the classification probabilities.
     */
    private static Emoji whichEmoji(Face face) {
        Emoji emoji = null;

        Float smilingProbability = face.getIsSmilingProbability();
        Float rightEyeProbability = face.getIsRightEyeOpenProbability();
        Float leftEyeProbability = face.getIsLeftEyeOpenProbability();

        // Log all the probabilities
        Timber.d("wichEmoji: smilingProb = " + smilingProbability);
        Timber.d("wichEmoji: leftEyeOpenProb = "
                + leftEyeProbability);
        Timber.d("wichEmoji: rightEyeOpenProb = "
                + rightEyeProbability);

        // Create an if/else system that selects the appropriate emoji based on the above booleans and log the result.
        if (smilingProbability > SMILING_THRESHOLD && leftEyeProbability > LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability > RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.SMILING;
        } else if (smilingProbability < SMILING_THRESHOLD && leftEyeProbability > LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability > RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.FROWNING;
        } else if (smilingProbability > SMILING_THRESHOLD && leftEyeProbability < LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability > RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.LEFT_WINK;
        }
        else if (smilingProbability > SMILING_THRESHOLD && leftEyeProbability > LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability < RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.RIGHT_WINK;
        }
        else if (smilingProbability < SMILING_THRESHOLD && leftEyeProbability < LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability > RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.LEFT_WINK_FROWNING;
        }
        else if (smilingProbability < SMILING_THRESHOLD && leftEyeProbability > LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability < RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.RIGHT_WINK_FROWNING;
        }
        else if (smilingProbability > SMILING_THRESHOLD && leftEyeProbability < LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability < RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.CLOSED_EYE_SMILING;
        }
        else if (smilingProbability < SMILING_THRESHOLD && leftEyeProbability < LEFT_EYE_OPENED_THRESHOLD && rightEyeProbability < RIGHT_EYE_OPENED_THRESHOLD) {
            emoji = Emoji.CLOSED_EYE_FROWNING;
        }

        // Log the chosen Emoji
        Timber.d("wichEmoji: " + emoji.name());

        return emoji;
    }

    /**
     * Combines the original picture with the emoji bitmaps
     *
     * @param backgroundBitmap The original picture
     * @param emojiBitmap      The chosen emoji
     * @param face             The detected face
     * @return The final bitmap, including the emojis over the faces
     */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

    // Create an enum class called Emoji that contains all the possible emoji you can make(smiling,
    // frowning, left wink, right wink, left wink frowning, right wink frowning, closed eye smiling,
    // close eye frowning).
    enum Emoji {
        SMILING,
        FROWNING,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWNING,
        RIGHT_WINK_FROWNING,
        CLOSED_EYE_SMILING,
        CLOSED_EYE_FROWNING
    }
}
