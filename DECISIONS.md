## DECISIONS
This file includes some decisions I've made so far

- Renamed my tables from gifs and frame_hashes to media and frames cuz it's easier to anyone to understand
- Ditched validation, frame extraction and hashing being async cause it made no sense? What am I not blocking the main thread from
- Decided to split uploaded media up to 5 frames max, before passing it through the image client to get a wider range of tags to upload to tenor
- For the downloaded gifs from tenor, I decided to actually extract all frames from the gif and compare them against the uploaded media for more acc results even though it means a bit longer waiting time
- I also decided to increase the tenor download limit to 15 to have a wider range of options to choose from