# INTRO
## Hamming Distance - A quick recap
Hamming distance between two equal length strings is the number of positions at which corresponding symbols are different
</br> The hamming distance between `james` and `janet` = 2. The hamming distance of 2 words is zero when both are identical. It also satisfies triangle inequality
</br> The hamming distance between two binary values `a` and `b` is `a ^ b` (i.e. the pop count of a and b)
</br> Hamming distance is also applied in error correction i.e. finding the k error between two given codes where `k = min hamming distance - 1` 
</br> It is also applied in image processing and deduping


## Perpetual Hashes
These are a class of algorithms comparable to hash functions which use certain features in images to generate a distinct(but not unique) fingerprint of an image which is comparable.
</br> There are multiple perpetual hash algorithms. Here are a few:
1. **Average hash:** It is a basic average based on low frequencies. It's relatively simple to write: 
- Downsizing the image aspect ratio to 8 x 8 (64 pixels) to reduce high frequencies
- Converting the image to gray scale (changes the pic from 64 pixels `red, blue, green` to 64 total colors)
- Computing the mean of all the colors
- Comparing each color against the mean. Each bit is set based on if the color value is above or beneath the mean
- Simply set up the bits into a `64 bit` integer. The order does not matter as long as you're consistent. Also average hash is very fast
You can compare this hash by calculating the `hamming distance` between both hashes
</br> A distance of zero means they're visually similar, same with a distance of 5. But this in practice might not always be the case as I've noticed
</br> While average hash might be good for finding similar images, it has significant issues. It is highly dependent on linear changes(i.e. Every pixel gets transformed the same way) in images. 
</br> Non-linear changes in the properties of images (i.e. Some pixels are transformed exponentially e.g. gamma correction, histogram equalization)

2. **DCT-Hash:** DCT simply means Discrete Cosine Transform. It represents a finite set of data points as cosine functions oscillating at different frequencies. This is what `perspective hash` uses
</br> For images it takes a pixel at a certain point and composes it as a sum of cosine waves. The DCT hash of an image can be calculated such as
- Rescaling the aspect ratio to 32 x 32(to make DCT calculation easier, not to reduce high frequencies)
- Compute the DCT for the image(Separating the image into frequencies and scalars)
- Reduce the DCT(keeping only the top left 8 x 8 because it contains the lowest frequencies). 
</br> The lowest frequencies contain the overall structure(i.e. perceptually important) of the image even if they aren't the sharpest. It's what every pHash algo depends on
- Then compute the average DCT value(Using the 8 x 8 DCT value)
- Further reduce the DCT. Set the 64 hash bits to 0 or 1 based on whether they are greater than the mean DCT value
- Simply set up the bits into a `64 bit` integer. The order does not matter as long as you're consistent.

## Conclusion
This brief intro should give you a basic understanding of hamming distance and perpetual algorithms

# RevGif
This project was inspired by `TinyEye`. 