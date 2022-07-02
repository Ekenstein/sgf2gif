# sgf2gif
A program that converts an SGF-file to an animated GIF.

```shell
Usage: sgf2gif options_list
Options: 
    --file, -f -> The SGF-file to convert to a GIF (always required) 
    --output, -o -> The destination file to write the GIF to. (always required) 
    --theme [NES] -> The theme to render the board with { Value should be one of [classic, nes] }
    --loop, -l [false] -> Whether the animation should be looped or not 
    --width, -w [1000] -> The width of the image. { Int }
    --height, -h [1000] -> The height of the image. { Int }
    --move-number, -mn [2147483647] -> The move number up to which the animation will run to. { Int }
    --delay, -d [2] -> The delay between frames in seconds. { Int }
    --show-move-number [false] -> Whether each stone should be annotated with its move number or not. 
    --remove-captured-stones, -r [false] -> Whether captured stones should be removed from the board or not. 
    --help -> Usage info
```

### NES theme
```shell
java -jar sgf2gif.jar -f ~/game.sgf -o ~/game.gif -r --theme nes
```
![](https://github.com/Ekenstein/sgf2gif/blob/main/nes.gif?raw=true)

### Classic theme
![](https://github.com/Ekenstein/sgf2gif/blob/main/Ding_Hao-Tuo_Jiaxi.gif?raw=true)
