The entire project was done on WSL (Ubuntu). I do not think it will work properly on the
Windows shell. The program takes in a file input using the JFileChooser class in java. To create a
greyscale image, the program takes 0.299 of the red, 0.587 of the green, and 0.114 from the blue.
These values came from the standard YUV conversion matrix. The next image was
the darkened images. For both the coloured and grey images, all three rbg values were divided by
2 and converted into an integer to get the values for the darker image. The next part was the
ordered dithered image. The 4x4 matrix ((0, 8, 2, 10), (12, 4, 14,
6), (3, 11, 1, 9), (15, 7, 13, 5)) was used. This matrix worked decently, so it was chosen to be the matrix.
To properly compare values when dithering, the grey value of the pixel had to first be divided by
15. The last step was the auto level image. The auto level image’s algorithm works by finding the
maximum and minimum values for red, green, and blue in the image. Then a factor of 255 / the
difference between the maximum and maximum values is used. The values of the pixel were
subtracted by the minimum values and then multiplied by the factor. This made very little
difference in the image. To compensate for this, additional saturation was added to the image to
make it more different. The interlaced image displays the image where every even column is black, so that it creates a CRT like image. The auto interlace image is similar but the odd columns (the non-black ones) are taken from the auto level image. The third interlaced image uses values from the darker image and the auto level image. This causes the interlaced effect to be less noticeable but still there.
The program uses while loops to let the user go back to previous images.
The large outer while loop can be broken out of by not choosing a file which ends the program.
The inner while loop can be broken out of by clicking the next image button.
