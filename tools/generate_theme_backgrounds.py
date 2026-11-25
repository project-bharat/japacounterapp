#!/usr/bin/env python3
"""Render the original, offline Bhajame theme landscapes at a phone-friendly size."""
from PIL import Image, ImageDraw, ImageFilter
from pathlib import Path
import math

W,H=540,960
OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/drawable-nodpi'
INK=(53,49,96); DEEP=(40,38,77); SLATE=(104,126,153); MIST=(145,177,190); CREAM=(254,245,216)

def background():
 im=Image.new('RGB',(W,H)); px=im.load()
 stops=[(0,(32,29,69)),(.40,(60,56,104)),(.74,(125,151,171)),(.91,(235,222,195)),(1,(164,178,184))]
 for y in range(H):
  t=y/(H-1); a,b=next(((a,b) for a,b in zip(stops,stops[1:]) if a[0]<=t<=b[0]),(stops[-2],stops[-1])); f=(t-a[0])/(b[0]-a[0]);c=tuple(round(a[1][j]*(1-f)+b[1][j]*f) for j in range(3))
  for x in range(W):px[x,y]=c
 return im

def glow(im,x,y,r):
 overlay=Image.new('RGBA',(W,H));d=ImageDraw.Draw(overlay)
 for rad in range(r,1,-4):
  a=round(24*(1-rad/r)**2);d.ellipse((x-rad,y-rad,x+rad,y+rad),fill=(*CREAM,a))
 return Image.alpha_composite(im.convert('RGBA'),overlay)
def hill(d,color,points):d.polygon(points+[(W,H),(0,H)],fill=color)
def arc(d,box,fill,width):d.arc(box,180,360,fill=fill,width=width)
def water(d):
 d.rectangle((0,815,W,H),fill=(142,168,181));
 for x,y in [(130,846),(360,864),(240,912),(474,934)]:d.arc((x-24,y-3,x+24,y+5),180,360,fill=(197,212,207),width=3)
def petals(d,x,y,s):
 d.ellipse((x-s,y-s*.33,x+s,y+s*.3),fill=INK)
 for dx in [-.65,0,.65]:
  d.polygon([(x,y),(x+dx*s-s*.24,y-s*.7),(x+dx*s,y-s*1.05),(x+dx*s+s*.24,y-s*.7)],fill=INK)
def lamp(d,x,y):
 d.ellipse((x-30,y-30,x+30,y+30),fill=(223,217,195));d.ellipse((x-7,y-15,x+7,y-1),fill=CREAM);d.pieslice((x-18,y-6,x+18,y+12),0,180,fill=DEEP)
def mountain(d):
 hill(d,(130,140,155),[(0,817),(100,804),(190,813),(290,758),(400,820),(540,797)])
 d.polygon([(250,785),(290,758),(325,791),(309,786),(294,775),(287,781),(279,779)],fill=(225,220,213))
 hill(d,(84,96,137),[(0,846),(116,827),(233,855),(349,824),(540,850)])
 hill(d,INK,[(0,902),(135,853),(248,887),(383,860),(540,886)])
 hill(d,DEEP,[(0,932),(128,913),(264,928),(410,897),(540,918)])

def render(key):
 im=background(); im=glow(im,270,810,150); d=ImageDraw.Draw(im)
 if key in ('dawn','kailash'):
  d.ellipse((226,778,314,866),fill=CREAM);mountain(d)
  if key=='dawn':
   d.rectangle((430,810,460,837),fill=DEEP);d.polygon([(426,810),(445,775),(464,810)],fill=DEEP);d.rectangle((443,768,447,779),fill=DEEP)
  else:
   d.ellipse((334,729,363,758),fill=CREAM);d.ellipse((343,722,368,748),fill=(147,164,177))
 elif key=='ghat':
  water(d); d.polygon([(0,750),(38,750),(38,779),(83,779),(83,808),(127,808),(127,838),(170,838),(170,868),(210,868),(210,960),(0,960)],fill=INK)
  for y in [868,892,916]:d.line((72,y,184,y),fill=(92,96,138),width=4)
  lamp(d,411,878)
 elif key=='bodhi':
  d.ellipse((244,760,331,847),fill=CREAM);hill(d,(105,128,153),[(0,850),(140,829),(271,860),(420,830),(540,856)])
  d.polygon([(0,645),(20,690),(28,780),(62,840),(95,855),(150,860),(105,869),(55,856),(40,883),(24,873),(30,849),(0,833)],fill=INK)
  d.line([(36,834),(60,870),(80,891)],fill=INK,width=10);d.line([(42,839),(20,874),(15,899)],fill=INK,width=9)
  hill(d,DEEP,[(0,914),(130,896),(240,930),(400,881),(540,894)])
 elif key=='lotus':
  water(d)
  for x,y,s in [(65,866,27),(270,915,35),(458,870,31),(370,947,22),(120,940,21)]:
   d.ellipse((x-s*1.4,y-5,x+s*1.4,y+10),fill=(89,112,145));petals(d,x,y,s)
 elif key=='tulsi':
  d.ellipse((225,790,315,880),fill=CREAM);hill(d,(181,191,190),[(0,851),(150,828),(294,857),(430,836),(540,848)])
  d.polygon([(0,851),(85,851),(85,872),(145,872),(145,893),(190,893),(190,960),(0,960)],fill=(116,151,169))
  d.rectangle((410,843,520,960),fill=INK);d.polygon([(400,843),(408,825),(522,825),(530,843)],fill=INK)
  d.ellipse((454,885,480,915),fill=DEEP);d.ellipse((464,891,471,903),fill=CREAM)
  for x in [438,450,462,474,486,498]:
   d.line((465,831,x,753-abs(x-465)),fill=INK,width=3)
   for dy in [0,20,40]:d.ellipse((x-6,764+dy-abs(x-465),x+5,774+dy-abs(x-465)),fill=INK)
 elif key=='waves':
  for r,c in [(260,(110,141,161)),(215,(131,161,177)),(170,(159,182,186)),(128,(194,202,192)),(86,CREAM)]:
   d.ellipse((270-r,850-r,270+r,850+r),fill=c)
  hill(d,(109,142,160),[(0,883),(120,852),(260,892),(390,861),(540,895)])
  hill(d,(80,100,141),[(0,924),(140,885),(260,918),(400,883),(540,920)])
  hill(d,INK,[(0,951),(150,927),(260,945),(410,910),(540,944)])
 elif key=='narmada':
  water(d); d.polygon([(0,836),(120,817),(203,839),(150,856),(67,865),(0,858)],fill=(108,128,148));d.polygon([(300,835),(410,820),(540,823),(540,854),(402,849)],fill=(105,124,147))
  for x,y,rx in [(50,889,36),(165,924,27),(399,905,31),(500,934,37)]:d.ellipse((x-rx,y-12,x+rx,y+12),fill=INK)
  d.rectangle((459,777,466,823),fill=INK);d.rectangle((503,777,510,823),fill=INK);d.polygon([(448,777),(455,764),(485,750),(515,764),(522,777)],fill=INK);d.ellipse((481,742,489,751),fill=INK)
  for x in [18,35,510,525]:d.line((x,930,x-8,865),fill=INK,width=3);d.ellipse((x-13,852,x-5,874),fill=INK)
 return im.convert('RGB')

OUT.mkdir(parents=True,exist_ok=True)
for k in ('dawn','ghat','bodhi','lotus','kailash','tulsi','waves','narmada'):
 render(k).save(OUT/f'theme_{k}.webp',format='WEBP',quality=88,method=6)
