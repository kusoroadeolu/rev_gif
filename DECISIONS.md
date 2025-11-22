## DECISIONS
This file includes some decisions I've made so far

- Renamed my tables from gifs and frame_hashes to media and frames cuz it's easier to anyone to understand
- Ditched validation, frame extraction and hashing being async cause it made no sense? What am I not blocking the main thread from
- Decided to split uploaded media up to 5 frames max, before passing it through the image client to get a wider range of tags to upload to tenor
- For the downloaded gifs from tenor, I decided to actually extract all frames from the gif and compare them against the uploaded media for more acc results even though it means a bit longer waiting time
- I also decided to increase the tenor download limit to 15 to have a wider range of options to choose from
- I actually ran into a weird bug whereby my list wasn't populated in the completable future chain before being sent to the DB, idk what happened but it started working
- I decided to pass lists as events instead of sending events to the sse emitter directly. The reason for this is so the frontend(later on) will know when no more events from the sse emitter are arriving so it can stop showing loading...
- I also realized that redis doesn't serialize sse emitters properly so my initial plan to tie the sse emitter to redis kinda failed. I still have to use a map for that,
</br> On the other hand, I used keyspace events to tie the session to a dummy value so when the session expires I can clean up the emitter
- I also decided to use executors to ensure async ordering for my sse emitter to prevent any weird race conditions. Since executors force async ordering(cause of its blocking queue under the hood)
- I decided to filter out frames that aren't similar to the uploaded image/gif and only save those which are similar to prevent stuffing my db with irrelevant frames
- Added gaussian blur to help with 