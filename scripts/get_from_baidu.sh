#!/bin/bash

wget 'https://fanyi.baidu.com/gettts?lan=zh&text=吃葡萄不吐葡萄皮，不吃葡萄倒吐葡萄皮' -O ./s1_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=说曹操曹操就到' -O ./s2_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=黑鲤鱼绿鲤鱼和驴' -O ./s3_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=学如逆水行舟，不进则退。' -O ./s4_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=这是蚕，那是蝉，蚕常在叶里藏，蝉常在林里唱' -O ./s5_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=魑魅魍魉' -O ./s6_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=八仙过海，各显其能' -O ./s7_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=冰冻三尺，非一日之寒' -O ./s8_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=乘兴而来，败兴而归' -O ./s9_f.mp3
wget 'https://fanyi.baidu.com/gettts?lan=zh&text=春蚕到死丝方尽，蜡炬成灰泪始干' -O ./s10_f.mp3

for((i=1;i<11;i++));do ffmpeg -i s${i}_f.mp3 -ac 1 -ar 44100 -acodec pcm_s16le -f wav ./s${i}_f.wav; done
