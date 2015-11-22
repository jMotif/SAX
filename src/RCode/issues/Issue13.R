library(jmotif)

x = 1:15 # series is 15 points long
inc = 15/7 # there shall be 7 segments

png(filename = "issues/Issue13.png", width = 850, height = 650, units = "px", pointsize = 18,
    bg = "white", type = c("cairo", "cairo-png", "Xlib", "quartz"), antialias="subpixel")

x = znorm(x)
plot(x, type="l", xlim=c(0,16), ylim=c(-2,2), col="cornflowerblue",
     main="Approximation distance computation"); 
points(x, col="cornflowerblue", cex=1.2)
breaks=seq(1-0.5,16,by=inc)
abline(v=breaks,lty=2,col="cyan")

paa7 = jmotif::paa(x, 7)
paa_centers <- (breaks + (inc / 2))[1:7]
points(x=paa_centers, y=paa7, pch=9, cex=0.7, col="brown")
segments(x0=breaks[1:7],x1=breaks[2:8],y0=paa7,y1=paa7,col="brown")

cuts3=jmotif::alphabet_to_cuts(3)[2:3]
abline(h=cuts3,col="red",lty=3)
abline(h=c(-0.967421566101701,0,0.967421566101701),col="magenta",lwd=3,lty=2)
yx <- seq(-2,2, length=30)
xx <- dnorm(yx, mean=0, sd=1)*5
lines(xx,yx, type="l", lwd=4, col="magenta")

library(plyr)
indexes = aaply(jmotif::series_to_chars(paa7, 3),1,jmotif::letter_to_idx)
central_lines = c(-0.967421566101701,0,0.967421566101701)

i=0
idx=2
total=0
for (i in 0:14) {
  
  idx = floor((i+0.5)/inc)
  if(idx<0){idx=0}
  if(idx>6){idx=6}
  
  text(i+1-0.4,x[i+1]+0.45, idx, col="violet")
  
  dd = as.numeric(dist(t(cbind( c(x[i+1]), c(central_lines[indexes[idx+1]]) ))))
  segments(paa_centers[idx+1],paa7[idx+1],paa_centers[idx+1],central_lines[indexes[idx+1]],
           col="green",lwd=2)
  total = total + dd
  
  text(i+1+0.5,x[i+1]-0.45, paste(round(total,2)), col="blue")
}
legend("topleft", 
       c("Assumed PAA index for original point",
         "Cumulative distance along original points",
         "Accounted distance segments",
         "Centers of cut segments, A=3"
         ), 
       lty=c(1,1,1,2),lwd=c(3,3,3,3),col=c("violet","blue","green","magenta"))

text(10, -1.8, paste("Total dist:",total), col="blue", adj=c(0,0))

dev.off()