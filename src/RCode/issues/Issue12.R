library(jmotif)
x = 1:15
inc = 14/7

plot(x, type="l");points(x)
breaks=seq(1,15,by=inc)
abline(v=breaks,lty=2,col="cyan")

for (i in x) {
  idx = round(i/inc) - 1
  if(idx<0){idx=0}
  if(idx>6){idx=6}
  print(paste(i, " -> ", idx))
  text(i-0.2,x[i]+0.2, idx)
}

p = paa(x,7)
for(i in 1:(length(breaks)-1)){
  segments(x0=breaks[i],x1=breaks[i+1],y0=p[i],y1=p[i],col="red")
}
segments(x0=breaks[7],x1=15,y0=p[7],y1=p[7],col="red")

total = 0
for (i in x) {
  idx = round(i/inc) - 1
  if(idx<0){idx=0}
  if(idx>6){idx=6}
  dd = dist(t(cbind( c(i), c(p[idx+1]) )))
  print(paste(i, " -> ", dd))
  text(i+0.4,x[i]-0.2, round(dd, digits = 2))
  total=total+dd
}
text(4, 10, paste("APPROX. DISTANCE:",total))

points(x=breaks[1:7]+1, y=p, col="blue")
lines(x=breaks[1:7]+1, y=p, col="blue")

p
