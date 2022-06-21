# sgf2gif
A program that converts an SGF-file to an animated GIF.

```shell
Usage: sgf2gif options_list
Options: 
    --file, -f -> The SGF-file to convert to a GIF (always required) 
    --output, -o -> The destination file to write the GIF to. (always required) 
    --loop, -l [true] -> Whether the animation should be looped or not 
    --width [1000] -> The width of the image. { Int }
    --height [1000] -> The height of the image. { Int }
    --move-number, -mn [2147483647] -> The move number up to which the animation will run to. { Int }
    --delay, -d [2] -> The delay between frames in seconds. { Int }
    --show-move-number [true] -> Whether each stone should be annotated with its move number or not. 
    --help, -h -> Usage info
```

![](https://github.com/Ekenstein/sgf2gif/blob/main/example.gif?raw=true)
